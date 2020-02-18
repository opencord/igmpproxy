/*
 * Copyright 2018-present Open Networking Foundation
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

package org.opencord.igmpproxy;

import org.onosproject.event.AbstractEvent;


/**
 * Event indicating the Statistics Data of IGMP.
 */

public class IgmpStatisticsEvent extends
                           AbstractEvent<IgmpStatisticsEvent.Type, IgmpStatistics> {
    /**
     * Statistics data.
     * IgmpStatisticsEvent event type.
     */

    public enum Type {
        /**
         * signifies that the IGMP Statistics Event stats has been updated.
         */
        STATS_UPDATE
    }

    public IgmpStatisticsEvent(Type type, IgmpStatistics stats) {
        super(type, stats);
    }

}
