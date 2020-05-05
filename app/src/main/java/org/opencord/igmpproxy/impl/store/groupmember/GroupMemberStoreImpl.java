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

import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.groupmember.GroupMember;
import org.opencord.igmpproxy.groupmember.GroupMemberId;
import org.opencord.igmpproxy.groupmember.GroupMemberStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Set;

import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED_DEFALT_VALUE;

@Component(
        service = GroupMemberStore.class,
        property = {
                DISTRIBUTED + ":Boolean=" + DISTRIBUTED_DEFALT_VALUE
        }
)
public class GroupMemberStoreImpl extends AbstractStore<InternalGroupMemberEvent, GroupMemberStoreDelegate>
        implements GroupMemberStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    private GroupMemberStore currentGroupMember;

    private DistributedGroupMemberStore distributedGroupMember;
    private SimpleGroupMemberStore simpleStateGroupMember;

    /**
     * Enable distributed state-machine store.
     */
    private boolean distributed;

    public GroupMemberStoreImpl() {
        super();
    }

    @Activate
    public void activate(ComponentContext context) {
        distributedGroupMember = new DistributedGroupMemberStore(storageService);
        distributedGroupMember.activate();

        simpleStateGroupMember = new SimpleGroupMemberStore();
        simpleStateGroupMember.activate();

        componentConfigService.registerProperties(getClass());
        modified(context);
        log.info("{} started.", this.getClass().getSimpleName());
    }

    @Deactivate
    public void deactivate() {
        simpleStateGroupMember.deactivate();
        distributedGroupMember.deactivate();

        componentConfigService.unregisterProperties(getClass(), false);
        log.info("{} stopped.", getClass().getSimpleName());
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();
        if (properties == null) {
            return;
        }

        String strDistributed = Tools.get(properties, "distributed");
        boolean expectDistributed = Boolean.parseBoolean(strDistributed);

        // Start state-machine store during first start or config change
        // NOTE: new state-machine store will be empty
        if (currentGroupMember == null || expectDistributed != distributed) {
            if (expectDistributed) {
                currentGroupMember = distributedGroupMember;
            } else {
                currentGroupMember = simpleStateGroupMember;
            }
            this.distributed = expectDistributed;
            log.info("Switched to {} state-machine store", distributed ? "distributed" : "local");
        }

    }

    @Override
    public void setDelegate(GroupMemberStoreDelegate delegate) {
        super.setDelegate(delegate);

        // Set the delegate of underlying state-machine store implementations
        simpleStateGroupMember.setDelegate(delegate);
        distributedGroupMember.setDelegate(delegate);
    }

    @Override
    public void unsetDelegate(GroupMemberStoreDelegate delegate) {
        super.unsetDelegate(delegate);

        // Unset the delegate of underlying state-machine store implementations
        simpleStateGroupMember.unsetDelegate(delegate);
        distributedGroupMember.unsetDelegate(delegate);
    }


    @Override
    public GroupMember putGroupMember(GroupMember groupMember) {
        return currentGroupMember.putGroupMember(groupMember);
    }

    @Override
    public GroupMember updateGroupMember(GroupMember groupMember) {
        return currentGroupMember.updateGroupMember(groupMember);
    }

    @Override
    public GroupMember removeGroupMember(GroupMemberId groupMemberId) {
        return currentGroupMember.removeGroupMember(groupMemberId);
    }

    @Override
    public GroupMember getGroupMember(GroupMemberId groupMemberId) {
        return currentGroupMember.getGroupMember(groupMemberId);
    }

    @Override
    public Set<GroupMemberId> keySet() {
        return currentGroupMember.keySet();
    }

    @Override
    public Collection<GroupMember> getAll() {
        return currentGroupMember.getAll();
    }
}
