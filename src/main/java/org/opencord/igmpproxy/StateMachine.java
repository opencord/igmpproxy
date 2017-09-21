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
package org.opencord.igmpproxy;

import com.google.common.collect.Maps;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import java.util.Map;
import java.util.Set;

/**
 * State machine for whole IGMP process. The state machine is implemented on
 * RFC 2236 "6. Host State Diagram".
 */
public final class StateMachine {
    private StateMachine() {

    }
    private static Map<String, SingleStateMachine> map = Maps.newConcurrentMap();

    private static String getId(DeviceId devId, Ip4Address groupIp) {
        return devId.toString() + "Group" + groupIp.toString();
    }

    private static SingleStateMachine get(DeviceId devId, Ip4Address groupIp) {
        String id = getId(devId, groupIp);
        return map.get(id);
    }

    public static void destorySingle(DeviceId devId, Ip4Address groupIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.cancelTimer();
        map.remove(getId(devId, groupIp));
    }

    public static boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            machine = new SingleStateMachine(devId, groupIp, srcIP);
            map.put(getId(devId, groupIp), machine);
            machine.join();
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

        if (machine.getCounter() == 0) {
            machine.leave();
            destorySingle(devId, groupIp);
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

}
