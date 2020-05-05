package org.opencord.igmpproxy.statemachine;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

/**
 * Custom serializer for {@link StateMachineId}.
 */
public class StateMachineIdSerializer extends Serializer<StateMachineId> {
    /**
     * Creates serializer instance.
     */
    public StateMachineIdSerializer() {
        // non-null, immutable
        super(false, true);
    }

    @Override
    public void write(Kryo kryo, Output output, StateMachineId stateMachineId) {
        kryo.writeClassAndObject(output, stateMachineId.getDeviceId());
        kryo.writeClassAndObject(output, stateMachineId.getGroupIp());
    }

    @Override
    public StateMachineId read(Kryo kryo, Input input, Class<StateMachineId> aClass) {
        DeviceId deviceId = (DeviceId) kryo.readClassAndObject(input);
        Ip4Address groupIp = (Ip4Address) kryo.readClassAndObject(input);
        return StateMachineId.of(deviceId, groupIp);
    }
}