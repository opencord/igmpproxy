package org.opencord.igmpproxy.groupmember;

import java.util.Collection;
import java.util.Set;

public interface GroupMemberStore {

    GroupMember putGroupMember(GroupMember groupMember);

    GroupMember updateGroupMember(GroupMember groupMember);

    GroupMember removeGroupMember(GroupMemberId groupMemberId);

    GroupMember getGroupMember(GroupMemberId groupMemberId);

    Set<GroupMemberId> keySet();

    Collection<GroupMember> getAll();
}
