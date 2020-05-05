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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.statemachine.StateMachineId;

/**
 * Custom serializer for {@link SingleStateMachine}.
 */
public class SingleStateMachineSerializer extends Serializer<SingleStateMachine> {
    /**
     * Creates serializer instance.
     */
    public SingleStateMachineSerializer() {
        // non-null, immutable
        super(false, true);
    }

    @Override
    public void write(Kryo kryo, Output output, SingleStateMachine singleStateMachine) {
        kryo.writeClassAndObject(output, singleStateMachine.getStateMachineId());
        kryo.writeClassAndObject(output, singleStateMachine.getSrcIp());
        kryo.writeClassAndObject(output, singleStateMachine.getUpLinkPort());
        kryo.writeClassAndObject(output, singleStateMachine.getTimeOut());
        kryo.writeObject(output, singleStateMachine.currentState());
    }

    @Override
    public SingleStateMachine read(Kryo kryo, Input input, Class<SingleStateMachine> aClass) {
        StateMachineId stateMachineId = (StateMachineId) kryo.readClassAndObject(input);
        Ip4Address srcIp = (Ip4Address) kryo.readClassAndObject(input);
        PortNumber uplinkPort = (PortNumber) kryo.readClassAndObject(input);
        Integer timeout = (Integer) kryo.readClassAndObject(input);
        int currentState = kryo.readObject(input, int.class);
        return new SingleStateMachine(stateMachineId, srcIp, uplinkPort, currentState, timeout);
    }

}
