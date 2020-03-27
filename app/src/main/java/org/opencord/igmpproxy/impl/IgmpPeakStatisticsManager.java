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

package org.opencord.igmpproxy.impl;

import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.PEAK_STATISTICS_GENERATION_PERIOD;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.PEAK_STATISTICS_GENERATION_PERIOD_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.onlab.util.SafeRecurringTask;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.event.AbstractListenerManager;
import org.opencord.igmpproxy.IgmpPeakStatistics;
import org.opencord.igmpproxy.IgmpPeakStatisticsEvent;
import org.opencord.igmpproxy.IgmpPeakStatisticsEventListener;
import org.opencord.igmpproxy.IgmpPeakStatisticsService;
import org.opencord.igmpproxy.IgmpStatisticsEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import com.google.common.base.Strings;

/**
*
* Process the stats collected in Igmp proxy application. Publish to kafka onos.
*
*/
@Component(immediate = true, property = {
       PEAK_STATISTICS_GENERATION_PERIOD + ":Integer=" + PEAK_STATISTICS_GENERATION_PERIOD_DEFAULT,
})
public class IgmpPeakStatisticsManager extends
                 AbstractListenerManager<IgmpPeakStatisticsEvent, IgmpPeakStatisticsEventListener>
                         implements IgmpPeakStatisticsService {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    private IgmpPeakStatistics igmpPeakStats;

    ScheduledExecutorService executorForIgmp;
    private ScheduledFuture<?> publisherTask;

    protected int peakStatisticsGenerationPeriodInSeconds = PEAK_STATISTICS_GENERATION_PERIOD_DEFAULT;

     @Override
     public IgmpPeakStatistics getPeakStatistics() {
          return igmpPeakStats;
     }

     @Activate
    public void activate(ComponentContext context) {
        igmpPeakStats = new IgmpPeakStatistics();
        eventDispatcher.addSink(IgmpPeakStatisticsEvent.class, listenerRegistry);
        executorForIgmp = Executors.newScheduledThreadPool(1);
        cfgService.registerProperties(getClass());
        modified(context);
        log.info("IgmpPeakStatisticsManager Activated");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<String, Object> properties = context.getProperties();

        try {
            String s = Tools.get(properties, PEAK_STATISTICS_GENERATION_PERIOD);
            peakStatisticsGenerationPeriodInSeconds = Strings.isNullOrEmpty(s) ?
                Integer.parseInt(PEAK_STATISTICS_GENERATION_PERIOD)
                    : Integer.parseInt(s.trim());
        } catch (NumberFormatException ne) {
            log.error("Unable to parse configuration parameter for eventGenerationPeriodInSeconds", ne);
            peakStatisticsGenerationPeriodInSeconds = PEAK_STATISTICS_GENERATION_PERIOD_DEFAULT;
        }
        if (publisherTask != null) {
            publisherTask.cancel(true);
        }
        publisherTask = executorForIgmp.scheduleAtFixedRate(SafeRecurringTask.wrap(this::publishPeakStats),
                0, peakStatisticsGenerationPeriodInSeconds, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(IgmpStatisticsEvent.class);
        publisherTask.cancel(true);
        executorForIgmp.shutdown();
        cfgService.unregisterProperties(getClass(), false);
        igmpPeakStats = null;
        log.info("IgmpPeakStatisticsManager Deactivated");
    }

    private void publishPeakStats() {
         if (log.isDebugEnabled()) {
            log.debug("Notifying stats: {}", igmpPeakStats);
            log.debug("--PeakConnectionCount--", igmpPeakStats.getPeakConnectionCount());
            log.debug("--PeakConnectionDuration--", igmpPeakStats.getPeakConnectionDuration());
            log.debug("--PeakDisconnectCount--", igmpPeakStats.getPeakDisconnectCount());
            log.debug("--PeakDisconnectDuration--", igmpPeakStats.getPeakDisconnectDuration());
            log.debug("--PeakMsgCount--", igmpPeakStats.getPeakMsgCount());
            log.debug("--PeakMsgDuration--", igmpPeakStats.getPeakMsgDuration());
        }
         post(new IgmpPeakStatisticsEvent(IgmpPeakStatisticsEvent.Type.STATS_UPDATE, igmpPeakStats));
    }

}
