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

import org.onosproject.store.AbstractStore;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.timer.IgmpTimer;
import org.opencord.igmpproxy.timer.IgmpTimerStore;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIgmpTimerStore extends AbstractStore<InternalIgmpTimerEvent, IgmpTimerStoreDelegate>
        implements IgmpTimerStore {
    protected Map<Long, IgmpTimer> timerMap;

    protected AbstractIgmpTimerStore() {
    }

    protected AbstractIgmpTimerStore(Map<Long, IgmpTimer> timerMap) {
        this.timerMap = timerMap;
    }

    public abstract void activate();

    public abstract void deactivate();

    @Override
    public IgmpTimer createTimer(StateMachineId machineId, int timeout) {
        long timerId = getTimerId();
        IgmpTimer timer = new SingleTimer(timerId, timeout, machineId);
        timerMap.put(timerId, timer);
        return timer;
    }

    @Override
    public IgmpTimer updateTimer(IgmpTimer timer) {
        return timerMap.replace(timer.getId(), timer);
    }

    @Override
    public IgmpTimer destroyTimer(long timerId) {
        return timerMap.remove(timerId);
    }

    @Override
    public long resetTimer(long oldTimerId, StateMachineId stateMachineId, int timeOut) {
        destroyTimer(oldTimerId);
        return createTimer(stateMachineId, timeOut).getId();
    }

    @Override
    public Collection<IgmpTimer> getAll() {
        return timerMap.values();
    }

    @Override
    public Set<Map.Entry<Long, IgmpTimer>> entrySet() {
        return timerMap.entrySet();
    }
}
