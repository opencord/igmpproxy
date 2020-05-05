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
import org.opencord.igmpproxy.timer.IgmpTimer;

public class InternalIgmpTimerEvent extends
        AbstractEvent<InternalIgmpTimerEvent.Type, IgmpTimer> {

    public enum Type {
        IGMP_TIMER_ADDED,
        IGMP_TIMER_UPDATED,
        IGMP_TIMER_REMOVED
    }

    public InternalIgmpTimerEvent(Type type, IgmpTimer subject) {
        super(type, subject);
    }

    protected InternalIgmpTimerEvent(Type type, IgmpTimer subject, long time) {
        super(type, subject, time);
    }
}
