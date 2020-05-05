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
     * Sets information of machine.
     *
     * @param info information of machine
     */
    void setStateMachineInfo(StateMachineInfo info);

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
     * Returns timeout.
     *
     * @return timeout
     */
    Integer getTimeOut();

    /**
     * Returns information of state-machine.
     *
     * @return information of state-machine
     */
    StateMachineInfo getInfo();

    /**
     * Returns current state.
     *
     * @return current state
     */
    int currentState();

    /**
     * @param messageOutAllowed message out
     */
    void join(boolean messageOutAllowed);

    /**
     * @param messageOutAllowed message out
     */
    void leave(boolean messageOutAllowed);

    /**
     * @param maxResp maximum resp
     */
    void query(int maxResp);

    /**
     * @param sendQuery send query for test
     */
    void timeOut(boolean sendQuery);

}
