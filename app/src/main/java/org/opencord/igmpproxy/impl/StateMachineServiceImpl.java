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
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.IgmpTimerConstants;
import org.opencord.igmpproxy.impl.store.machine.StateMachine;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.statemacine.StateMachineService;
import org.opencord.igmpproxy.statemacine.StateMachineStore;
import org.opencord.igmpproxy.statemacine.StateMachineInfo;
import org.opencord.igmpproxy.statemacine.StateConstants;
import org.opencord.igmpproxy.impl.store.timer.IgmpTimer;
import org.opencord.igmpproxy.impl.store.timer.IgmpTimerStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Random;
import java.util.Collection;

/**
 * State machine for whole IGMP process. The state machine is implemented on
 * RFC 2236 "6. Host State Diagram".
 */
@Component(immediate = true, service = StateMachineService.class)
public class StateMachineServiceImpl implements StateMachineService {
    // Only for tests purposes
    public static boolean sendQuery = true;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StateMachineStore stateMachineStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IgmpTimerStore igmpTimerStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected MastershipService mastershipService;

    @Activate
    public void activate(ComponentContext context) {
        log.info("{} started.", this.getClass().getSimpleName());
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        log.info("{} stopped.", getClass().getSimpleName());
    }

    private static StateMachineId getId(DeviceId devId, Ip4Address groupIp) {
        return StateMachineId.of(devId, groupIp);
    }

    private StateMachine get(DeviceId devId, Ip4Address groupIp) {
        StateMachineId id = StateMachineId.of(devId, groupIp);
        StateMachineInfo info = stateMachineStore.getStateMachine(id);
        if (info == null) {
            return null;
        } else {
            return new SingleStateMachine(info);
        }
    }

    private StateMachine get(StateMachineInfo info) {
        return new SingleStateMachine(info);
    }

    public void destroySingle(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        if (IgmpTimerConstants.INVALID_TIMER_ID != machine.getTimerId()) {
            igmpTimerStore.destroyTimer(machine.getTimerId());
        }
        stateMachineStore.removeStateMachine(machine.getStateMachineId());
        stateMachineStore.removeCounter(machine.getStateMachineId());
    }

    @Override
    public boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP, PortNumber upLinkPort) {
        StateMachine machine = get(devId, groupIp);

        if (null == machine) {
            machine = new SingleStateMachine(devId, groupIp, srcIP, upLinkPort);
            stateMachineStore.putStateMachine(machine.getInfo());
            stateMachineStore.increaseCounter(machine.getStateMachineId());
            boolean shouldSendJoin = true;
            if (IgmpManager.isIgmpOnPodBasis() &&
                    groupListenedByOtherDevices(devId, groupIp)) {
                // unset the flag if igmp messages are evaluated on POD basis
                // and there are already active members of this group
                // across the entire POD
                shouldSendJoin = false;
            }
            machine.join(shouldSendJoin);
            stateMachineStore.updateStateMachine(machine.getInfo());
            return true;
        }
        stateMachineStore.increaseCounter(machine.getStateMachineId());
        return false;
    }

    @Override
    public boolean leave(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return false;
        }

        stateMachineStore.decreaseCounter(machine.getStateMachineId());
        // make sure machine instance still exists.
        // it may be removed by the preceding thread
        if (stateMachineStore.getCounter(machine.getStateMachineId()) == 0) {
            boolean shouldSendLeave = true;
            if (IgmpManager.isIgmpOnPodBasis() &&
                    groupListenedByOtherDevices(devId, groupIp)) {
                // unset the flag if igmp messages are evaluated on POD basis
                // and there are still active members of this group
                // across the entire POD
                shouldSendLeave = false;
            }
            machine.leave(shouldSendLeave);
            destroySingle(devId, groupIp);
            return true;
        }
        return false;
    }

    @Override
    public void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp) {
        StateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.query(maxResp);
        stateMachineStore.updateStateMachine(machine.getInfo());
    }

    @Override
    public void generalQuery(DeviceId devId, int maxResp) {
        for (StateMachineInfo info : stateMachineStore.getAll()) {
            StateMachine machine = get(info);
            if (devId.equals(machine.getDeviceId())) {
                machine.query(maxResp);
                stateMachineStore.updateStateMachine(machine.getInfo());
            }
        }
    }

    @Override
    public void generalQuery(int maxResp) {
        for (StateMachineInfo info : stateMachineStore.getAll()) {
            StateMachine machine = get(info);
            machine.query(maxResp);
            stateMachineStore.updateStateMachine(machine.getInfo());
        }
    }

    @Override
    public void timeOut(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.timeOut(sendQuery);
        stateMachineStore.updateStateMachine(machine.getInfo());
    }

    @Override
    public void timeOut1s() {
        Collection<IgmpTimer> mapSet = igmpTimerStore.getAll();
        Iterator<IgmpTimer> itr = mapSet.iterator();
        while (itr.hasNext()) {
            IgmpTimer single = itr.next();
            StateMachineId machineId = single.getStateMachineId();
            if (mastershipService.isLocalMaster(machineId.getDeviceId())) {
                if (single.getTimeOut() > 0) {
                    single.decreaseTimeOut();
                    igmpTimerStore.updateTimer(single);
                } else {
                    StateMachineInfo info = stateMachineStore.getStateMachine(machineId);
                    StateMachine machine = get(info);

                    machine.timeOut(sendQuery);
                    stateMachineStore.updateStateMachine(machine.getInfo());

                    itr.remove();
                    igmpTimerStore.destroyTimer(single.getId());
                }
            }
        }
    }

    @Override
    public void clearMap() {
        stateMachineStore.clearMap();
    }

    /**
     * @param devId   id of the device being excluded
     * @param groupIp group IP address
     * @return true if this group has at least one listener connected to
     * any device in the map except for the device specified; false otherwise.
     */
    private boolean groupListenedByOtherDevices(DeviceId devId, Ip4Address groupIp) {
        for (StateMachineInfo machine : stateMachineStore.getAll()) {
            if (machine.getStateMachineId().getDeviceId().equals(devId)) {
                continue;
            }
            if (machine.getStateMachineId().getGroupIp().equals(groupIp)) {
                //means group is being listened by other peers in the domain
                return true;
            }
        }
        return false;
    }

    //Single State Machine implementation
    private class SingleStateMachine implements StateMachine {
        private StateMachineInfo stateMachineInfo;
        private AbstractState[] states = {
                new NonMember(),
                new DelayMember(),
                new IdleMember()
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

        public SingleStateMachine(DeviceId devId,
                                  Ip4Address groupIp,
                                  Ip4Address srcIp,
                                  PortNumber upLinkPort) {
            this.stateMachineInfo = new StateMachineInfo(devId, groupIp, srcIp, upLinkPort);
        }

        public SingleStateMachine(StateMachineInfo info) {
            this.stateMachineInfo = info;
        }

        @Override
        public StateMachineId getStateMachineId() {
            return this.stateMachineInfo.getStateMachineId();
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
        public void setTimerId(long timerId) {
            this.stateMachineInfo.setTimerId(timerId);
        }

        @Override
        public long getTimerId() {
            return this.stateMachineInfo.getTimerId();
        }

        @Override
        public void setTimeOut(int timeOut) {
            this.stateMachineInfo.setTimeOut(timeOut);
        }

        @Override
        public int getTimeOut() {
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
        public void next(int msg) {
            int currentState = this.stateMachineInfo.getCurrentState();
            this.stateMachineInfo.setCurrentState(transition[currentState][msg]);
        }

        @Override
        public void join(boolean messageOutAllowed) {
            int currentState = this.stateMachineInfo.getCurrentState();
            AbstractState state = states[currentState];
            state.join(messageOutAllowed);
            next(StateConstants.TRANSITION_JOIN);
        }

        @Override
        public void leave(boolean messageOutAllowed) {
            int currentState = this.stateMachineInfo.getCurrentState();
            AbstractState state = states[currentState];
            state.leave(messageOutAllowed);
            next(StateConstants.TRANSITION_LEAVE);
        }

        @Override
        public void query(int maxResp) {
            int currentState = this.stateMachineInfo.getCurrentState();
            AbstractState state = states[currentState];
            state.query(maxResp);
            next(StateConstants.TRANSITION_QUERY);
        }

        @Override
        public void timeOut(boolean sendQuery) {
            int currentState = this.stateMachineInfo.getCurrentState();
            AbstractState state = states[currentState];
            state.timeOut(sendQuery);
            next(StateConstants.TRANSITION_TIMEOUT);
        }

        private abstract class AbstractState {
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
            @Override
            public void query(int maxResp) {

                if (maxResp < stateMachineInfo.getTimeOut()) {
                    stateMachineInfo.setTimeOut(this.getTimeOut(maxResp));
                    long timerId = stateMachineInfo.getTimerId();
                    stateMachineInfo.setTimerId(igmpTimerStore.resetTimer(timerId,
                            stateMachineInfo.getStateMachineId(), stateMachineInfo.getTimeOut()));
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
                    setTimeOut(StateMachineInfo.DEFAULT_MAX_RESP);
                }
            }
        }

        private class IdleMember extends AbstractState {
            @Override
            public void query(int maxResp) {
                int timeOut = this.getTimeOut(maxResp);
                setTimeOut(timeOut);
                IgmpTimer timer = igmpTimerStore.createTimer(stateMachineInfo.getStateMachineId(), timeOut);
                setTimerId(timer.getId());
            }
        }

        private class NonMember extends AbstractState {
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
                    int timeOut = this.getTimeOut(IgmpManager.getUnsolicitedTimeout());
                    setTimeOut(timeOut);
                    IgmpTimer newTimer = igmpTimerStore.createTimer(stateMachineInfo.getStateMachineId(), timeOut);
                    setTimerId(newTimer.getId());
                }
            }
        }
    }
}
