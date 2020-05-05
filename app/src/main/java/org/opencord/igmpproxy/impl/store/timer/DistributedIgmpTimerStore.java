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

import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.AtomicCounter;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.timer.IgmpTimer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedIgmpTimerStore extends AbstractIgmpTimerStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String IGMP_TIMER_MAP_NAME = "onos-igmp-timer-store";
    private static final String IGMP_TIMER_ID_NAME = "igmp-timer-id-counter";

    private AtomicCounter atomicTimerId;

    private ConsistentMap<Long, IgmpTimer> consistentTimerMap;

    public StorageService storageService;

    public DistributedIgmpTimerStore(StorageService storageService) {
        super();
        this.storageService = storageService;
    }

    @Activate
    @Override
    public void activate() {
        KryoNamespace masterRouteTableSerializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(KryoNamespaces.BASIC)
                .register(StateMachineId.class)
                .register(IgmpTimer.class)
                .register(SingleTimer.class)
                .build();
        this.consistentTimerMap = storageService.<Long, IgmpTimer>consistentMapBuilder()
                .withName(IGMP_TIMER_MAP_NAME)
                .withSerializer(Serializer.using(masterRouteTableSerializer))
                .build();
        atomicTimerId = storageService.getAtomicCounter(IGMP_TIMER_ID_NAME);
        atomicTimerId.set(INVALID_TIMER_ID + 1L);
        this.timerMap = consistentTimerMap.asJavaMap();
        log.info("{} started.", getClass().getSimpleName());
    }

    @Deactivate
    @Override
    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }

    @Override
    public long getTimerId() {
        return atomicTimerId.getAndIncrement();
    }
}
