/*
 * Copyright 2017-2023 Open Networking Foundation (ONF) and the ONF Contributors
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
import org.opencord.igmpproxy.statemachine.StateMachine;
import org.opencord.igmpproxy.statemachine.StateMachineId;

import java.util.Collection;
import java.util.Map;

/**
 * Abstract implementation of state-machine store.
 */
public abstract class AbstractStateMachineStore
        extends AbstractStore<StateMachineEvent, StateMachineStoreDelegate>
        implements StateMachineStore {

    protected Map<StateMachineId, StateMachine> stateMachineMap;

    protected AbstractStateMachineStore() {
    }

    protected AbstractStateMachineStore(Map<StateMachineId, StateMachine> stateMachineMap) {
        this.stateMachineMap = stateMachineMap;
    }

    @Override
    public StateMachine putStateMachine(StateMachine stateMachine) {
        return stateMachineMap.put(stateMachine.getStateMachineId(), stateMachine);
    }

    @Override
    public StateMachine updateStateMachine(StateMachine machine) {
        return stateMachineMap.replace(machine.getStateMachineId(), machine);
    }

    @Override
    public StateMachine removeStateMachine(StateMachineId id) {
        return stateMachineMap.remove(id);
    }

    @Override
    public StateMachine getStateMachine(StateMachineId id) {
        return stateMachineMap.get(id);
    }

    @Override
    public Collection<StateMachine> getAllStateMachines() {
        return ImmutableList.copyOf(stateMachineMap.values());
    }

    @Override
    public void clearAllStateMachines() {
        stateMachineMap.clear();
        stateMachineMap = null;
    }

    @Override
    public void decreaseTimeout(StateMachineId machineId) {
        StateMachine machine = stateMachineMap.get(machineId);
        machine.decreaseTimeOut();
        updateStateMachine(machine);
    }

    @Override
    public void increaseTimeout(StateMachineId machineId) {
        StateMachine machine = stateMachineMap.get(machineId);
        machine.increaseTimeOut();
        updateStateMachine(machine);
    }

}
