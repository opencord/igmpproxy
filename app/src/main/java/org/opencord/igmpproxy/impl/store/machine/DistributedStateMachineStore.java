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

import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.AtomicCounterMap;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.impl.SingleStateMachine;
import org.opencord.igmpproxy.impl.SingleStateMachineSerializer;
import org.opencord.igmpproxy.statemachine.StateMachine;
import org.opencord.igmpproxy.statemachine.StateMachineId;
import org.opencord.igmpproxy.statemachine.StateMachineIdSerializer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State machine store based on distributed storage.
 */
@Component(service = StateMachineStore.class)
public class DistributedStateMachineStore extends AbstractStateMachineStore {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String STATE_MACHINE_COUNTER_STORE = "onos-state-machine-counter-store";
    private static final String STATE_MACHINE_MAP_NAME = "onos-state-machine-store";

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    private AtomicCounterMap<StateMachineId> stateMachineCounters;

    private ConsistentMap<StateMachineId, StateMachine> consistentMap;

    public DistributedStateMachineStore() {
        super();
    }

    @Activate
    public void activate() {
        KryoNamespace.Builder stateMachineKryoBuilder = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(new StateMachineIdSerializer(), StateMachineId.class);

        stateMachineCounters = storageService.<StateMachineId>atomicCounterMapBuilder()
                .withName(STATE_MACHINE_COUNTER_STORE)
                .withSerializer(Serializer.using(stateMachineKryoBuilder.build())).build();

        stateMachineKryoBuilder
                .register(new SingleStateMachineSerializer(), SingleStateMachine.class);
        this.consistentMap = storageService.<StateMachineId, StateMachine>consistentMapBuilder()
                .withName(STATE_MACHINE_MAP_NAME)
                .withSerializer(Serializer.using(stateMachineKryoBuilder.build()))
                .build();
        super.stateMachineMap = consistentMap.asJavaMap();


        log.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        stateMachineMap.clear();
        stateMachineMap = null;
        consistentMap.destroy();
        stateMachineCounters.clear();
        stateMachineCounters.destroy();
        log.info("Stopped.");
    }

    @Override
    public long increaseAndGetCounter(StateMachineId stateMachineId) {
        return stateMachineCounters.incrementAndGet(stateMachineId);
    }

    @Override
    public long decreaseAndGetCounter(StateMachineId stateMachineId) {
        if (stateMachineCounters.get(stateMachineId) > 0) {
            return stateMachineCounters.decrementAndGet(stateMachineId);
        } else {
            return stateMachineCounters.get(stateMachineId);
        }
    }

    @Override
    public long getCounter(StateMachineId stateMachineId) {
        return stateMachineCounters.get(stateMachineId);
    }

    @Override
    public boolean removeCounter(StateMachineId stateMachineId) {
        stateMachineCounters.remove(stateMachineId);
        return true;
    }

    @Override
    public void clearAllStateMachines() {
        super.clearAllStateMachines();
        stateMachineCounters.clear();
    }
}
