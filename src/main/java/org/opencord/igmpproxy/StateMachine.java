package org.opencord.igmpproxy;

import com.google.common.collect.Maps;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.Map;
import java.util.Set;

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
        machine.count++;
        return false;
    }

    public static boolean leave(DeviceId devId, Ip4Address groupIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return false;
        }
        machine.count--;
        if (machine.count == 0) {
            machine.leave();
            destorySingle(devId, groupIp);
            return true;
        }
        return false;
    }

    static void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp, Ip4Address queryIp) {
        SingleStateMachine machine = get(devId, groupIp);
        if (null == machine) {
            return;
        }
        machine.query(maxResp, queryIp);
    }

    static void generalQuery(DeviceId devId, int maxResp, Ip4Address queryIp) {
        for (Map.Entry<String, SingleStateMachine> entry : map.entrySet()) {
            SingleStateMachine machine = entry.getValue();
            if (devId.equals(machine.devId)) {
                machine.query(maxResp, queryIp);
            }
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
