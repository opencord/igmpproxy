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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

/**
 * Custom serializer for {@link GroupMemberId}.
 */
public class GroupMemberIdSerializer extends Serializer<GroupMemberId> {
    /**
     * Creates serializer instance.
     */
    public GroupMemberIdSerializer() {
        // non-null, immutable
        super(false, true);
    }

    @Override
    public void write(Kryo kryo, Output output, GroupMemberId groupMemberId) {
        output.writeString(groupMemberId.getDeviceId().toString());
        kryo.writeClassAndObject(output, groupMemberId.getGroupIp());
        kryo.writeClassAndObject(output, groupMemberId.getPortNum());
    }

    @Override
    public GroupMemberId read(Kryo kryo, Input input, Class<GroupMemberId> aClass) {
        DeviceId deviceId = DeviceId.deviceId(input.readString());
        Ip4Address groupIp = (Ip4Address) kryo.readClassAndObject(input);
        PortNumber portNum = (PortNumber) kryo.readClassAndObject(input);
        return GroupMemberId.of(groupIp, deviceId, portNum);
    }
}
