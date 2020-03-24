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

import com.google.common.collect.Maps;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.Map;
import java.util.Set;

/**
 * State machine for whole IGMP process. The state machine is implemented on
 * RFC 2236 "6. Host State Diagram".
 */
public final class StateMachine {

    private static final String GROUP = "Group";

    private StateMachine() {

    }
    private static Map<String, SingleStateMachine> map = Maps.newConcurrentMap();

    private static String getId(DeviceId devId, Ip4Address groupIp) {
        return devId.toString() + GROUP + groupIp.toString();
    }

    private static SingleStateMachine get(DeviceId devId, Ip4Address groupIp) {
        String id = getId(devId, groupIp);
        return map.get(id);
    }

    public static void destroySingle(DeviceId devId, Ip4Address groupIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.cancelTimer();
        map.remove(getId(devId, groupIp));
    }

    public static boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP, PortNumber upLinkPort) {
        SingleStateMachine machine = get(devId, groupIp);

        if (null == machine) {
            machine = new SingleStateMachine(devId, groupIp, srcIP, upLinkPort);
            map.put(getId(devId, groupIp), machine);

            boolean shouldSendJoin = true;
            if (IgmpManager.isIgmpOnPodBasis() &&
                    groupListenedByOtherDevices(devId, groupIp)) {
                // unset the flag if igmp messages are evaluated on POD basis
                // and there are already active members of this group
                // across the entire POD
                shouldSendJoin = false;
            }
            machine.join(shouldSendJoin);
            return true;
        }
        machine.increaseCounter();
        return false;
    }

    public static boolean leave(DeviceId devId, Ip4Address groupIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return false;
        }

        machine.decreaseCounter();
        // make sure machine instance still exists.
        // it may be removed by the preceding thread
        if (machine.getCounter() == 0) {
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

    static void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.query(maxResp);
    }

    static void generalQuery(DeviceId devId, int maxResp) {
        for (Map.Entry<String, SingleStateMachine> entry : map.entrySet()) {
            SingleStateMachine machine = entry.getValue();
            if (devId.equals(machine.getDeviceId())) {
                machine.query(maxResp);
            }
        }
    }

    static void generalQuery(int maxResp) {
        for (Map.Entry<String, SingleStateMachine> entry : map.entrySet()) {
            SingleStateMachine machine = entry.getValue();
            machine.query(maxResp);
        }
    }

    public static Set<Map.Entry<String, SingleStateMachine>> entrySet() {
        return map.entrySet();
    }

    public static void timeOut(DeviceId devId, Ip4Address groupIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.timeOut();
    }

    public static void clearMap() {
        map.clear();
    }

    /**
     * @param devId   id of the device being excluded
     * @param groupIp group IP address
     * @return true if this group has at least one listener connected to
     * any device in the map except for the device specified; false otherwise.
     */
    private static boolean groupListenedByOtherDevices(DeviceId devId, Ip4Address groupIp) {
        for (SingleStateMachine machine : map.values()) {
            if (machine.getDeviceId().equals(devId)) {
                continue;
            }
            if (machine.getGroupIp().equals(groupIp)) {
                //means group is being listened by other peers in the domain
                return true;
            }
        }
        return false;
    }

}
