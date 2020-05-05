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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Informations of state machine.
 */
public class StateMachineInfo {
    public static final int DEFAULT_MAX_RESP = 0xfffffff;

    private StateMachineId stateMachineId;
    private int currentState;

    private Ip4Address srcIp;
    private PortNumber upLinkPort;

    private AtomicInteger timeOut;  // unit is 1 second

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
    public StateMachineInfo(DeviceId deviceId,
                            Ip4Address groupIp,
                            Ip4Address srcIp,
                            PortNumber upLinkPort) {
        this.stateMachineId = StateMachineId.of(deviceId, groupIp);
        this.srcIp = srcIp;
        this.upLinkPort = upLinkPort;
        this.currentState = StateConstants.STATE_NON;
        this.timeOut = null;
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

    /**
     * Returns timeout value of state-machine.
     *
     * @return if timeout is empty return null
     */
    public Integer getTimeOut() {
        return timeOut == null ? null : timeOut.get();
    }

    /**
     * Set timeout of state-machine.
     *
     * @param timeout current timeout value
     */
    public void setTimeOut(int timeout) {
        timeOut.set(timeout);
    }

    /**
     * Set timeout of state-machine.
     *
     * @param timeout current timeout
     */
    public void setTimeOut(Integer timeout) {
        if (timeout == null) {
            timeOut = null;
        } else {
            timeOut.set(timeout);
        }
    }

    /**
     * Create timeout of state-machine.
     *
     * @param timeout current timeout
     */
    public void createTimeOut(Integer timeout) {
        timeOut = new AtomicInteger(timeout);
    }

    /**
     * Increases timeout of timer.
     */
    public void increaseTimeOut() {
        this.timeOut.getAndIncrement();
    }

    /**
     * Decreases timeout of timer.
     */
    public void decreaseTimeOut() {
        this.timeOut.getAndDecrement();
    }
}
