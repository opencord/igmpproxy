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
package org.opencord.igmpproxy.statemachine;

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

/**
 * Manage State Machine and Igmp Timer.
 */
public interface StateMachineService {

    /**
     * Makes the requirements of igmp-join request.
     *
     * @param devId      device id of connect point
     * @param groupIp    group ip of igmp group
     * @param srcIP      source ip of device
     * @param upLinkPort uplink port of device
     * @return if this is first join from the device and group-ip return true
     */
    boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP, PortNumber upLinkPort);

    /**
     * Makes the requirements of igmp-leave request.
     *
     * @param devId   device id of connect point
     * @param groupIp group ip of group-member
     * @return If there are no more members of that device and group returns true
     */
    boolean leave(DeviceId devId, Ip4Address groupIp);

    /**
     * clear map of state-machine.
     */
    void clearAllMaps();

    /**
     * Makes the requirements of special query.
     *
     * @param devId   device id
     * @param groupIp group ip of igmp group
     * @param maxResp maximum resp of igmp
     */
    void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp);

    /**
     * Makes the requirements of igmp-query request.
     *
     * @param devId   device id
     * @param maxResp maximum resp of igmp
     */
    void generalQuery(DeviceId devId, int maxResp);

    /**
     * Makes the requirements of igmp-query request.
     *
     * @param maxResp maximum resp of igmp
     */
    void generalQuery(int maxResp);

    /**
     * Makes the requirements of timeout.
     *
     * @param devId   device id
     * @param groupIp group ip of igmp group
     */
    void timeOut(DeviceId devId, Ip4Address groupIp);

    /**
     * Checks all state-machines periodically.
     */
    void timeOut1s();
}
