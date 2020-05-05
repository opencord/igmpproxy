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

import org.opencord.igmpproxy.GroupMemberId;

import java.util.Collection;
import java.util.Set;

/**
 * Group Member Store Service.
 */
public interface GroupMemberStore {

    /**
     * Add new group-member to store.
     *
     * @param groupMember given group-member.
     * @return added group-member.
     */
    GroupMember putGroupMember(GroupMember groupMember);

    /**
     * Update group-member in store.
     *
     * @param groupMember given group-member.
     * @return updated group-member.
     */
    GroupMember updateGroupMember(GroupMember groupMember);

    /**
     * removed group-member from store.
     *
     * @param groupMemberId given group-member id
     * @return removed group-member
     */
    GroupMember removeGroupMember(GroupMemberId groupMemberId);

    /**
     * get group-member from store.
     *
     * @param groupMemberId given group-member identification
     * @return group-member or null if not found
     */
    GroupMember getGroupMember(GroupMemberId groupMemberId);

    /**
     * Returns all group member ids.
     *
     * @return set of group member ids
     */
    Set<GroupMemberId> getAllGroupMemberIds();

    /**
     * Returns all group members.
     *
     * @return all group members
     */
    Collection<GroupMember> getAllGroupMembers();
}
