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
        kryo.writeClassAndObject(output, groupMemberId.getDeviceId());
        kryo.writeClassAndObject(output, groupMemberId.getGroupIp());
        kryo.writeClassAndObject(output, groupMemberId.getPortNum());
    }

    @Override
    public GroupMemberId read(Kryo kryo, Input input, Class<GroupMemberId> aClass) {
        DeviceId deviceId = (DeviceId) kryo.readClassAndObject(input);
        Ip4Address groupIp = (Ip4Address) kryo.readClassAndObject(input);
        PortNumber portNum = (PortNumber) kryo.readClassAndObject(input);
        return GroupMemberId.of(groupIp, deviceId, portNum);
    }
}
