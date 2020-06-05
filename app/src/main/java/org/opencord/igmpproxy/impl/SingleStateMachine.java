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

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.impl.state.DelayMember;
import org.opencord.igmpproxy.impl.state.IdleMember;
import org.opencord.igmpproxy.impl.state.NonMember;
import org.opencord.igmpproxy.statemachine.StateMachine;
import org.opencord.igmpproxy.statemachine.State;
import org.opencord.igmpproxy.statemachine.StateMachineId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * State machine implementation.
 */
public final class SingleStateMachine implements StateMachine {
    public static final int DEFAULT_MAX_RESP = 0xfffffff;

    private Logger log = LoggerFactory.getLogger(getClass());

    private StateMachineId stateMachineId;
    private int currentState;

    private Ip4Address srcIp;
    private PortNumber upLinkPort;

    private AtomicInteger timeOut;  // unit is 1 second
    private State[] states;
    private int[] nonTransition =
            {State.STATE_DELAY, State.STATE_NON,
                    State.STATE_NON, State.STATE_NON};
    private int[] delayTransition =
            {State.STATE_DELAY, State.STATE_NON,
                    State.STATE_DELAY, State.STATE_IDLE};
    private int[] idleTransition =
            {State.STATE_IDLE, State.STATE_NON,
                    State.STATE_DELAY, State.STATE_IDLE};
    //THE TRANSITION TABLE
    private int[][] transition =
            {nonTransition, delayTransition, idleTransition};

    /**
     * Constructor for serializer.
     */
    private SingleStateMachine() {
        this.stateMachineId = null;
        this.srcIp = null;
        this.upLinkPort = null;
        this.timeOut = null;
        this.states = null;
    }

    /**
     * Constructor of single state machine.
     *
     * @param deviceId   device id of state-machine
     * @param groupIp    group id of state-machine
     * @param srcIp      source ip of state-machine
     * @param upLinkPort uplink port of state-machine
     */
    public SingleStateMachine(DeviceId deviceId,
                              Ip4Address groupIp,
                              Ip4Address srcIp,
                              PortNumber upLinkPort) {
        this.stateMachineId = StateMachineId.of(deviceId, groupIp);
        this.srcIp = srcIp;
        this.upLinkPort = upLinkPort;
        this.currentState = State.STATE_NON;
        this.timeOut = null;
        this.states = new State[]{new NonMember(this), new DelayMember(this), new IdleMember(this)};
    }

    /**
     * Constructor of single state machine.
     *
     * @param machineId id of state-machine
     * @param srcIp source ip of state-machine
     * @param upLinkPort uplink port of state-machine
     * @param currentState current state of state-machine
     * @param timeout timeout value of state-machine
     */
    public SingleStateMachine(StateMachineId machineId,
                              Ip4Address srcIp,
                              PortNumber upLinkPort,
                              int currentState,
                              Integer timeout) {
        this.stateMachineId = machineId;
        this.srcIp = srcIp;
        this.upLinkPort = upLinkPort;
        this.currentState = currentState;
        if (timeout != null) {
            createTimeOut(timeout);
        } else {
            this.timeOut = null;
        }
        this.states = new State[]{new NonMember(this), new DelayMember(this), new IdleMember(this)};
    }

    @Override
    public StateMachineId getStateMachineId() {
        return this.stateMachineId;
    }

    @Override
    public Ip4Address getGroupIp() {
        return this.stateMachineId.getGroupIp();
    }

    @Override
    public DeviceId getDeviceId() {
        return this.stateMachineId.getDeviceId();
    }


    @Override
    public Ip4Address getSrcIp() {
        return srcIp;
    }

    public PortNumber getUpLinkPort() {
        return upLinkPort;
    }

    @Override
    public int currentState() {
        return this.currentState;
    }

    public Integer getTimeOut() {
        return timeOut == null ? null : timeOut.get();
    }

    @Override
    public void increaseTimeOut() {
        this.timeOut.getAndIncrement();
    }

    @Override
    public void decreaseTimeOut() {
        this.timeOut.getAndDecrement();
    }

    @Override
    public void setMaxTimeout() {
        this.timeOut.set(DEFAULT_MAX_RESP);
    }

    @Override
    public void startTimer(int timeout) {
        createTimeOut(timeout);
    }

    @Override
    public void resetTimer(int timeout) {
        setTimeOut(timeout);
    }

    @Override
    public void destroyTimer() {
        setTimeOut(null);
    }

    @Override
    public void join(boolean messageOutAllowed) {
        if (messageOutAllowed) {
            State state = states[currentState];
            state.join();
        }
        next(State.TRANSITION_JOIN);
    }

    @Override
    public void leave(boolean messageOutAllowed) {
        if (messageOutAllowed) {
            State state = states[currentState];
            state.leave();
        }
        next(State.TRANSITION_LEAVE);
    }

    @Override
    public void query(int maxResp) {
        State state = states[currentState];
        state.query(maxResp);
        next(State.TRANSITION_QUERY);
    }

    @Override
    public void timeOut(boolean sendQuery) {
        if (sendQuery) {
            State state = states[currentState];
            state.timeOut();
        }
        next(State.TRANSITION_TIMEOUT);
    }

    private void next(int msg) {
        log.debug("Transitioning to State {}", msg);
        this.currentState = transition[currentState][msg];
    }

    private void setTimeOut(int timeout) {
        timeOut.set(timeout);
    }

    private void setTimeOut(Integer timeout) {
        if (timeout == null) {
            timeOut = null;
        } else {
            timeOut.set(timeout);
        }
    }

    private void createTimeOut(Integer timeout) {
        timeOut = new AtomicInteger(timeout);
    }
}
