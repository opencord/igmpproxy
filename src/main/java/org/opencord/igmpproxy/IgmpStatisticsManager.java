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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.event.AbstractListenerManager;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.google.common.base.Strings;

import static org.opencord.igmpproxy.OsgiPropertyConstants.STATISTICS_GENERATION_PERIOD;
import static org.opencord.igmpproxy.OsgiPropertyConstants.STATISTICS_GENERATION_PERIOD_DEFAULT;


/**
 *
 * Process the stats collected in Igmp proxy application. Publish to kafka onos.
 *
 */
@Component(immediate = true, property = {
        STATISTICS_GENERATION_PERIOD + ":Integer=" + STATISTICS_GENERATION_PERIOD_DEFAULT,
})
public class IgmpStatisticsManager extends
                 AbstractListenerManager<IgmpStatisticsEvent, IgmpStatisticsEventListener>
                         implements IgmpStatisticsService {
    private IgmpStatisticsDelegate statsDelegate;
    private final Logger log = getLogger(getClass());
    private IgmpStatistics igmpStats;

    protected IgmpStatisticsEventPublisher igmpStatisticsPublisher;
    ScheduledFuture<?> scheduledFuture;
    ScheduledExecutorService executorForIgmp;

    private int statisticsGenerationPeriodInSeconds = STATISTICS_GENERATION_PERIOD_DEFAULT;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Override
    public IgmpStatistics getIgmpStats() {
        return igmpStats;
    }

    @Activate
    public void activate() {
        igmpStats = new IgmpStatistics();
        statsDelegate = new InternalIgmpDelegateForStatistics();
        eventDispatcher.addSink(IgmpStatisticsEvent.class, listenerRegistry);
        executorForIgmp = Executors.newScheduledThreadPool(1);
        igmpStatisticsPublisher = new IgmpStatisticsEventPublisher();
        cfgService.registerProperties(getClass());
        try {
            scheduledFuture = executorForIgmp.scheduleAtFixedRate(igmpStatisticsPublisher,
                0, statisticsGenerationPeriodInSeconds, TimeUnit.SECONDS);
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            log.error("Caught Exception while scheduling executorForIgmp during activate");
        }
        log.info("IgmpStatisticsManager Activated");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<String, Object> properties = context.getProperties();
        String s = Tools.get(properties, STATISTICS_GENERATION_PERIOD);
        statisticsGenerationPeriodInSeconds = Strings.isNullOrEmpty(s) ?
                Integer.parseInt(STATISTICS_GENERATION_PERIOD)
                    : Integer.parseInt(s.trim());
        scheduledFuture.cancel(true);
        try {
            scheduledFuture = executorForIgmp.scheduleAtFixedRate(igmpStatisticsPublisher,
                0, statisticsGenerationPeriodInSeconds, TimeUnit.SECONDS);
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            log.error("Caught Exception while scheduling executorForIgmp during modify");
        }
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(IgmpStatisticsEvent.class);
        igmpStats = null;
        statsDelegate = null;
        scheduledFuture.cancel(true);
        executorForIgmp.shutdown();
        cfgService.unregisterProperties(getClass(), false);
        log.info("IgmpStatisticsManager Deactivated");
    }

    /**
     *Delegate allowing the StateMachine to notify us of events.
     */
    private class InternalIgmpDelegateForStatistics implements IgmpStatisticsDelegate {
        @Override
        public void notify(IgmpStatisticsEvent igmpStatisticsEvent) {
            log.debug("Authentication Statistics event {} for {}", igmpStatisticsEvent.type(),
                    igmpStatisticsEvent.subject());
            post(igmpStatisticsEvent);
        }
    }

    private class IgmpStatisticsEventPublisher implements Runnable {
        public void run() {

            log.debug("Notifying IgmpStatisticsEvent");
            log.debug("--IgmpDisconnect--" + igmpStats.getIgmpDisconnect());
            log.debug("--IgmpFailJoinReq--" + igmpStats.getIgmpFailJoinReq());
            log.debug("--IgmpJoinReq--" + igmpStats.getIgmpJoinReq());
            log.debug("--IgmpLeaveReq--" + igmpStats.getIgmpLeaveReq());
            log.debug("--IgmpMsgReceived--" + igmpStats.getIgmpMsgReceived());
            log.debug("--IgmpSuccessJoinRejoinReq--" + igmpStats.getIgmpSuccessJoinRejoinReq());
            log.debug("--Igmpv1MemershipReport--" + igmpStats.getIgmpv1MemershipReport());
            log.debug("--Igmpv2LeaveGroup--" + igmpStats.getIgmpv2LeaveGroup());
            log.debug("--Igmpv2MembershipReport--" + igmpStats.getIgmpv2MembershipReport());
            log.debug("--Igmpv3MembershipQuery--" + igmpStats.getIgmpv3MembershipQuery());
            log.debug("--Igmpv3MembershipReport--" + igmpStats.getIgmpv3MembershipReport());
            log.debug("--InvalidIgmpMsgReceived--" + igmpStats.getInvalidIgmpMsgReceived());
            log.debug("--TotalMsgReceived--  " + igmpStats.getTotalMsgReceived());
            if (statsDelegate != null) {
                statsDelegate.
                    notify(new IgmpStatisticsEvent(IgmpStatisticsEvent.Type.STATS_UPDATE, igmpStats));
            }
        }
    }

}
