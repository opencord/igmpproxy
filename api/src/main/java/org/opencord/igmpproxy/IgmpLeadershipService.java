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
package org.opencord.igmpproxy;

import org.onosproject.cluster.Leadership;
import org.onosproject.cluster.NodeId;
import org.onosproject.net.DeviceId;

/**
 * Leadership control service.
 */
public interface IgmpLeadershipService {
    /**
     * Makes leadership control.
     *
     * @param deviceId received deviceId
     * @return if it is leadership of this device, return true
     */
    boolean isLocalLeader(DeviceId deviceId);

    /**
     * Gets local node id.
     *
     * @return node id
     */
    NodeId getLocalNodeId();

    /**
     * Gets leader for topic.
     *
     * @param topic topic name
     * @return leader of topic
     */
    NodeId getLeader(String topic);

    /**
     * Enters a leadership contest.
     *
     * @param topic leadership topic
     * @return {@code Leadership} future
     */
    Leadership runForLeadership(String topic);

    /**
     * Withdraws from a leadership contest.
     *
     * @param topic leadership topic
     */
    void withdraw(String topic);
}
