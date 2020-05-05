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

import com.google.common.collect.ImmutableList;
import org.onosproject.store.AbstractStore;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.statemacine.StateMachineInfo;
import org.opencord.igmpproxy.statemacine.StateMachineStore;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractStateMachineStore
        extends AbstractStore<InternalStateMachineEvent, StateMachineStoreDelegate>
        implements StateMachineStore {

    protected Map<StateMachineId, StateMachineInfo> stateMachineMap;

    protected AbstractStateMachineStore() {
    }

    protected AbstractStateMachineStore(Map<StateMachineId, StateMachineInfo> stateMachineMap) {
        this.stateMachineMap = stateMachineMap;
    }

    @Override
    public StateMachineInfo putStateMachine(StateMachineInfo stateMachine) {
        return stateMachineMap.put(stateMachine.getStateMachineId(), stateMachine);
    }

    @Override
    public StateMachineInfo updateStateMachine(StateMachineInfo machine) {
        return stateMachineMap.replace(machine.getStateMachineId(), machine);
    }

    @Override
    public StateMachineInfo removeStateMachine(StateMachineId id) {
        return stateMachineMap.remove(id);
    }

    @Override
    public StateMachineInfo getStateMachine(StateMachineId id) {
        return stateMachineMap.get(id);
    }

    @Override
    public Collection<StateMachineInfo> getAll() {
        return ImmutableList.copyOf(stateMachineMap.values());
    }

    @Override
    public void clearMap() {
        stateMachineMap.clear();
        stateMachineMap = null;
    }
}
