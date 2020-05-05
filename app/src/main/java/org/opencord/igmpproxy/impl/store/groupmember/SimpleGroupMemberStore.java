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

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group member store based on in-memory storage.
 */
public class SimpleGroupMemberStore extends AbstractGroupMemberStore {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public SimpleGroupMemberStore() {
        super();
    }

    /**
     * Sets up in-memory map.
     */
    public void activate() {
        groupMemberMap = Maps.newConcurrentMap();
        log.info("{} started.", getClass().getSimpleName());
    }

    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }

    @Override
    public GroupMember putGroupMember(GroupMember groupMember) {
        GroupMember created = super.putGroupMember(groupMember);
        notifyDelegate(new InternalGroupMemberEvent(InternalGroupMemberEvent.Type.GROUP_MEMBER_ADDED, created));
        return created;
    }

    @Override
    public GroupMember updateGroupMember(GroupMember groupMember) {
        GroupMember updated = super.updateGroupMember(groupMember);
        notifyDelegate(new InternalGroupMemberEvent(InternalGroupMemberEvent.Type.GROUP_MEMBER_UPDATED, updated));
        return updated;
    }

    @Override
    public GroupMember removeGroupMember(GroupMemberId groupMemberId) {
        GroupMember removed = super.removeGroupMember(groupMemberId);
        notifyDelegate(new InternalGroupMemberEvent(InternalGroupMemberEvent.Type.GROUP_MEMBER_REMOVED, removed));
        return removed;
    }
}
