/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencord.igmpproxy.impl;

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.statemacine.StateMachine;
import org.opencord.igmpproxy.statemacine.State;
import org.opencord.igmpproxy.statemacine.StateConstants;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.statemacine.StateMachineInfo;
import org.opencord.igmpproxy.statemacine.StateMachineStore;

import java.util.Random;


/**
 * State machine implementation.
 */
public class SingleStateMachine implements StateMachine {
    private StateMachineInfo stateMachineInfo;
    private State[] states = {
            new NonMember(), new DelayMember(), new IdleMember()
    };
    private int[] nonTransition =
            {StateConstants.STATE_DELAY, StateConstants.STATE_NON,
                    StateConstants.STATE_NON, StateConstants.STATE_NON};
    private int[] delayTransition =
            {StateConstants.STATE_DELAY, StateConstants.STATE_NON,
                    StateConstants.STATE_DELAY, StateConstants.STATE_IDLE};
    private int[] idleTransition =
            {StateConstants.STATE_IDLE, StateConstants.STATE_NON,
                    StateConstants.STATE_DELAY, StateConstants.STATE_IDLE};
    //THE TRANSITION TABLE
    private int[][] transition =
            {nonTransition, delayTransition, idleTransition};

    private StateMachineStore stateMachineStore;

    public SingleStateMachine(StateMachineInfo info, StateMachineStore stateMachineStore) {
        this.stateMachineInfo = info;
        this.stateMachineStore = stateMachineStore;
    }

    @Override
    public StateMachineId getStateMachineId() {
        return this.stateMachineInfo.getStateMachineId();
    }

    @Override
    public void setStateMachineInfo(StateMachineInfo info) {
        this.stateMachineInfo = info;
    }

    @Override
    public Ip4Address getGroupIp() {
        return this.stateMachineInfo.getStateMachineId().getGroupIp();
    }

    @Override
    public DeviceId getDeviceId() {
        return this.stateMachineInfo.getStateMachineId().getDeviceId();
    }

    @Override
    public Integer getTimeOut() {
        return this.stateMachineInfo.getTimeOut();
    }

    @Override
    public StateMachineInfo getInfo() {
        return this.stateMachineInfo;
    }

    @Override
    public int currentState() {
        return this.stateMachineInfo.getCurrentState();
    }

    @Override
    public void join(boolean messageOutAllowed) {
        int currentState = this.stateMachineInfo.getCurrentState();
        State state = states[currentState];
        state.join(messageOutAllowed);
        next(StateConstants.TRANSITION_JOIN);
    }

    @Override
    public void leave(boolean messageOutAllowed) {
        int currentState = this.stateMachineInfo.getCurrentState();
        State state = states[currentState];
        state.leave(messageOutAllowed);
        next(StateConstants.TRANSITION_LEAVE);
    }

    @Override
    public void query(int maxResp) {
        int currentState = this.stateMachineInfo.getCurrentState();
        State state = states[currentState];
        state.query(maxResp);
        next(StateConstants.TRANSITION_QUERY);
    }

    @Override
    public void timeOut(boolean sendQuery) {
        int currentState = this.stateMachineInfo.getCurrentState();
        State state = states[currentState];
        state.timeOut(sendQuery);
        next(StateConstants.TRANSITION_TIMEOUT);
    }

    protected void next(int msg) {
        int currentState = this.stateMachineInfo.getCurrentState();
        this.stateMachineInfo.setCurrentState(transition[currentState][msg]);
    }

    private abstract class AbstractState implements State {

        protected AbstractState() {
        }

        public void join(boolean messageOutAllowed) {
        }

        public void leave(boolean messageOutAllowed) {
            DeviceId devId = stateMachineInfo.getStateMachineId().getDeviceId();
            Ip4Address groupIp = stateMachineInfo.getStateMachineId().getGroupIp();
            Ip4Address srcIp = stateMachineInfo.getSrcIp();
            PortNumber upLinkPort = stateMachineInfo.getUpLinkPort();

            if (messageOutAllowed) {
                Ethernet eth = IgmpManager.outgoingIgmpWithV3() ?
                        IgmpSender.getInstance().buildIgmpV3Leave(groupIp, srcIp) :
                        IgmpSender.getInstance().buildIgmpV2Leave(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId, upLinkPort);
            }
        }

        public void query(int maxResp) {
        }

        public void timeOut(boolean sendQuery) {
        }

        protected int getTimeOut(int maxTimeOut) {
            Random random = new Random();
            return Math.abs(random.nextInt()) % maxTimeOut;
        }
    }

    private class DelayMember extends AbstractState {
        public DelayMember() {
            super();
        }

        @Override
        public void query(int maxResp) {
            if (maxResp < stateMachineInfo.getTimeOut()) {
                int timeout = getTimeOut(maxResp);
                stateMachineStore.resetTimer(stateMachineInfo.getStateMachineId(), timeout);
            }
        }

        @Override
        public void timeOut(boolean sendQuery) {
            if (sendQuery) {
                DeviceId devId = stateMachineInfo.getStateMachineId().getDeviceId();
                Ip4Address groupIp = stateMachineInfo.getStateMachineId().getGroupIp();
                Ip4Address srcIp = stateMachineInfo.getSrcIp();
                PortNumber upLinkPort = stateMachineInfo.getUpLinkPort();

                Ethernet eth = IgmpManager.outgoingIgmpWithV3() ?
                        IgmpSender.getInstance().buildIgmpV3ResponseQuery(groupIp, srcIp) :
                        IgmpSender.getInstance().buildIgmpV2ResponseQuery(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId, upLinkPort);
                stateMachineStore.setMaxTimeout(stateMachineInfo.getStateMachineId());
            }
        }
    }

    private class IdleMember extends AbstractState {
        public IdleMember() {
            super();
        }

        @Override
        public void query(int maxResp) {
            int timeout = this.getTimeOut(maxResp);
            stateMachineStore.startTimer(stateMachineInfo.getStateMachineId(), timeout);
        }
    }

    private class NonMember extends AbstractState {
        public NonMember() {
            super();
        }

        @Override
        public void join(boolean messageOutAllowed) {
            if (messageOutAllowed) {
                DeviceId devId = stateMachineInfo.getStateMachineId().getDeviceId();
                Ip4Address groupIp = stateMachineInfo.getStateMachineId().getGroupIp();
                Ip4Address srcIp = stateMachineInfo.getSrcIp();
                PortNumber upLinkPort = stateMachineInfo.getUpLinkPort();

                Ethernet eth = IgmpManager.outgoingIgmpWithV3() ?
                        IgmpSender.getInstance().buildIgmpV3Join(groupIp, srcIp) :
                        IgmpSender.getInstance().buildIgmpV2Join(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId, upLinkPort);
                int timeout = this.getTimeOut(IgmpManager.getUnsolicitedTimeout());
                stateMachineStore.startTimer(stateMachineInfo.getStateMachineId(), timeout);
            }
        }
    }
}
