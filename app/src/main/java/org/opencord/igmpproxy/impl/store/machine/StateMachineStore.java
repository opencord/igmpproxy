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

import org.opencord.igmpproxy.statemachine.StateMachine;
import org.opencord.igmpproxy.statemachine.StateMachineId;

import java.util.Collection;

/**
 * State Machine Store.
 */
public interface StateMachineStore {
    /**
     * Increases counter state-machine.
     *
     * @param stateMachineId identification of machine
     * @return counter
     */
    long increaseAndGetCounter(StateMachineId stateMachineId);

    /**
     * Decreases counter of state-machine.
     *
     * @param stateMachineId identification of machine
     * @return if counter greater than zero, decreases counter and return counter
     */
    long decreaseAndGetCounter(StateMachineId stateMachineId);

    /**
     * Removes counter from counter-map.
     *
     * @param stateMachineId identification of machine
     * @return true
     */
    boolean removeCounter(StateMachineId stateMachineId);

    /**
     * Get counter using state-machine id.
     *
     * @param stateMachineId identification of machine
     * @return counter of member that use this id
     */
    long getCounter(StateMachineId stateMachineId);

    /**
     * Add new machine to store.
     *
     * @param stateMachine given machine
     * @return added informations
     */
    StateMachine putStateMachine(StateMachine stateMachine);

    /**
     * Update information in store.
     *
     * @param stateMachine given machine
     * @return updated info
     */
    StateMachine updateStateMachine(StateMachine stateMachine);

    /**
     * Get information from store.
     *
     * @param id given identification of machine
     * @return information of machine
     */
    StateMachine getStateMachine(StateMachineId id);

    /**
     * Remove machine from store.
     *
     * @param id given identification of machine
     * @return removed info
     */
    StateMachine removeStateMachine(StateMachineId id);

    /**
     * Get informations of all machine.
     *
     * @return machine informations
     */
    Collection<StateMachine> getAllStateMachines();

    /**
     * clear information map.
     */
    void clearAllStateMachines();

    /**
     * Decreases timeout of timer.
     *
     * @param machineId given machine id
     */
    void decreaseTimeout(StateMachineId machineId);

    /**
     * Increases timeout of timer.
     *
     * @param machineId given machine id
     */
    void increaseTimeout(StateMachineId machineId);
}
