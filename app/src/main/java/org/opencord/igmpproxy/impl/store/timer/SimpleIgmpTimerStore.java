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

import com.google.common.collect.Maps;
import org.opencord.igmpproxy.IgmpTimerConstants;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Igmp-timer store based on in-memory storage.
 */
public class SimpleIgmpTimerStore extends AbstractIgmpTimerStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AtomicLong atomicTimerId;

    @Override
    public void activate() {
        this.timerMap = Maps.newConcurrentMap();
        atomicTimerId = new AtomicLong(IgmpTimerConstants.INVALID_TIMER_ID + 1L);
        log.info("{} started.", getClass().getSimpleName());
    }

    @Override
    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }


    @Override
    public IgmpTimer createTimer(StateMachineId machineId, int timeout) {
        IgmpTimer igmpTimer = super.createTimer(machineId, timeout);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_ADDED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public IgmpTimer updateTimer(IgmpTimer timer) {
        IgmpTimer igmpTimer = super.updateTimer(timer);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_UPDATED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public IgmpTimer destroyTimer(long timerId) {
        IgmpTimer igmpTimer = super.destroyTimer(timerId);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_REMOVED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public long getTimerId() {
        return atomicTimerId.getAndIncrement();
    }

}
