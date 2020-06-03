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

import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.cluster.Leadership;
import org.onosproject.cluster.NodeId;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.opencord.igmpproxy.IgmpLeadershipService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leadership service implementation.
 */
@Component(immediate = true, service = IgmpLeadershipService.class)
public class IgmpLeadershipManager implements IgmpLeadershipService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LeadershipService leadershipService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ClusterService clusterService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected MastershipService mastershipService;

    @Activate
    public void activate(ComponentContext context) {
        log.info("Started.");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        log.info("Stopped.");
    }

    @Override
    public boolean isLocalLeader(DeviceId deviceId) {
        if (!mastershipService.isLocalMaster(deviceId)) {
            if (deviceService.isAvailable(deviceId)) {
                return false;
            }
            NodeId leader = leadershipService.runForLeadership(
                    deviceId.toString()).leaderNodeId();
            return clusterService.getLocalNode().id().equals(leader);
        }
        return true;
    }

    @Override
    public NodeId getLocalNodeId() {
        return clusterService.getLocalNode().id();
    }

    @Override
    public NodeId getLeader(String topic) {
        return leadershipService.getLeader(topic);
    }

    @Override
    public Leadership runForLeadership(String topic) {
        return leadershipService.runForLeadership(topic);
    }

    @Override
    public void withdraw(String topic) {
        leadershipService.withdraw(topic);
    }
}
