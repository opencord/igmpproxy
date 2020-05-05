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
package org.opencord.igmpproxy.impl.store.groupmember;

import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.groupmember.GroupMember;
import org.opencord.igmpproxy.groupmember.GroupMemberId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedGroupMemberStore extends AbstractGroupMemberStore {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String GROUP_MEMBER_MAP_NAME = "onos-igmpproxy-groupmember-table";

    protected StorageService storageService;

    private ConsistentMap<GroupMemberId, GroupMember> consistentMap;

    public DistributedGroupMemberStore(StorageService storageService) {
        super();
        this.storageService = storageService;
    }

    public void activate() {
        KryoNamespace groupMemberSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(GroupMemberId.class)
                .register(GroupMember.class)
                .build();
        this.consistentMap = storageService.<GroupMemberId, GroupMember>consistentMapBuilder()
                .withName(GROUP_MEMBER_MAP_NAME)
                .withSerializer(Serializer.using(groupMemberSerializer))
                .build();
        super.groupMemberMap = consistentMap.asJavaMap();
        log.info("{} started.", getClass().getSimpleName());
    }

    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }
}
