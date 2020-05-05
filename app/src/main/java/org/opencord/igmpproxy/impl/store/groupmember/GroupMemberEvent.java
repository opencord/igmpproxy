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

import org.onosproject.event.AbstractEvent;

/**
 * Group-member event.
 */
public class GroupMemberEvent extends
        AbstractEvent<GroupMemberEvent.Type, GroupMember> {

    /**
     * Type of group-member event.
     */
    public enum Type {
        /**
         * Signifies that group-member added to store.
         */
        GROUP_MEMBER_ADDED,
        /**
         * Signifies that group-member updated in store.
         */
        GROUP_MEMBER_UPDATED,
        /**
         * Signifies that group-member has been removed from store.
         */
        GROUP_MEMBER_REMOVED
    }

    /**
     *
     * @param type group-member event type.
     * @param subject group-member.
     */
    public GroupMemberEvent(GroupMemberEvent.Type type, GroupMember subject) {
        super(type, subject);
    }

    protected GroupMemberEvent(GroupMemberEvent.Type type, GroupMember subject, long time) {
        super(type, subject, time);
    }
}
