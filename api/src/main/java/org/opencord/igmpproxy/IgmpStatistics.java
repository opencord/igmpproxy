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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Records metrics for IgmpProxy application.
 */
public class IgmpStatistics {
    private static final long RESET_VALUE = 0L;

    //Total number of join requests
    private AtomicLong igmpJoinReq = new AtomicLong();
    //Total number of successful join and rejoin requests
    private AtomicLong igmpSuccessJoinRejoinReq = new AtomicLong();
    //Total number of failed join requests
    private AtomicLong igmpFailJoinReq = new AtomicLong();
    //Total number of leaves requests
    private AtomicLong igmpLeaveReq = new AtomicLong();
    // Total number of disconnects
    private AtomicLong igmpDisconnect = new AtomicLong();
    //Count of Total number of IGMPV3_MEMBERSHIP_QUERY
    private AtomicLong igmpv3MembershipQuery = new AtomicLong();
    //Count of IGMPV1_MEMBERSHIP_REPORT
    private AtomicLong igmpv1MembershipReport = new AtomicLong();
    //Count of IGMPV3_MEMBERSHIP_REPORT
    private AtomicLong igmpv3MembershipReport = new AtomicLong();
    //Count of IGMPV2_MEMBERSHIP_REPORT
    private AtomicLong igmpv2MembershipReport = new AtomicLong();
    //Count of TYPE_IGMPV2_LEAVE_GROUP
    private AtomicLong igmpv2LeaveGroup = new AtomicLong();
    //Total number of messages received.
    private AtomicLong totalMsgReceived = new AtomicLong();
    //Total number of IGMP messages received
    private AtomicLong igmpMsgReceived = new AtomicLong();
    //Total number of invalid IGMP messages received
    private AtomicLong invalidIgmpMsgReceived = new AtomicLong();
    //Counter for unknown igmp type
    private AtomicLong unknownIgmpTypePacketsRxCounter = new AtomicLong();
    // Counter for igmp report with wrong mode.
    private AtomicLong reportsRxWithWrongModeCounter = new AtomicLong();
    // Counter for failed join due to insufficient permission access
    private AtomicLong failJoinReqInsuffPermissionAccessCounter = new AtomicLong();
    // Counter for invalid group ip address i.e not a valid multicast address.
    private AtomicLong failJoinReqUnknownMulticastIpCounter = new AtomicLong();
    // Counter for unconfigured group
    private AtomicLong unconfiguredGroupCounter = new AtomicLong();
    // Counter for valid igmp packet
    private AtomicLong validIgmpPacketCounter = new AtomicLong();
    // Counter for current number of igmp channel joins
    private AtomicLong igmpChannelJoinCounter = new AtomicLong();
    // Counter for current group number
    private AtomicLong currentGrpNumCounter = new AtomicLong();
    // Counter for igmp Checksum
    private AtomicLong igmpValidChecksumCounter = new AtomicLong();
    // Counter for Invalid Igmp Length
    private AtomicLong invalidIgmpLength = new AtomicLong();
    //Total number of general IGMP membership query messages received
    private AtomicLong igmpGeneralMembershipQuery = new AtomicLong();
    //Total number of group specific IGMP membership query messages received
    private AtomicLong igmpGrpSpecificMembershipQuery = new AtomicLong();
    //Total number of group and source specific IGMP membership query messages received
    private AtomicLong igmpGrpAndSrcSpecificMembershipQuery = new AtomicLong();

    public void setStats(IgmpStatistics current) {
        igmpJoinReq.set(current.igmpJoinReq.get());
        igmpSuccessJoinRejoinReq.set(current.igmpSuccessJoinRejoinReq.get());
        igmpFailJoinReq.set(current.igmpFailJoinReq.get());
        igmpLeaveReq.set(current.igmpLeaveReq.get());
        igmpDisconnect.set(current.igmpDisconnect.get());
        igmpv3MembershipQuery.set(current.igmpv3MembershipQuery.get());
        igmpv1MembershipReport.set(current.igmpv1MembershipReport.get());
        igmpv3MembershipReport.set(current.igmpv3MembershipReport.get());
        igmpv2MembershipReport.set(current.igmpv2MembershipReport.get());
        igmpv2LeaveGroup.set(current.igmpv2LeaveGroup.get());
        totalMsgReceived.set(current.totalMsgReceived.get());
        igmpMsgReceived.set(current.igmpMsgReceived.get());
        invalidIgmpMsgReceived.set(current.invalidIgmpMsgReceived.get());
        unknownIgmpTypePacketsRxCounter.set(current.unknownIgmpTypePacketsRxCounter.get());
        reportsRxWithWrongModeCounter.set(current.reportsRxWithWrongModeCounter.get());
        failJoinReqInsuffPermissionAccessCounter.set(current.failJoinReqInsuffPermissionAccessCounter.get());
        failJoinReqUnknownMulticastIpCounter.set(current.failJoinReqUnknownMulticastIpCounter.get());
        unconfiguredGroupCounter.set(current.unconfiguredGroupCounter.get());
        validIgmpPacketCounter.set(current.validIgmpPacketCounter.get());
        igmpChannelJoinCounter.set(current.igmpChannelJoinCounter.get());
        currentGrpNumCounter.set(current.currentGrpNumCounter.get());
        igmpValidChecksumCounter.set(current.igmpValidChecksumCounter.get());
        invalidIgmpLength.set(current.invalidIgmpLength.get());
        igmpGeneralMembershipQuery.set(current.igmpGeneralMembershipQuery.get());
        igmpGrpSpecificMembershipQuery.set(current.igmpGrpSpecificMembershipQuery.get());
        igmpGrpAndSrcSpecificMembershipQuery.set(current.igmpGrpAndSrcSpecificMembershipQuery.get());
    }

    public void resetAll() {
        igmpJoinReq.set(RESET_VALUE);
        igmpLeaveReq.set(RESET_VALUE);
        igmpDisconnect.set(RESET_VALUE);
        totalMsgReceived.set(RESET_VALUE);
        igmpv2LeaveGroup.set(RESET_VALUE);
        invalidIgmpLength.set(RESET_VALUE);
        igmpv3MembershipQuery.set(RESET_VALUE);
        igmpChannelJoinCounter.set(RESET_VALUE);
        igmpv1MembershipReport.set(RESET_VALUE);
        igmpv2MembershipReport.set(RESET_VALUE);
        igmpv3MembershipReport.set(RESET_VALUE);
        invalidIgmpMsgReceived.set(RESET_VALUE);
        validIgmpPacketCounter.set(RESET_VALUE);
        currentGrpNumCounter.set(RESET_VALUE);
        igmpFailJoinReq.set(RESET_VALUE);
        unconfiguredGroupCounter.set(RESET_VALUE);
        igmpValidChecksumCounter.set(RESET_VALUE);
        igmpGeneralMembershipQuery.set(RESET_VALUE);
        igmpSuccessJoinRejoinReq.set(RESET_VALUE);
        igmpGrpSpecificMembershipQuery.set(RESET_VALUE);
        reportsRxWithWrongModeCounter.set(RESET_VALUE);
        unknownIgmpTypePacketsRxCounter.set(RESET_VALUE);
        failJoinReqUnknownMulticastIpCounter.set(RESET_VALUE);
        igmpGrpAndSrcSpecificMembershipQuery.set(RESET_VALUE);
        failJoinReqInsuffPermissionAccessCounter.set(RESET_VALUE);
    }

    public void increaseStat(IgmpStatisticType type) {
        switch (type) {
            case IGMP_JOIN_REQ:
                igmpJoinReq.incrementAndGet();
                break;
            case IGMP_LEAVE_REQ:
                igmpLeaveReq.incrementAndGet();
                break;
            case IGMP_DISCONNECT:
                igmpDisconnect.incrementAndGet();
                break;
            case IGMP_MSG_RECEIVED:
                break;
            case TOTAL_MSG_RECEIVED:
                totalMsgReceived.incrementAndGet();
                break;
            case IGMP_V2_LEAVE_GROUP:
                igmpv2LeaveGroup.incrementAndGet();
                igmpMsgReceived.incrementAndGet();
                break;
            case INVALID_IGMP_LENGTH:
                invalidIgmpLength.incrementAndGet();
                break;
            case IGMP_V3_MEMBERSHIP_QUERY:
                igmpv3MembershipQuery.incrementAndGet();
                igmpMsgReceived.incrementAndGet();
                break;
            case IGMP_CHANNEL_JOIN_COUNTER:
                igmpChannelJoinCounter.incrementAndGet();
                break;
            case IGMP_V1_MEMBERSHIP_REPORT:
                igmpv1MembershipReport.incrementAndGet();
                igmpMsgReceived.incrementAndGet();
                break;
            case IGMP_V2_MEMBERSHIP_REPORT:
                igmpv2MembershipReport.incrementAndGet();
                igmpMsgReceived.incrementAndGet();
                break;
            case IGMP_V3_MEMBERSHIP_REPORT:
                igmpv3MembershipReport.incrementAndGet();
                igmpMsgReceived.incrementAndGet();
                break;
            case INVALID_IGMP_MSG_RECEIVED:
                invalidIgmpMsgReceived.incrementAndGet();
                break;
            case VALID_IGMP_PACKET_COUNTER:
                validIgmpPacketCounter.incrementAndGet();
                break;
            case CURRENT_GRP_NUMBER_COUNTER:
                currentGrpNumCounter.incrementAndGet();
                break;
            case IGMP_FAIL_JOIN_REQ:
                igmpFailJoinReq.incrementAndGet();
                break;
            case UNCONFIGURED_GROUP_COUNTER:
                unconfiguredGroupCounter.incrementAndGet();
                break;
            case IGMP_VALID_CHECKSUM_COUNTER:
                igmpValidChecksumCounter.incrementAndGet();
                break;
            case IGMP_GENERAL_MEMBERSHIP_QUERY:
                igmpGeneralMembershipQuery.incrementAndGet();
                break;
            case IGMP_SUCCESS_JOIN_RE_JOIN_REQ:
                igmpSuccessJoinRejoinReq.incrementAndGet();
                break;
            case IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY:
                igmpGrpSpecificMembershipQuery.incrementAndGet();
                break;
            case REPORTS_RX_WITH_WRONG_MODE_COUNTER:
                reportsRxWithWrongModeCounter.incrementAndGet();
                break;
            case UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER:
                unknownIgmpTypePacketsRxCounter.incrementAndGet();
                break;
            case FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER:
                failJoinReqUnknownMulticastIpCounter.incrementAndGet();
                break;
            case IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY:
                igmpGrpAndSrcSpecificMembershipQuery.incrementAndGet();
                break;
            case FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER:
                failJoinReqInsuffPermissionAccessCounter.incrementAndGet();
                break;
            default:
                break;
        }
    }

    public Long getStat(IgmpStatisticType type) {
        Long value;
        switch (type) {
            case IGMP_JOIN_REQ:
                value = igmpJoinReq.get();
                break;
            case IGMP_LEAVE_REQ:
                value = igmpLeaveReq.get();
                break;
            case IGMP_DISCONNECT:
                value = igmpDisconnect.get();
                break;
            case IGMP_MSG_RECEIVED:
                value = igmpMsgReceived.get();
                break;
            case TOTAL_MSG_RECEIVED:
                value = totalMsgReceived.get();
                break;
            case IGMP_V2_LEAVE_GROUP:
                value = igmpv2LeaveGroup.get();
                break;
            case INVALID_IGMP_LENGTH:
                value = invalidIgmpLength.get();
                break;
            case IGMP_V3_MEMBERSHIP_QUERY:
                value = igmpv3MembershipQuery.get();
                break;
            case IGMP_CHANNEL_JOIN_COUNTER:
                value = igmpChannelJoinCounter.get();
                break;
            case IGMP_V1_MEMBERSHIP_REPORT:
                value = igmpv1MembershipReport.get();
                break;
            case IGMP_V2_MEMBERSHIP_REPORT:
                value = igmpv2MembershipReport.get();
                break;
            case IGMP_V3_MEMBERSHIP_REPORT:
                value = igmpv3MembershipReport.get();
                break;
            case INVALID_IGMP_MSG_RECEIVED:
                value = invalidIgmpMsgReceived.get();
                break;
            case VALID_IGMP_PACKET_COUNTER:
                value = validIgmpPacketCounter.get();
                break;
            case CURRENT_GRP_NUMBER_COUNTER:
                value = currentGrpNumCounter.get();
                break;
            case IGMP_FAIL_JOIN_REQ:
                value = igmpFailJoinReq.get();
                break;
            case UNCONFIGURED_GROUP_COUNTER:
                value = unconfiguredGroupCounter.get();
                break;
            case IGMP_VALID_CHECKSUM_COUNTER:
                value = igmpValidChecksumCounter.get();
                break;
            case IGMP_GENERAL_MEMBERSHIP_QUERY:
                value = igmpGeneralMembershipQuery.get();
                break;
            case IGMP_SUCCESS_JOIN_RE_JOIN_REQ:
                value = igmpSuccessJoinRejoinReq.get();
                break;
            case IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY:
                value = igmpGrpSpecificMembershipQuery.get();
                break;
            case REPORTS_RX_WITH_WRONG_MODE_COUNTER:
                value = reportsRxWithWrongModeCounter.get();
                break;
            case UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER:
                value = unknownIgmpTypePacketsRxCounter.get();
                break;
            case FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER:
                value = failJoinReqUnknownMulticastIpCounter.get();
                break;
            case IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY:
                value = igmpGrpAndSrcSpecificMembershipQuery.get();
                break;
            case FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER:
                value = failJoinReqInsuffPermissionAccessCounter.get();
                break;
            default:
                value = null;
                break;
        }
        return value;
    }
}
