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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Records Peak metrics for IgmpProxy application.
 *
 */
public class IgmpPeakStatistics {

    //Per Second Stats
    private AtomicLong peakMsgCount = new AtomicLong();
    private AtomicLong peakMsgDuration = new AtomicLong();
    private AtomicLong peakDisconnectCount = new AtomicLong();
    private AtomicLong peakDisconnectDuration = new AtomicLong();
    private AtomicLong peakConnectionCount = new AtomicLong();
    private AtomicLong peakConnectionDuration = new AtomicLong();

    // Map to store the peak value
    private Map<Instant, AtomicLong> peakMsgCountMap = new HashMap<Instant, AtomicLong>();
    private Map<Instant, AtomicLong> peakMsgCountDurationMap = new HashMap<Instant, AtomicLong>();
    private Map<String, Map<Instant, AtomicLong>> peakStatsMap = new HashMap<String, Map<Instant, AtomicLong>>();

    public Map<Instant, AtomicLong> getPeakMsgCountDurationMap() {
        return peakMsgCountDurationMap;
    }

    public Map<String, Map<Instant, AtomicLong>> getPeakStatsMap() {
        return peakStatsMap;
    }

    public Map<Instant, AtomicLong> getPeakMsgCountMap() {
        return peakMsgCountMap;
    }

    public AtomicLong getPeakMsgCount() {
        return peakMsgCount;
    }

    public void increasePeakMsgCount() {
        peakMsgCount.incrementAndGet();
    }

    public AtomicLong getPeakMsgDuration() {
        return peakMsgDuration;
    }

    public void increasePeakMsgDuration() {
        peakMsgDuration.incrementAndGet();
    }

    public AtomicLong getPeakDisconnectCount() {
        return peakDisconnectCount;
    }

    public void increasePeakDisconnectCount() {
        peakDisconnectCount.incrementAndGet();
    }

    public AtomicLong getPeakDisconnectDuration() {
        return peakDisconnectDuration;
    }

    public void increasePeakDisconnectDuration() {
        peakDisconnectDuration.incrementAndGet();
    }

    public AtomicLong getPeakConnectionCount() {
        return peakConnectionCount;
    }

    public void increasePeakConnectionCount() {
        peakConnectionCount.incrementAndGet();
    }

    public AtomicLong getPeakConnectionDuration() {
        return peakConnectionDuration;
    }

    public void increasePeakConnectionDuration() {
        peakConnectionDuration.incrementAndGet();
    }
}
