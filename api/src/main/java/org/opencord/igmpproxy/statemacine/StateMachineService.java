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

public interface StateMachineService {

    int TRANSITION_JOIN = 0;
    int TRANSITION_LEAVE = 1;
    int TRANSITION_QUERY = 2;
    int TRANSITION_TIMEOUT = 3;

    boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP, PortNumber upLinkPort);

    boolean leave(DeviceId devId, Ip4Address groupIp);

    void clearMap();

    void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp);

    void generalQuery(DeviceId devId, int maxResp);

    void generalQuery(int maxResp);

    void timeOut(DeviceId devId, Ip4Address groupIp);

    void timeOut1s();
}
