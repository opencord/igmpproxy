package org.opencord.igmpproxy.groupmember;

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.Objects;

public class GroupMemberId {
    private Ip4Address groupIp;
    private DeviceId deviceId;
    private PortNumber portNum;

    public GroupMemberId(Ip4Address groupIp, DeviceId deviceId, PortNumber portNum) {
        this.groupIp = groupIp;
        this.deviceId = deviceId;
        this.portNum = portNum;
    }

    public static GroupMemberId of(Ip4Address groupIp, DeviceId deviceId, PortNumber portNum) {
        return new GroupMemberId(groupIp, deviceId, portNum);
    }

    @Override
    public String toString() {
        return groupIp.toString() + deviceId.toString() + portNum.toString();
    }

    public Ip4Address getGroupIp() {
        return groupIp;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public PortNumber getPortNum() {
        return portNum;
    }

    public void setGroupIp(Ip4Address groupIp) {
        this.groupIp = groupIp;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public void setPortNum(PortNumber portNum) {
        this.portNum = portNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupMemberId that = (GroupMemberId) o;
        return Objects.equals(groupIp, that.groupIp) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(portNum, that.portNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupIp, deviceId, portNum);
    }
}

