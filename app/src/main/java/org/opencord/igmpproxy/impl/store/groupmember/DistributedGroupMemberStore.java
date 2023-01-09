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
package org.opencord.igmpproxy.impl.store.groupmember;

import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.GroupMemberId;
import org.opencord.igmpproxy.GroupMemberIdSerializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group member store based on distributed storage.
 */
@Component(service = GroupMemberStore.class)
public class DistributedGroupMemberStore extends AbstractGroupMemberStore {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String GROUP_MEMBER_MAP_NAME = "onos-igmpproxy-groupmember-table";

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    private ConsistentMap<GroupMemberId, GroupMember> consistentMap;

    public DistributedGroupMemberStore() {
        super();
    }

    @Activate
    public void activate() {
        KryoNamespace groupMemberSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(new GroupMemberIdSerializer(), GroupMemberId.class)
                .register(GroupMember.class)
                .build();
        consistentMap = storageService.<GroupMemberId, GroupMember>consistentMapBuilder()
                .withName(GROUP_MEMBER_MAP_NAME)
                .withSerializer(Serializer.using(groupMemberSerializer))
                .build();
        groupMemberMap = consistentMap.asJavaMap();
        log.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped.");
    }
}
