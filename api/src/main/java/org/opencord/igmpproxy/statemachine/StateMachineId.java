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
package org.opencord.igmpproxy.statemachine;

import com.google.common.base.MoreObjects;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.Objects;

/**
 * Identification of state machine.
 */
public final class StateMachineId {
    private DeviceId deviceId;
    private Ip4Address groupIp;

    /**
     * Constructor for serializer.
     */
    private StateMachineId() {
        this.deviceId = null;
        this.groupIp = null;
    }

    private StateMachineId(DeviceId deviceId, Ip4Address groupIp) {
        this.deviceId = deviceId;
        this.groupIp = groupIp;
    }

    /**
     * Creates new state-machine identification using device-id and group-ip.
     *
     * @param deviceId device id of state-machie
     * @param groupIp  group ip of state-machine
     * @return created identification
     */
    public static StateMachineId of(DeviceId deviceId, Ip4Address groupIp) {
        return new StateMachineId(deviceId, groupIp);
    }

    /**
     * Returns device id of identification.
     *
     * @return device id
     */
    public DeviceId getDeviceId() {
        return deviceId;
    }

    /**
     * Returns group ip of identification.
     *
     * @return group ip
     */
    public Ip4Address getGroupIp() {
        return groupIp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceId", deviceId)
                .add("groupIp", groupIp)
                .toString();
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
