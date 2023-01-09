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
package org.opencord.igmpproxy.impl.state;

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.opencord.igmpproxy.impl.IgmpManager;
import org.opencord.igmpproxy.impl.IgmpSender;
import org.opencord.igmpproxy.statemachine.StateMachine;

/**
 * Implementation of non-member state.
 */
public class NonMember extends AbstractState {
    public NonMember(StateMachine machine) {
        super(machine);
    }

    @Override
    public void join() {
        DeviceId devId = machine.getStateMachineId().getDeviceId();
        Ip4Address groupIp = machine.getStateMachineId().getGroupIp();

        Ethernet eth = IgmpManager.outgoingIgmpWithV3() ?
                IgmpSender.getInstance().buildIgmpV3Join(groupIp, machine.getSrcIp()) :
                IgmpSender.getInstance().buildIgmpV2Join(groupIp, machine.getSrcIp());
        IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId, machine.getUpLinkPort());
        int timeout = getTimeOut(IgmpManager.getUnsolicitedTimeout());
        machine.startTimer(timeout);
    }
}