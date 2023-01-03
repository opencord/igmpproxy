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

package org.opencord.igmpproxy;

import com.google.common.base.MoreObjects;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.Objects;

/**
 * Identification of group member.
 */
public final class GroupMemberId {
    private Ip4Address groupIp;
    private DeviceId deviceId;
    private PortNumber portNum;

    /**
     * Constructor for serializer.
     */
    private GroupMemberId() {
        this.groupIp = null;
        this.deviceId = null;
        this.portNum = null;
    }

    private GroupMemberId(Ip4Address groupIp, DeviceId deviceId, PortNumber portNum) {
        this.groupIp = groupIp;
        this.deviceId = deviceId;
        this.portNum = portNum;
    }

    /**
     * Creates new group member identification.
     *
     * @param groupIp  group ip of member
     * @param deviceId device id
     * @param portNum  port number of connect point
     * @return new identification of group-member
     */
    public static GroupMemberId of(Ip4Address groupIp, DeviceId deviceId, PortNumber portNum) {
        return new GroupMemberId(groupIp, deviceId, portNum);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupIp", groupIp)
                .add("deviceId", deviceId)
                .add("portNumber", portNum).toString();
    }

    /**
     * Get group ip of group.
     *
     * @return group ip
     */
    public Ip4Address getGroupIp() {
        return groupIp;
    }

    /**
     * Get device id of connect point.
     *
     * @return device id
     */
    public DeviceId getDeviceId() {
        return deviceId;
    }

    /**
     * Get port number of connect point.
     *
     * @return port number
     */
    public PortNumber getPortNum() {
        return portNum;
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

