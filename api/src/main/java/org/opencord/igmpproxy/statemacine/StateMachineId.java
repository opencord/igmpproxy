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

import java.util.Objects;

public final class StateMachineId {
    private static final String GROUP = "Group";
    private DeviceId deviceId;
    private Ip4Address groupIp;

    private StateMachineId(DeviceId deviceId, Ip4Address groupIp) {
        this.deviceId = deviceId;
        this.groupIp = groupIp;
    }

    public static StateMachineId of(DeviceId deviceId, Ip4Address groupIp) {
        return new StateMachineId(deviceId, groupIp);
    }

    public String id() {
        return toString();
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public Ip4Address getGroupIp() {
        return groupIp;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public void setGroupIp(Ip4Address groupIp) {
        this.groupIp = groupIp;
    }

    @Override
    public String toString() {
        return deviceId.toString().concat(GROUP).concat(groupIp.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StateMachineId that = (StateMachineId) o;
        return Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(groupIp, that.groupIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, groupIp);
    }
}
