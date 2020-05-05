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

/**
 * Timer of igmp.
 */
public interface IgmpTimer {
    /**
     * Get identification of timer.
     *
     * @return identification
     */
    long getId();

    /**
     * Get timeout of timer.
     *
     * @return timeout
     */
    int getTimeOut();

    /**
     * Increases timeout of timer.
     */
    void increaseTimeOut();

    /**
     * Decreases timeout of timer.
     */
    void decreaseTimeOut();

    /**
     * Get identification of machine that use this timer.
     *
     * @return machine id
     */
    StateMachineId getStateMachineId();
}
