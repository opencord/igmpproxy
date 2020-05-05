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

public interface StateMachine {
    StateMachineId getStateMachineId();

    Ip4Address getGroupIp();

    DeviceId getDeviceId();

    void setTimerId(long timerId);

    long getTimerId();

    void setTimeOut(int timeOut);

    int getTimeOut();

    StateMachineInfo getInfo();

    int currentState();

    void next(int msg);

    void join(boolean messageOutAllowed);

    void leave(boolean messageOutAllowed);

    void query(int maxResp);

    void timeOut(boolean sendQuery);

}
