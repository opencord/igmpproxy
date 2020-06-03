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

import org.onlab.util.KryoNamespace;
import org.onosproject.cluster.NodeId;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.cluster.messaging.ClusterMessage;
import org.onosproject.store.cluster.messaging.MessageSubject;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.opencord.igmpproxy.IgmpStatisticType;
import org.opencord.igmpproxy.IgmpStatisticsEvent;
import org.opencord.igmpproxy.IgmpStatisticsEventListener;
import org.opencord.igmpproxy.IgmpStatisticsService;
import org.opencord.igmpproxy.IgmpLeadershipService;
import org.opencord.igmpproxy.IgmpStatistics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.onlab.util.SafeRecurringTask;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.event.AbstractListenerManager;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.STATISTICS_GENERATION_PERIOD;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.STATISTICS_SYNC_PERIOD;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.STATISTICS_GENERATION_PERIOD_DEFAULT;
import static org.opencord.igmpproxy.impl.OsgiPropertyConstants.STATISTICS_SYNC_PERIOD_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.google.common.base.Strings;


/**
 * Process the stats collected in Igmp proxy application. Publish to kafka onos.
 */
@Component(immediate = true, property = {
        STATISTICS_GENERATION_PERIOD + ":Integer=" + STATISTICS_GENERATION_PERIOD_DEFAULT,
        STATISTICS_SYNC_PERIOD + ":Integer=" + STATISTICS_SYNC_PERIOD_DEFAULT,
})
public class IgmpStatisticsManager extends
        AbstractListenerManager<IgmpStatisticsEvent, IgmpStatisticsEventListener>
        implements IgmpStatisticsService {
    private static final String ALL_STATISTICS = "all-statistics";

    private static final String IGMP_STATISTICS = "igmp-statistics";
    private static final String IGMP_STATISTICS_LEADERSHIP = "igmp-statistics";

    private final Logger log = getLogger(getClass());
    private IgmpStatistics igmpStats;

    private ScheduledExecutorService executorForIgmp;
    private ScheduledFuture<?> publisherTask;
    private ScheduledFuture<?> syncTask;

    protected int statisticsGenerationPeriodInSeconds = STATISTICS_GENERATION_PERIOD_DEFAULT;
    protected int statisticsSyncPeriodInSeconds = STATISTICS_SYNC_PERIOD_DEFAULT;

    private EventuallyConsistentMap<NodeId, IgmpStatistics> statistics;

    private static final MessageSubject RESET_SUBJECT = new MessageSubject("igmp-statistics-reset");

    private static final boolean INVALID_VALUE = false;
    private static final boolean VALID_VALUE = true;

    KryoNamespace statSerializer = KryoNamespace.newBuilder()
            .register(KryoNamespaces.API)
            .register(IgmpStatistics.class)
            .build();

    //Statistics values are valid or invalid
    private AtomicBoolean validityCheck = new AtomicBoolean(INVALID_VALUE);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IgmpLeadershipService leadershipManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ClusterCommunicationService clusterCommunicationService;

    @Activate
    public void activate(ComponentContext context) {
        igmpStats = getIgmpStatsInstance();


        statistics = storageService.<NodeId, IgmpStatistics>eventuallyConsistentMapBuilder()
                .withName(IGMP_STATISTICS)
                .withSerializer(statSerializer)
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        initStats(statistics.get(leadershipManager.getLocalNodeId()));
        syncStats();

        leadershipManager.runForLeadership(IGMP_STATISTICS_LEADERSHIP);

        eventDispatcher.addSink(IgmpStatisticsEvent.class, listenerRegistry);
        executorForIgmp = Executors.newScheduledThreadPool(1);
        cfgService.registerProperties(getClass());

        clusterCommunicationService.addSubscriber(RESET_SUBJECT, Serializer.using(statSerializer)::decode,
                this::resetLocal, executorForIgmp);

        modified(context);
        log.info("IgmpStatisticsManager Activated");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<String, Object> properties = context.getProperties();
        try {
            String s = Tools.get(properties, STATISTICS_GENERATION_PERIOD);
            statisticsGenerationPeriodInSeconds = Strings.isNullOrEmpty(s) ?
                    Integer.parseInt(STATISTICS_GENERATION_PERIOD)
                    : Integer.parseInt(s.trim());
            log.debug("statisticsGenerationPeriodInSeconds: {}", statisticsGenerationPeriodInSeconds);
            statisticsSyncPeriodInSeconds = Strings.isNullOrEmpty(s) ?
                    Integer.parseInt(STATISTICS_SYNC_PERIOD)
                    : Integer.parseInt(s.trim());
            log.debug("statisticsSyncPeriodInSeconds: {}", statisticsSyncPeriodInSeconds);
        } catch (NumberFormatException ne) {
            log.error("Unable to parse configuration parameter", ne);
            statisticsGenerationPeriodInSeconds = STATISTICS_GENERATION_PERIOD_DEFAULT;
            statisticsSyncPeriodInSeconds = STATISTICS_SYNC_PERIOD_DEFAULT;
        }
        stopPublishTask();
        stopSyncTask();

        startPublishTask();
        startSyncTask();
    }

    @Deactivate
    public void deactivate() {
        eventDispatcher.removeSink(IgmpStatisticsEvent.class);
        stopPublishTask();
        stopSyncTask();
        executorForIgmp.shutdown();
        cfgService.unregisterProperties(getClass(), false);
        igmpStats = null;
        clusterCommunicationService.removeSubscriber(RESET_SUBJECT);
        leadershipManager.withdraw(IGMP_STATISTICS_LEADERSHIP);
        log.info("IgmpStatisticsManager Deactivated");
    }

    private IgmpStatistics getIgmpStatsInstance() {
        if (igmpStats == null) {
            igmpStats = new IgmpStatistics();
            log.info("Instance of igmp-statistics created.");
        }
        return igmpStats;
    }

    private void syncStats() {
        if (!validityCheck.get()) {
            //sync with valid values
            statistics.put(leadershipManager.getLocalNodeId(), snapshot());
            validityCheck.set(VALID_VALUE);
            log.debug("Valid statistic values are put.");
        }
    }

    private void initStats(IgmpStatistics init) {
        if (init == null) {
            log.warn("Igmp statistics was not created.");
            return;
        }
        igmpStats.setStats(init);
    }

    private IgmpStatistics snapshot() {
        return getIgmpStatsInstance();
    }

    private void startSyncTask() {
        syncTask = startTask(this::syncStats, statisticsSyncPeriodInSeconds);
        log.debug("Sync task started. period in seconds: {}", statisticsSyncPeriodInSeconds);
    }

    private void stopSyncTask() {
        stopTask(syncTask);
        log.debug("Sync task stopped.");
    }

    private void startPublishTask() {
        publisherTask = startTask(this::publishStats, statisticsGenerationPeriodInSeconds);
        log.debug("Publisher task started. period in seconds: {}", statisticsGenerationPeriodInSeconds);
    }

    private void stopPublishTask() {
        stopTask(publisherTask);
        log.debug("Publisher task stopped.");
    }

    private ScheduledFuture<?> startTask(Runnable r, int rate) {
        return executorForIgmp.scheduleAtFixedRate(SafeRecurringTask.wrap(r),
                0, rate, TimeUnit.SECONDS);
    }

    private void stopTask(ScheduledFuture<?> task) {
        if (task != null) {
            task.cancel(true);
        }
    }

    private void resetLocal(ClusterMessage message) {
        String resetType = new String(message.payload(), StandardCharsets.UTF_8);
        if (resetType.equals(ALL_STATISTICS)) {
            //reset all-statistics
            for (IgmpStatisticType type : IgmpStatisticType.values()) {
                resetLocalStats(type);
            }
        } else {
            log.warn("Unhandled this type. {}", resetType);
        }
    }

    private void resetLocalStats(IgmpStatisticType type) {
        switch (type) {
            case IGMP_JOIN_REQ:
                igmpStats.resetIgmpJoinReq();
                break;
            case IGMP_LEAVE_REQ:
                igmpStats.resetIgmpLeaveReq();
                break;
            case IGMP_DISCONNECT:
                igmpStats.resetIgmpDisconnect();
                break;
            case IGMP_MSG_RECEIVED:
                break;
            case TOTAL_MSG_RECEIVED:
                igmpStats.resetTotalMsgReceived();
                break;
            case IGMP_V2_LEAVE_GROUP:
                igmpStats.resetIgmpv2LeaveGroup();
                break;
            case INVALID_IGMP_LENGTH:
                igmpStats.resetInvalidIgmpLength();
                break;
            case IGMP_V3_MEMBERSHIP_QUERY:
                igmpStats.resetIgmpv3MembershipQuery();
                break;
            case IGMP_CHANNEL_JOIN_COUNTER:
                igmpStats.resetIgmpChannelJoinCounter();
                break;
            case IGMP_V1_MEMBERSHIP_REPORT:
                igmpStats.resetIgmpv1MembershipReport();
                break;
            case IGMP_V2_MEMBERSHIP_REPORT:
                igmpStats.resetIgmpv2MembershipReport();
                break;
            case IGMP_V3_MEMBERSHIP_REPORT:
                igmpStats.resetIgmpv3MembershipReport();
                break;
            case INVALID_IGMP_MSG_RECEIVED:
                igmpStats.resetInvalidIgmpMsgReceived();
                break;
            case VALID_IGMP_PACKET_COUNTER:
                igmpStats.resetValidIgmpPacketCounter();
                break;
            case CURRENT_GRP_NUMBER_COUNTER:
                igmpStats.resetCurrentGrpNumCounter();
                break;
            case IGMP_FAIL_JOIN_RE_JOIN_REQ:
                igmpStats.resetIgmpFailJoinReq();
                break;
            case UNCONFIGURED_GROUP_COUNTER:
                igmpStats.resetUnconfiguredGroupCounter();
                break;
            case IGMP_VALID_CHECKSUM_COUNTER:
                igmpStats.resetIgmpValidChecksumCounter();
                break;
            case IGMP_GENERAL_MEMBERSHIP_QUERY:
                igmpStats.resetIgmpGeneralMembershipQuery();
                break;
            case IGMP_SUCCESS_JOIN_RE_JOIN_REQ:
                igmpStats.resetIgmpSuccessJoinRejoinReq();
                break;
            case IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY:
                igmpStats.resetIgmpGrpSpecificMembershipQuery();
                break;
            case REPORTS_RX_WITH_WRONG_MODE_COUNTER:
                igmpStats.resetReportsRxWithWrongModeCounter();
                break;
            case UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER:
                igmpStats.resetUnknownIgmpTypePacketsRxCounter();
                break;
            case FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER:
                igmpStats.resetFailJoinReqUnknownMulticastIpCounter();
                break;
            case IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY:
                igmpStats.resetIgmpGrpAndSrcSpecificMembershipQuery();
                break;
            case FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER:
                igmpStats.resetFailJoinReqInsuffPermissionAccessCounter();
                break;
            default:
                log.warn("Unhandled statistic type. {}", type);
                break;
        }
        validityCheck.set(INVALID_VALUE);
    }

    /**
     * Publishes stats.
     */
    private void publishStats() {
        // Only publish events if we are the cluster leader for Igmp-stats
        if (!Objects.equals(leadershipManager.getLeader(IGMP_STATISTICS_LEADERSHIP),
                leadershipManager.getLocalNodeId())) {
            log.debug("This is not leader of : {}", IGMP_STATISTICS_LEADERSHIP);
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Notifying stats: {}", igmpStats);
            log.debug("--IgmpDisconnect--" + igmpStats.getIgmpDisconnect());
            log.debug("--IgmpFailJoinReq--" + igmpStats.getIgmpFailJoinReq());
            log.debug("--IgmpJoinReq--" + igmpStats.getIgmpJoinReq());
            log.debug("--IgmpLeaveReq--" + igmpStats.getIgmpLeaveReq());
            log.debug("--IgmpMsgReceived--" + igmpStats.getIgmpMsgReceived());
            log.debug("--IgmpSuccessJoinRejoinReq--" + igmpStats.getIgmpSuccessJoinRejoinReq());
            log.debug("--Igmpv1MemershipReport--" + igmpStats.getIgmpv1MembershipReport());
            log.debug("--Igmpv2LeaveGroup--" + igmpStats.getIgmpv2LeaveGroup());
            log.debug("--Igmpv2MembershipReport--" + igmpStats.getIgmpv2MembershipReport());
            log.debug("--Igmpv3MembershipQuery--" + igmpStats.getIgmpv3MembershipQuery());
            log.debug("--Igmpv3MembershipReport--" + igmpStats.getIgmpv3MembershipReport());
            log.debug("--InvalidIgmpMsgReceived--" + igmpStats.getInvalidIgmpMsgReceived());
            log.debug("--TotalMsgReceived--  " + igmpStats.getTotalMsgReceived());
            log.debug("--UnknownIgmpTypePacketsRx--" + igmpStats.getUnknownIgmpTypePacketsRxCounter());
            log.debug("--ReportsRxWithWrongMode--" + igmpStats.getReportsRxWithWrongModeCounter());
            log.debug("--FailJoinReqInsuffPermission--" + igmpStats.getFailJoinReqInsuffPermissionAccessCounter());
            log.debug("--FailJoinReqUnknownMulticastIp--" + igmpStats.getFailJoinReqUnknownMulticastIpCounter());
            log.debug("--UnconfiguredGroupCounter--" + igmpStats.getUnconfiguredGroupCounter());
            log.debug("--ValidIgmpPacketCounter--" + igmpStats.getValidIgmpPacketCounter());
            log.debug("--IgmpChannelJoinCounter--" + igmpStats.getIgmpChannelJoinCounter());
            log.debug("--CurrentGrpNumCounter--" + igmpStats.getCurrentGrpNumCounter());
            log.debug("--IgmpValidChecksumCounter--" + igmpStats.getIgmpValidChecksumCounter());
            log.debug("--InvalidIgmpLength--" + igmpStats.getInvalidIgmpLength());
            log.debug("--IgmpGeneralMembershipQuery--" + igmpStats.getIgmpGeneralMembershipQuery());
            log.debug("--IgmpGrpSpecificMembershipQuery--" + igmpStats.getIgmpGrpSpecificMembershipQuery());
            log.debug("--IgmpGrpAndSrcSpecificMembershipQuery--" + igmpStats.getIgmpGrpAndSrcSpecificMembershipQuery());
        }

        post(new IgmpStatisticsEvent(IgmpStatisticsEvent.Type.STATS_UPDATE, igmpStats));
    }

    @Override
    public void increaseStat(IgmpStatisticType type) {
        switch (type) {
            case IGMP_JOIN_REQ:
                igmpStats.increaseIgmpJoinReq();
                break;
            case IGMP_LEAVE_REQ:
                igmpStats.increaseIgmpLeaveReq();
                break;
            case IGMP_DISCONNECT:
                igmpStats.increaseIgmpDisconnect();
                break;
            case IGMP_MSG_RECEIVED:
                break;
            case TOTAL_MSG_RECEIVED:
                igmpStats.increaseTotalMsgReceived();
                break;
            case IGMP_V2_LEAVE_GROUP:
                igmpStats.increaseIgmpv2LeaveGroup();
                break;
            case INVALID_IGMP_LENGTH:
                igmpStats.increaseInvalidIgmpLength();
                break;
            case IGMP_V3_MEMBERSHIP_QUERY:
                igmpStats.increaseIgmpv3MembershipQuery();
                break;
            case IGMP_CHANNEL_JOIN_COUNTER:
                igmpStats.increaseIgmpChannelJoinCounter();
                break;
            case IGMP_V1_MEMBERSHIP_REPORT:
                igmpStats.increaseIgmpv1MembershipReport();
                break;
            case IGMP_V2_MEMBERSHIP_REPORT:
                igmpStats.increaseIgmpv2MembershipReport();
                break;
            case IGMP_V3_MEMBERSHIP_REPORT:
                igmpStats.increaseIgmpv3MembershipReport();
                break;
            case INVALID_IGMP_MSG_RECEIVED:
                igmpStats.increaseInvalidIgmpMsgReceived();
                break;
            case VALID_IGMP_PACKET_COUNTER:
                igmpStats.increaseValidIgmpPacketCounter();
                break;
            case CURRENT_GRP_NUMBER_COUNTER:
                igmpStats.increaseCurrentGrpNumCounter();
                break;
            case IGMP_FAIL_JOIN_RE_JOIN_REQ:
                igmpStats.increaseIgmpFailJoinReq();
                break;
            case UNCONFIGURED_GROUP_COUNTER:
                igmpStats.increaseUnconfiguredGroupCounter();
                break;
            case IGMP_VALID_CHECKSUM_COUNTER:
                igmpStats.increaseIgmpValidChecksumCounter();
                break;
            case IGMP_GENERAL_MEMBERSHIP_QUERY:
                igmpStats.increaseIgmpGeneralMembershipQuery();
                break;
            case IGMP_SUCCESS_JOIN_RE_JOIN_REQ:
                igmpStats.increaseIgmpSuccessJoinRejoinReq();
                break;
            case IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY:
                igmpStats.increaseIgmpGrpSpecificMembershipQuery();
                break;
            case REPORTS_RX_WITH_WRONG_MODE_COUNTER:
                igmpStats.increaseReportsRxWithWrongModeCounter();
                break;
            case UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER:
                igmpStats.increaseUnknownIgmpTypePacketsRxCounter();
                break;
            case FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER:
                igmpStats.increaseFailJoinReqUnknownMulticastIpCounter();
                break;
            case IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY:
                igmpStats.increaseIgmpGrpAndSrcSpecificMembershipQuery();
                break;
            case FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER:
                igmpStats.increaseFailJoinReqInsuffPermissionAccessCounter();
                break;
            default:
                log.warn("Unhandled statistic type. {}", type);
                break;
        }
        validityCheck.set(INVALID_VALUE);
    }

    @Override
    public void resetAllStats() {
        byte[] payload = ALL_STATISTICS.getBytes(StandardCharsets.UTF_8);
        ClusterMessage reset = new ClusterMessage(leadershipManager.getLocalNodeId(), RESET_SUBJECT, payload);
        clusterCommunicationService.broadcastIncludeSelf(reset, RESET_SUBJECT,
                Serializer.using(statSerializer)::encode);
    }

    @Override
    public Long getStat(IgmpStatisticType type) {
        Long value;
        switch (type) {
            case IGMP_JOIN_REQ:
                value = igmpStats.getIgmpJoinReq();
                break;
            case IGMP_LEAVE_REQ:
                value = igmpStats.getIgmpLeaveReq();
                break;
            case IGMP_DISCONNECT:
                value = igmpStats.getIgmpDisconnect();
                break;
            case IGMP_MSG_RECEIVED:
                value = igmpStats.getIgmpMsgReceived();
                break;
            case TOTAL_MSG_RECEIVED:
                value = igmpStats.getTotalMsgReceived();
                break;
            case IGMP_V2_LEAVE_GROUP:
                value = igmpStats.getIgmpv2LeaveGroup();
                break;
            case INVALID_IGMP_LENGTH:
                value = igmpStats.getInvalidIgmpLength();
                break;
            case IGMP_V3_MEMBERSHIP_QUERY:
                value = igmpStats.getIgmpv3MembershipQuery();
                break;
            case IGMP_CHANNEL_JOIN_COUNTER:
                value = igmpStats.getIgmpChannelJoinCounter();
                break;
            case IGMP_V1_MEMBERSHIP_REPORT:
                value = igmpStats.getIgmpv1MembershipReport();
                break;
            case IGMP_V2_MEMBERSHIP_REPORT:
                value = igmpStats.getIgmpv2MembershipReport();
                break;
            case IGMP_V3_MEMBERSHIP_REPORT:
                value = igmpStats.getIgmpv3MembershipReport();
                break;
            case INVALID_IGMP_MSG_RECEIVED:
                value = igmpStats.getInvalidIgmpMsgReceived();
                break;
            case VALID_IGMP_PACKET_COUNTER:
                value = igmpStats.getValidIgmpPacketCounter();
                break;
            case CURRENT_GRP_NUMBER_COUNTER:
                value = igmpStats.getCurrentGrpNumCounter();
                break;
            case IGMP_FAIL_JOIN_RE_JOIN_REQ:
                value = igmpStats.getIgmpFailJoinReq();
                break;
            case UNCONFIGURED_GROUP_COUNTER:
                value = igmpStats.getUnconfiguredGroupCounter();
                break;
            case IGMP_VALID_CHECKSUM_COUNTER:
                value = igmpStats.getIgmpValidChecksumCounter();
                break;
            case IGMP_GENERAL_MEMBERSHIP_QUERY:
                value = igmpStats.getIgmpGeneralMembershipQuery();
                break;
            case IGMP_SUCCESS_JOIN_RE_JOIN_REQ:
                value = igmpStats.getIgmpSuccessJoinRejoinReq();
                break;
            case IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY:
                value = igmpStats.getIgmpGrpSpecificMembershipQuery();
                break;
            case REPORTS_RX_WITH_WRONG_MODE_COUNTER:
                value = igmpStats.getReportsRxWithWrongModeCounter();
                break;
            case UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER:
                value = igmpStats.getUnknownIgmpTypePacketsRxCounter();
                break;
            case FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER:
                value = igmpStats.getFailJoinReqUnknownMulticastIpCounter();
                break;
            case IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY:
                value = igmpStats.getIgmpGrpAndSrcSpecificMembershipQuery();
                break;
            case FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER:
                value = igmpStats.getFailJoinReqInsuffPermissionAccessCounter();
                break;
            default:
                value = null;
                log.warn("Unhandled statistic type. {}", type);
                break;
        }
        return value;
    }
}
