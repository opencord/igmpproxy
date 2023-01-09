/*
 * Copyright 2017-2023 Open Networking Foundation (ONF) and the ONF Contributors
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
package org.opencord.igmpproxy.statemachine;

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

/**
 * State machine object.
 */
public interface StateMachine {
    /**
     * Returns identification of state-machine.
     *
     * @return identification
     */
    StateMachineId getStateMachineId();

    /**
     * Returns group ip of state-machine.
     *
     * @return ip of group
     */
    Ip4Address getGroupIp();

    /**
     * Returns device id of state-machine.
     *
     * @return device id
     */
    DeviceId getDeviceId();

    /**
     * Returns source ip of state-machine.
     *
     * @return source ip
     */
    Ip4Address getSrcIp();

    /**
     * Returns up-link port of state-machine.
     *
     * @return up-link port
     */
    PortNumber getUpLinkPort();

    /**
     * Returns timeout. it is nullable.
     *
     * @return timeout
     */
    Integer getTimeOut();

    /**
     * Set timer to max timeout.
     */
    void setMaxTimeout();

    /**
     * Start timer.
     *
     * @param timeout timeout
     */
    void startTimer(int timeout);

    /**
     * Resets timer.
     *
     * @param timeout timeout of timer
     */
    void resetTimer(int timeout);

    /**
     * Destroy timeout.
     */
    void destroyTimer();

    /**
     * Increases timeout of timer.
     */
    void increaseTimeOut();

    /**
     * Decreases timeout of timer.
     */
    void decreaseTimeOut();

    /**
     * Returns current state.
     *
     * @return current state
     */
    int currentState();

    /**
     * Makes the requirements of join request and transition to next station.
     *
     * @param messageOutAllowed message out allowed
     */
    void join(boolean messageOutAllowed);

    /**
     * Makes the requirements of leave request and transition to next station.
     *
     * @param messageOutAllowed message out allowed
     */
    void leave(boolean messageOutAllowed);

    /**
     * Makes the requirements of query request and transition to next station.
     *
     * @param maxResp maximum resp
     */
    void query(int maxResp);

    /**
     * Makes the requirements of timeout request and transition to next station.
     *
     * @param sendQuery send query for test
     */
    void timeOut(boolean sendQuery);
}
