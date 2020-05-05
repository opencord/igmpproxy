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
    public StateMachineInfo putStateMachineInformations(StateMachineInfo stateMachine) {
        return stateMachineMap.put(stateMachine.getStateMachineId(), stateMachine);
    }

    @Override
    public StateMachineInfo updateStateMachineInformations(StateMachineInfo machine) {
        return stateMachineMap.replace(machine.getStateMachineId(), machine);
    }

    @Override
    public StateMachineInfo removeStateMachineInfo(StateMachineId id) {
        return stateMachineMap.remove(id);
    }

    @Override
    public StateMachineInfo getStateMachineInformations(StateMachineId id) {
        return stateMachineMap.get(id);
    }

    @Override
    public Collection<StateMachineInfo> getAllStateMachineInformations() {
        return ImmutableList.copyOf(stateMachineMap.values());
    }

    @Override
    public void clearAllStateMachineInformations() {
        stateMachineMap.clear();
        stateMachineMap = null;
    }

    @Override
    public void startTimer(StateMachineId stateMachineId, int timeout) {
        createTimeout(stateMachineId, timeout);
    }

    @Override
    public void resetTimer(StateMachineId stateMachineId, int timeout) {
        updateTimeout(stateMachineId, timeout);
    }


    @Override
    public void destroyTimer(StateMachineId stateMachineId) {
        updateTimeout(stateMachineId, null);
    }

    @Override
    public void setMaxTimeout(StateMachineId machineId) {
        StateMachineInfo machine = stateMachineMap.get(machineId);
        machine.setTimeOut(StateMachineInfo.DEFAULT_MAX_RESP);
        updateStateMachineInformations(machine);
    }

    @Override
    public void decreaseTimeout(StateMachineId machineId) {
        StateMachineInfo machine = stateMachineMap.get(machineId);
        machine.decreaseTimeOut();
        updateStateMachineInformations(machine);
    }

    @Override
    public void increaseTimeout(StateMachineId machineId) {
        StateMachineInfo machine = stateMachineMap.get(machineId);
        machine.increaseTimeOut();
        updateStateMachineInformations(machine);
    }

    protected void updateTimeout(StateMachineId stateMachineId, Integer timeout) {
        StateMachineInfo machine = stateMachineMap.get(stateMachineId);
        machine.setTimeOut(timeout);
        updateStateMachineInformations(machine);
    }

    protected void createTimeout(StateMachineId stateMachineId, Integer timeout) {
        StateMachineInfo machine = stateMachineMap.get(stateMachineId);
        machine.createTimeOut(timeout);
        updateStateMachineInformations(machine);
    }
}
