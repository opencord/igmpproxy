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
package org.opencord.igmpproxy.statemacine;

import java.util.Collection;

/**
 * State Machine Store.
 */
public interface StateMachineStore {
    /**
     * Increases counter state-machine.
     *
     * @param stateMachineId identification of machine
     * @return true
     */
    boolean increaseCounter(StateMachineId stateMachineId);

    /**
     * Decreases counter of state-machine.
     *
     * @param stateMachineId identification of machine
     * @return if counter greater than zero, decreases counter and return true
     */
    boolean decreaseCounter(StateMachineId stateMachineId);

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
     * Add new information of machine to store.
     *
     * @param stateMachine given machine
     * @return added informations
     */
    StateMachineInfo putStateMachineInformations(StateMachineInfo stateMachine);

    /**
     * Update information in store.
     *
     * @param stateMachine given info
     * @return updated info
     */
    StateMachineInfo updateStateMachineInformations(StateMachineInfo stateMachine);

    /**
     * Get information from store.
     *
     * @param id given identification of machine
     * @return information of machine
     */
    StateMachineInfo getStateMachineInformations(StateMachineId id);

    /**
     * Remove machine from store.
     *
     * @param id given identification of machine
     * @return removed info
     */
    StateMachineInfo removeStateMachineInfo(StateMachineId id);

    /**
     * Get informations of all machine.
     *
     * @return machine informations
     */
    Collection<StateMachineInfo> getAllStateMachineInformations();

    /**
     * clear information map.
     */
    void clearAllStateMachineInformations();

    /**
     * Start timer.
     *
     * @param machineId given machine id
     * @param timeout   timeout
     */
    void startTimer(StateMachineId machineId, int timeout);

    /**
     * Resets timer.
     *
     * @param stateMachineId identification of machine
     * @param timeout        timeout of timer
     */
    void resetTimer(StateMachineId stateMachineId, int timeout);

    /**
     * Destroy timeout.
     *
     * @param stateMachineId identification of machine
     */
    void destroyTimer(StateMachineId stateMachineId);

    /**
     * Set timer to max timeout.
     *
     * @param machineId given machine id
     */
    void setMaxTimeout(StateMachineId machineId);

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
