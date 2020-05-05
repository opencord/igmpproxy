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
package org.opencord.igmpproxy.statemacine;

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.IgmpTimerConstants;

/**
 * Informations of state machine.
 */
public class StateMachineInfo {
    public static final int DEFAULT_MAX_RESP = 0xfffffff;

    private StateMachineId stateMachineId;
    private int currentState;

    private long timerId;
    private Ip4Address srcIp;
    private PortNumber upLinkPort;
    private int timeOut;

    private StateMachineInfo() {
    }

    /**
     * Creates new state-machine information.
     *
     * @param deviceId   device identifier
     * @param groupIp    group ip
     * @param srcIp      source ip
     * @param upLinkPort uplink port
     */
    public StateMachineInfo(DeviceId deviceId, Ip4Address groupIp, Ip4Address srcIp, PortNumber upLinkPort) {
        this.stateMachineId = StateMachineId.of(deviceId, groupIp);
        this.srcIp = srcIp;
        this.upLinkPort = upLinkPort;
        this.currentState = StateConstants.STATE_NON;
        this.timerId = IgmpTimerConstants.INVALID_TIMER_ID;
        this.timeOut = DEFAULT_MAX_RESP;
    }

    /**
     * Returns current state of state-machine.
     *
     * @return current state number
     */
    public int getCurrentState() {
        return currentState;
    }

    /**
     * Updates state of state-machine.
     *
     * @param newState new state
     */
    public void setCurrentState(int newState) {
        this.currentState = newState;
    }

    /**
     * Returns timer identification of state-machine.
     *
     * @return timer identification
     */
    public long getTimerId() {
        return timerId;
    }

    /**
     * Updates timer identification of state-machine.
     *
     * @param newTimerId new timer identification
     */
    public void setTimerId(long newTimerId) {
        this.timerId = newTimerId;
    }

    /**
     * Returns timeout value of state-machine.
     *
     * @return timeout value
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Updates timeout value of state-machine.
     *
     * @param timeOut new timeout
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Returns identification of state-machine.
     *
     * @return state-machine id
     */
    public StateMachineId getStateMachineId() {
        return stateMachineId;
    }

    /**
     * Returns source ip of state-machine.
     *
     * @return source ip
     */
    public Ip4Address getSrcIp() {
        return srcIp;
    }

    /**
     * Returns up-link port of state-machine.
     *
     * @return up-link port
     */
    public PortNumber getUpLinkPort() {
        return upLinkPort;
    }
}
