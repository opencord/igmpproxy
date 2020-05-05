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

import org.onosproject.event.AbstractEvent;

/**
 * Igmp timer event.
 */
public class InternalIgmpTimerEvent extends
        AbstractEvent<InternalIgmpTimerEvent.Type, IgmpTimer> {
    /**
     * Type of igmp-timer event.
     */
    public enum Type {
        /**
         * Signifies that igmp-timer added to store.
         */
        IGMP_TIMER_ADDED,
        /**
         * Signifies that igmp-timer updated in store.
         */
        IGMP_TIMER_UPDATED,
        /**
         * Signifies that igmp-timer removed from store.
         */
        IGMP_TIMER_REMOVED
    }

    /**
     * Creates new igmp-timer evet.
     *
     * @param type timer event type.
     * @param subject timer.
     */
    public InternalIgmpTimerEvent(Type type, IgmpTimer subject) {
        super(type, subject);
    }

    protected InternalIgmpTimerEvent(Type type, IgmpTimer subject, long time) {
        super(type, subject, time);
    }
}
