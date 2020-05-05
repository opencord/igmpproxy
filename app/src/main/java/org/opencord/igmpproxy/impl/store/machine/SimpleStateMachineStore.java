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

import com.google.common.collect.Maps;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.statemacine.StateMachineInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleStateMachineStore extends AbstractStateMachineStore {
    private static final int DEFAULT_COUNT = 0;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<StateMachineId, AtomicLong> countsMap;

    public SimpleStateMachineStore() {
        super();
    }

    public void activate() {
        stateMachineMap = Maps.newConcurrentMap();
        countsMap = Maps.newConcurrentMap();
        log.info("{} started.", getClass().getSimpleName());
    }

    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }

    @Override
    public boolean increaseCounter(StateMachineId stateMachineId) {
        AtomicLong count = countsMap.get(stateMachineId);
        if (count == null) {
            count = new AtomicLong(DEFAULT_COUNT);
            countsMap.put(stateMachineId, count);
        }
        count.incrementAndGet();
        return true;
    }

    @Override
    public boolean decreaseCounter(StateMachineId stateMachineId) {
        AtomicLong count = countsMap.get(stateMachineId);
        if (count.get() > 0) {
            count.decrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeCounter(StateMachineId stateMachineId) {
        countsMap.remove(stateMachineId);
        return true;
    }

    @Override
    public long getCounter(StateMachineId stateMachineId) {
        return countsMap.get(stateMachineId).get();
    }


    @Override
    public StateMachineInfo putStateMachine(StateMachineInfo stateMachine) {
        StateMachineInfo created = super.putStateMachine(stateMachine);
        notifyDelegate(new InternalStateMachineEvent(
                InternalStateMachineEvent.Type.STATE_MACHINE_ADDED, created));
        return created;
    }

    @Override
    public StateMachineInfo updateStateMachine(StateMachineInfo stateMachine) {
        StateMachineInfo updated = super.updateStateMachine(stateMachine);
        notifyDelegate(new InternalStateMachineEvent(
                InternalStateMachineEvent.Type.STATE_MACHINE_UPDATED, updated));
        return updated;
    }

    @Override
    public StateMachineInfo removeStateMachine(StateMachineId id) {
        StateMachineInfo removed = super.removeStateMachine(id);
        notifyDelegate(new InternalStateMachineEvent(
                InternalStateMachineEvent.Type.STATE_MACHINE_REMOVED, removed));
        return removed;
    }
}
