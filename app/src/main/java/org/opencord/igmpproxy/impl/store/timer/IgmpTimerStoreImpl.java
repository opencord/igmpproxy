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

package org.opencord.igmpproxy.impl.store.timer;

import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.timer.IgmpTimer;
import org.opencord.igmpproxy.timer.IgmpTimerStore;
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
import java.util.Map;
import java.util.Set;

import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.DISTRIBUTED_DEFALT_VALUE;

@Component(
        service = IgmpTimerStore.class,
        property = {
                DISTRIBUTED + ":Boolean=" + DISTRIBUTED_DEFALT_VALUE
        }
)
public class IgmpTimerStoreImpl extends AbstractStore<InternalIgmpTimerEvent, IgmpTimerStoreDelegate>
        implements IgmpTimerStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    private IgmpTimerStore currentTimerStore;

    private DistributedIgmpTimerStore distributedIgmpTimerStore;
    private SimpleIgmpTimerStore simpleIgmpTimerStore;

    /**
     * Enable distributed state-machine store.
     */
    private boolean distributed;

    public IgmpTimerStoreImpl() {
        super();
    }

    @Activate
    public void activate(ComponentContext context) {
        distributedIgmpTimerStore = new DistributedIgmpTimerStore(storageService);
        distributedIgmpTimerStore.activate();

        simpleIgmpTimerStore = new SimpleIgmpTimerStore();
        simpleIgmpTimerStore.activate();

        componentConfigService.registerProperties(getClass());
        modified(context);
        log.info("{} started.", this.getClass().getSimpleName());
    }

    @Deactivate
    public void deactivate() {
        simpleIgmpTimerStore.deactivate();
        distributedIgmpTimerStore.deactivate();

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
        if (currentTimerStore == null || expectDistributed != distributed) {
            if (expectDistributed) {
                currentTimerStore = distributedIgmpTimerStore;
            } else {
                currentTimerStore = simpleIgmpTimerStore;
            }
            this.distributed = expectDistributed;
            log.info("Switched to {} state-machine store", distributed ? "distributed" : "local");
        }

    }

    @Override
    public void setDelegate(IgmpTimerStoreDelegate delegate) {
        super.setDelegate(delegate);

        // Set the delegate of underlying state-machine store implementations
        simpleIgmpTimerStore.setDelegate(delegate);
        distributedIgmpTimerStore.setDelegate(delegate);
    }

    @Override
    public void unsetDelegate(IgmpTimerStoreDelegate delegate) {
        super.unsetDelegate(delegate);

        // Unset the delegate of underlying state-machine store implementations
        simpleIgmpTimerStore.unsetDelegate(delegate);
        distributedIgmpTimerStore.unsetDelegate(delegate);
    }


    @Override
    public IgmpTimer createTimer(StateMachineId machineId, int timeout) {
        return currentTimerStore.createTimer(machineId, timeout);
    }

    @Override
    public IgmpTimer updateTimer(IgmpTimer timer) {
        return currentTimerStore.updateTimer(timer);
    }

    @Override
    public IgmpTimer destroyTimer(long timerId) {
        return currentTimerStore.destroyTimer(timerId);
    }

    @Override
    public long resetTimer(long oldTimerId, StateMachineId stateMachineId, int timeout) {
        return currentTimerStore.resetTimer(oldTimerId, stateMachineId, timeout);
    }

    @Override
    public long getTimerId() {
        return currentTimerStore.getTimerId();
    }

    @Override
    public Collection<IgmpTimer> getAll() {
        return currentTimerStore.getAll();
    }

    @Override
    public Set<Map.Entry<Long, IgmpTimer>> entrySet() {
        return currentTimerStore.entrySet();
    }
}
