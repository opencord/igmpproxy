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

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.IgmpLeadershipService;
import org.opencord.igmpproxy.statemachine.StateMachine;
import org.opencord.igmpproxy.statemachine.StateMachineId;
import org.opencord.igmpproxy.statemachine.StateMachineService;
import org.opencord.igmpproxy.impl.store.machine.StateMachineStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * State machine for whole IGMP process. The state machine is implemented on
 * RFC 2236 "6. Host State Diagram".
 */
@Component(immediate = true, service = StateMachineService.class)
public class StateMachineManager implements StateMachineService {
    // Only for tests purposes
    public static boolean sendQuery = true;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StateMachineStore stateMachineStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IgmpLeadershipService igmpLeadershipService;

    @Activate
    public void activate(ComponentContext context) {
        log.info("Started.");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        log.info("Stopped.");
    }

    private StateMachine get(DeviceId devId, Ip4Address groupIp) {
        StateMachineId id = StateMachineId.of(devId, groupIp);
        return stateMachineStore.getStateMachine(id);
    }

    public void destroySingle(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (machine == null) {
            log.debug("Machine has already been destroyed. deviceId: {} and groupIp: {} ", devId, groupIp);
            return;
        }
        stateMachineStore.removeStateMachine(machine.getStateMachineId());
        stateMachineStore.removeCounter(machine.getStateMachineId());
    }

    @Override
    public boolean join(DeviceId devId, Ip4Address groupIp, Ip4Address srcIP, PortNumber upLinkPort) {
        StateMachine machineInstance = get(devId, groupIp);

        if (machineInstance == null) {
            machineInstance = new SingleStateMachine(devId, groupIp, srcIP, upLinkPort);
            stateMachineStore.putStateMachine(machineInstance);
            stateMachineStore.increaseAndGetCounter(machineInstance.getStateMachineId());
            log.debug("Instance of machine created with id: {}", machineInstance.getStateMachineId());
            boolean shouldSendJoin = true;
            if (IgmpManager.isIgmpOnPodBasis() &&
                    groupListenedByOtherDevices(devId, groupIp)) {
                // unset the flag if igmp messages are evaluated on POD basis
                // and there are already active members of this group
                // across the entire POD
                shouldSendJoin = false;
            }
            machineInstance.join(shouldSendJoin);
            stateMachineStore.updateStateMachine(machineInstance);
            return true;
        }
        log.debug("Instance of machine has already been created. deviceId: {} and groupIp: {}",
                devId, groupIp);
        stateMachineStore.increaseAndGetCounter(machineInstance.getStateMachineId());
        return false;
    }

    @Override
    public boolean leave(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (machine == null) {
            log.debug("Machine has already been left and destroyed. deviceId: {} and groupIp: {} ", devId, groupIp);
            return false;
        }

        long count = stateMachineStore.decreaseAndGetCounter(machine.getStateMachineId());
        // make sure machine instance still exists.
        // it may be removed by the preceding thread
        if (count == 0) {
            boolean shouldSendLeave = true;
            if (IgmpManager.isIgmpOnPodBasis() &&
                    groupListenedByOtherDevices(devId, groupIp)) {
                // unset the flag if igmp messages are evaluated on POD basis
                // and there are still active members of this group
                // across the entire POD
                shouldSendLeave = false;
            }
            machine.leave(shouldSendLeave);
            destroySingle(devId, groupIp);
            log.debug("This machine left and destroyed. deviceId: {} and groupIp: {}", devId, groupIp);
            return true;
        }
        log.debug("This machine still has member/members. number of member/members: {}, deviceId: {}, groupIp: {} ",
                count, devId, groupIp);
        return false;
    }

    @Override
    public void specialQuery(DeviceId devId, Ip4Address groupIp, int maxResp) {
        StateMachine machine = get(devId, groupIp);
        if (machine == null) {
            log.debug("Machine is not found. deviceId: {} and groupIp: {} ", devId, groupIp);
            return;
        }
        machine.query(maxResp);
        stateMachineStore.updateStateMachine(machine);
    }

    @Override
    public void generalQuery(DeviceId devId, int maxResp) {
        for (StateMachine machine : stateMachineStore.getAllStateMachines()) {
            if (devId.equals(machine.getDeviceId())) {
                machine.query(maxResp);
                stateMachineStore.updateStateMachine(machine);
            }
        }
    }

    @Override
    public void generalQuery(int maxResp) {
        for (StateMachine machine : stateMachineStore.getAllStateMachines()) {
            machine.query(maxResp);
            stateMachineStore.updateStateMachine(machine);
        }
    }

    @Override
    public void timeOut(DeviceId devId, Ip4Address groupIp) {
        StateMachine machine = get(devId, groupIp);
        if (machine == null) {
            log.debug("Machine is not found. deviceId: {} and groupIp: {}", devId, groupIp);
            return;
        }
        machine.timeOut(sendQuery);
        stateMachineStore.updateStateMachine(machine);
    }

    @Override
    public void timeOut1s() {
        Collection<StateMachine> mapSet = stateMachineStore.getAllStateMachines();
        for (StateMachine machineInfo : mapSet) {
            if (machineInfo.getTimeOut() != null) {
                StateMachineId machineId = machineInfo.getStateMachineId();
                if (igmpLeadershipService.isLocalLeader(machineId.getDeviceId())) {
                    if (machineInfo.getTimeOut() > 0) {
                        stateMachineStore.decreaseTimeout(machineId);
                    } else {
                        StateMachine machine = stateMachineStore.getStateMachine(machineId);
                        machine.timeOut(sendQuery);
                        machine.destroyTimer();
                        stateMachineStore.updateStateMachine(machine);
                    }
                }
            }
        }
    }

    @Override
    public void clearAllMaps() {
        stateMachineStore.clearAllStateMachines();
    }

    /**
     * @param devId   id of the device being excluded
     * @param groupIp group IP address
     * @return true if this group has at least one listener connected to
     * any device in the map except for the device specified; false otherwise.
     */
    private boolean groupListenedByOtherDevices(DeviceId devId, Ip4Address groupIp) {
        for (StateMachine machine : stateMachineStore.getAllStateMachines()) {
            if (machine.getStateMachineId().getDeviceId().equals(devId)) {
                continue;
            }
            if (machine.getStateMachineId().getGroupIp().equals(groupIp)) {
                //means group is being listened by other peers in the domain
                return true;
            }
        }
        return false;
    }
}
