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

    public Long getIgmpJoinReq() {
        return igmpJoinReq.get();
    }

    public Long getIgmpSuccessJoinRejoinReq() {
        return igmpSuccessJoinRejoinReq.get();
    }

    public Long getIgmpFailJoinReq() {
        return igmpFailJoinReq.get();
    }

    public Long getIgmpLeaveReq() {
        return igmpLeaveReq.get();
    }

    public Long getIgmpDisconnect() {
        return igmpDisconnect.get();
    }

    public Long getIgmpv3MembershipQuery() {
        return igmpv3MembershipQuery.get();
    }

    public Long getIgmpv1MemershipReport() {
        return igmpv1MembershipReport.get();
    }

    public Long getIgmpv3MembershipReport() {
        return igmpv3MembershipReport.get();
    }

    public Long getIgmpv2MembershipReport() {
        return igmpv2MembershipReport.get();
    }

    public Long getIgmpv2LeaveGroup() {
        return igmpv2LeaveGroup.get();
    }

    public Long getTotalMsgReceived() {
        return totalMsgReceived.get();
    }

    public Long getIgmpMsgReceived() {
        return igmpMsgReceived.get();
    }

    public Long getInvalidIgmpMsgReceived() {
        return invalidIgmpMsgReceived.get();
    }

    public void increaseIgmpJoinReq() {
        igmpJoinReq.incrementAndGet();
    }

    public void resetIgmpJoinReq() {
        igmpJoinReq.set(RESET_VALUE);
    }

    public void increaseIgmpSuccessJoinRejoinReq() {
        igmpSuccessJoinRejoinReq.incrementAndGet();
    }

    public void resetIgmpSuccessJoinRejoinReq() {
        igmpSuccessJoinRejoinReq.set(RESET_VALUE);
    }

    public void increaseIgmpFailJoinReq() {
        igmpFailJoinReq.incrementAndGet();
    }

    public void resetIgmpFailJoinReq() {
        igmpFailJoinReq.set(RESET_VALUE);
    }

    public void increaseIgmpLeaveReq() {
        igmpLeaveReq.incrementAndGet();
    }

    public void resetIgmpLeaveReq() {
        igmpLeaveReq.set(RESET_VALUE);
    }

    public void increaseIgmpDisconnect() {
        igmpDisconnect.incrementAndGet();
    }

    public void resetIgmpDisconnect() {
        igmpDisconnect.set(RESET_VALUE);
    }

    public void increaseIgmpv3MembershipQuery() {
        igmpv3MembershipQuery.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void resetIgmpv3MembershipQuery() {
        igmpv3MembershipQuery.set(RESET_VALUE);
        igmpMsgReceived.decrementAndGet();
    }

    public void increaseIgmpv2MembershipReport() {
        igmpv2MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void resetIgmpv2MembershipReport() {
        igmpv2MembershipReport.set(RESET_VALUE);
        igmpMsgReceived.decrementAndGet();
    }


    public void increaseIgmpv1MembershipReport() {
        igmpv1MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void resetIgmpv1MembershipReport() {
        igmpv1MembershipReport.set(RESET_VALUE);
        igmpMsgReceived.decrementAndGet();
    }

    public void increaseIgmpv3MembershipReport() {
        igmpv3MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void resetIgmpv3MembershipReport() {
        igmpv3MembershipReport.set(RESET_VALUE);
        igmpMsgReceived.decrementAndGet();
    }

    public void increaseIgmpv2LeaveGroup() {
        igmpv2LeaveGroup.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void resetIgmpv2LeaveGroup() {
        igmpv2LeaveGroup.set(RESET_VALUE);
        igmpMsgReceived.decrementAndGet();
    }

    public void increaseInvalidIgmpMsgReceived() {
        invalidIgmpMsgReceived.incrementAndGet();
    }

    public void resetInvalidIgmpMsgReceived() {
        invalidIgmpMsgReceived.set(RESET_VALUE);
    }

    public void increaseTotalMsgReceived() {
        totalMsgReceived.incrementAndGet();
    }

    public void resetTotalMsgReceived() {
        totalMsgReceived.set(RESET_VALUE);
    }

    public Long getValidIgmpPacketCounter() {
        return validIgmpPacketCounter.get();
    }

    public void increaseValidIgmpPacketCounter() {
        validIgmpPacketCounter.incrementAndGet();
    }

    public void resetValidIgmpPacketCounter() {
        validIgmpPacketCounter.set(RESET_VALUE);
    }

    public Long getCurrentGrpNumCounter() {
        return currentGrpNumCounter.get();
    }

    public void increaseCurrentGrpNumCounter() {
        currentGrpNumCounter.incrementAndGet();
    }

    public void resetCurrentGrpNumCounter() {
        currentGrpNumCounter.set(RESET_VALUE);
    }

    public Long getIgmpChannelJoinCounter() {
        return igmpChannelJoinCounter.get();
    }

    public Long getIgmpValidChecksumCounter() {
        return igmpValidChecksumCounter.get();
    }

    public void increaseIgmpChannelJoinCounter() {
        igmpChannelJoinCounter.incrementAndGet();
    }

    public void resetIgmpChannelJoinCounter() {
        igmpChannelJoinCounter.set(RESET_VALUE);
    }

    public void increaseIgmpValidChecksumCounter() {
        igmpValidChecksumCounter.incrementAndGet();
    }

    public void resetIgmpValidChecksumCounter() {
        igmpValidChecksumCounter.set(RESET_VALUE);
    }

    public Long getUnconfiguredGroupCounter() {
        return unconfiguredGroupCounter.get();
    }

    public void increaseUnconfiguredGroupCounter() {
        unconfiguredGroupCounter.incrementAndGet();
    }

    public void resetUnconfiguredGroupCounter() {
        unconfiguredGroupCounter.set(RESET_VALUE);
    }

    public Long getFailJoinReqUnknownMulticastIpCounter() {
        return failJoinReqUnknownMulticastIpCounter.get();
    }

    public void increaseFailJoinReqUnknownMulticastIpCounter() {
        failJoinReqUnknownMulticastIpCounter.incrementAndGet();
    }

    public void resetFailJoinReqUnknownMulticastIpCounter() {
        failJoinReqUnknownMulticastIpCounter.set(RESET_VALUE);
    }

    public Long getFailJoinReqInsuffPermissionAccessCounter() {
        return failJoinReqInsuffPermissionAccessCounter.get();
    }

    public void increaseFailJoinReqInsuffPermissionAccessCounter() {
        failJoinReqInsuffPermissionAccessCounter.incrementAndGet();
    }

    public void resetFailJoinReqInsuffPermissionAccessCounter() {
        failJoinReqInsuffPermissionAccessCounter.set(RESET_VALUE);
    }

    public Long getReportsRxWithWrongModeCounter() {
        return reportsRxWithWrongModeCounter.get();
    }

    public Long getUnknownIgmpTypePacketsRxCounter() {
        return unknownIgmpTypePacketsRxCounter.get();
    }

    public void increaseUnknownIgmpTypePacketsRxCounter() {
        unknownIgmpTypePacketsRxCounter.incrementAndGet();
    }

    public void resetUnknownIgmpTypePacketsRxCounter() {
        unknownIgmpTypePacketsRxCounter.set(RESET_VALUE);
    }

    public void increaseReportsRxWithWrongModeCounter() {
        reportsRxWithWrongModeCounter.incrementAndGet();
    }

    public void resetReportsRxWithWrongModeCounter() {
        reportsRxWithWrongModeCounter.set(RESET_VALUE);
    }

    public Long getInvalidIgmpLength() {
        return invalidIgmpLength.get();
    }

    public void increaseInvalidIgmpLength() {
        invalidIgmpLength.incrementAndGet();
    }

    public void resetInvalidIgmpLength() {
        invalidIgmpLength.set(RESET_VALUE);
    }

    public Long getIgmpGeneralMembershipQuery() {
        return igmpGeneralMembershipQuery.get();
    }

    public Long getIgmpGrpSpecificMembershipQuery() {
        return igmpGrpSpecificMembershipQuery.get();
    }

    public Long getIgmpGrpAndSrcSpecificMembershipQuery() {
        return igmpGrpAndSrcSpecificMembershipQuery.get();
    }

    public void increaseIgmpGeneralMembershipQuery() {
        igmpGeneralMembershipQuery.incrementAndGet();
    }

    public void resetIgmpGeneralMembershipQuery() {
        igmpGeneralMembershipQuery.set(RESET_VALUE);
    }

    public void increaseIgmpGrpSpecificMembershipQuery() {
        igmpGrpSpecificMembershipQuery.incrementAndGet();
    }

    public void resetIgmpGrpSpecificMembershipQuery() {
        igmpGrpSpecificMembershipQuery.set(RESET_VALUE);
    }

    public void increaseIgmpGrpAndSrcSpecificMembershipQuery() {
        igmpGrpAndSrcSpecificMembershipQuery.incrementAndGet();
    }

    public void resetIgmpGrpAndSrcSpecificMembershipQuery() {
        igmpGrpAndSrcSpecificMembershipQuery.set(RESET_VALUE);
    }


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
}
