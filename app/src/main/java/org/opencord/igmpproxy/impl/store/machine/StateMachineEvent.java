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

import org.onosproject.event.AbstractEvent;
import org.opencord.igmpproxy.statemachine.StateMachine;

/**
 * State machine event.
 */
public class StateMachineEvent extends
        AbstractEvent<StateMachineEvent.Type, StateMachine> {
    /**
     * Internal state-machine event type.
     */
    public enum Type {
        /**
         * Signifies that state-machine added to store.
         */
        STATE_MACHINE_ADDED,
        /**
         * Signifies that state-machine updated in store.
         */
        STATE_MACHINE_UPDATED,
        /**
         * Signifies that state-machine removed from store.
         */
        STATE_MACHINE_REMOVED
    }

    /**
     * Creates new state machine event.
     *
     * @param type    state-machine event type.
     * @param subject state machine.
     */
    public StateMachineEvent(Type type, StateMachine subject) {
        super(type, subject);
    }

    protected StateMachineEvent(Type type, StateMachine subject, long time) {
        super(type, subject, time);
    }
}
