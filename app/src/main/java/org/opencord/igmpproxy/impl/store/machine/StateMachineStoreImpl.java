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
package org.opencord.igmpproxy.impl.store.machine;

import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.statemacine.StateMachineInfo;
import org.opencord.igmpproxy.statemacine.StateMachineStore;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED_DEFALT_VALUE;

@Component(
        service = StateMachineStore.class,
        property = {
                DISTRIBUTED + ":Boolean=" + DISTRIBUTED_DEFALT_VALUE
        }
)
public class StateMachineStoreImpl
        extends AbstractStore<InternalStateMachineEvent, StateMachineStoreDelegate> implements StateMachineStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    private StateMachineStore currentStateMachineStore;

    private DistributedStateMachineStore distributedStateMachineStore;
    private SimpleStateMachineStore simpleStateMachineStore;

    /**
     * Enable distributed state-machine store.
     */
    private boolean distributed;

    public StateMachineStoreImpl() {
        super();
    }

    @Activate
    public void activate(ComponentContext context) {
        distributedStateMachineStore = new DistributedStateMachineStore(storageService);
        distributedStateMachineStore.activate();

        simpleStateMachineStore = new SimpleStateMachineStore();
        simpleStateMachineStore.activate();

        componentConfigService.registerProperties(getClass());
        modified(context);
        log.info("{} started.", this.getClass().getSimpleName());
    }

    @Deactivate
    public void deactivate() {
        simpleStateMachineStore.deactivate();
        distributedStateMachineStore.deactivate();

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
        if (currentStateMachineStore == null || expectDistributed != distributed) {
            if (expectDistributed) {
                currentStateMachineStore = distributedStateMachineStore;
            } else {
                currentStateMachineStore = simpleStateMachineStore;
            }
            this.distributed = expectDistributed;
            log.info("Switched to {} state-machine store", distributed ? "distributed" : "local");
        }

    }

    @Override
    public void setDelegate(StateMachineStoreDelegate delegate) {
        super.setDelegate(delegate);

        // Set the delegate of underlying state-machine store implementations
        simpleStateMachineStore.setDelegate(delegate);
        distributedStateMachineStore.setDelegate(delegate);
    }

    @Override
    public void unsetDelegate(StateMachineStoreDelegate delegate) {
        super.unsetDelegate(delegate);

        // Unset the delegate of underlying state-machine store implementations
        simpleStateMachineStore.unsetDelegate(delegate);
        distributedStateMachineStore.unsetDelegate(delegate);
    }

    @Override
    public boolean increaseCounter(StateMachineId stateMachineId) {
        return currentStateMachineStore.increaseCounter(stateMachineId);
    }

    @Override
    public boolean decreaseCounter(StateMachineId stateMachineId) {
        return currentStateMachineStore.decreaseCounter(stateMachineId);
    }

    @Override
    public boolean removeCounter(StateMachineId stateMachineId) {
        return currentStateMachineStore.removeCounter(stateMachineId);
    }

    @Override
    public long getCounter(StateMachineId stateMachineId) {
        return currentStateMachineStore.getCounter(stateMachineId);
    }

    @Override
    public StateMachineInfo putStateMachine(StateMachineInfo stateMachine) {
        return currentStateMachineStore.putStateMachine(stateMachine);
    }

    @Override
    public StateMachineInfo updateStateMachine(StateMachineInfo machine) {
        return currentStateMachineStore.updateStateMachine(machine);
    }

    @Override
    public StateMachineInfo getStateMachine(StateMachineId id) {
        return currentStateMachineStore.getStateMachine(id);
    }

    @Override
    public StateMachineInfo removeStateMachine(StateMachineId id) {
        return currentStateMachineStore.removeStateMachine(id);
    }

    @Override
    public Collection<StateMachineInfo> getAll() {
        return currentStateMachineStore.getAll();
    }

    @Override
    public Set<Map.Entry<StateMachineId, StateMachineInfo>> entrySet() {
        return currentStateMachineStore.entrySet();
    }

    @Override
    public void clearMap() {
        currentStateMachineStore.clearMap();
    }
}
