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

import org.opencord.igmpproxy.statemacine.StateMachineId;

import java.util.Collection;

/**
 * Igmp Timer Store Service.
 */
public interface IgmpTimerStore {

    /**
     * Creates new igmp timer using machine id and timeout.
     *
     * @param machineId identification of machine
     * @param timeout   timeout of machine
     * @return created timer
     */
    IgmpTimer createTimer(StateMachineId machineId, int timeout);

    /**
     * Updates timer.
     *
     * @param timer given timer
     * @return updated timer
     */
    IgmpTimer updateTimer(IgmpTimer timer);

    /**
     * Destroy timer using identification of timer.
     *
     * @param timerId identification of timer
     * @return destroyed timer
     */
    IgmpTimer destroyTimer(long timerId);

    /**
     * Resets timer.
     *
     * @param oldTimerId     identification previous timer
     * @param stateMachineId identification of machine
     * @param timeout        timeout of timer
     * @return identification of new timer
     */
    long resetTimer(long oldTimerId, StateMachineId stateMachineId, int timeout);

    /**
     * Get all of timer.
     *
     * @return timers
     */
    Collection<IgmpTimer> getAll();
}
