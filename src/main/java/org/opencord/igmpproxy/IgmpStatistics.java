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
 *
 * Records metrics for IgmpProxy application.
 *
 */
public class IgmpStatistics {

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

    public void increaseIgmpSuccessJoinRejoinReq() {
        igmpSuccessJoinRejoinReq.incrementAndGet();
    }

    public void increaseIgmpFailJoinReq() {
        igmpFailJoinReq.incrementAndGet();
    }

    public void increaseIgmpLeaveReq() {
        igmpLeaveReq.incrementAndGet();
    }

    public void increaseIgmpDisconnect() {
        igmpDisconnect.incrementAndGet();
    }

    public void increaseIgmpv3MembershipQuery() {
        igmpv3MembershipQuery.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void increaseIgmpv2MembershipReport() {
        igmpv2MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void increaseIgmpv1MembershipReport() {
        igmpv1MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void increaseIgmpv3MembershipReport() {
        igmpv3MembershipReport.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void increaseIgmpv2LeaveGroup() {
        igmpv2LeaveGroup.incrementAndGet();
        igmpMsgReceived.incrementAndGet();
    }

    public void increaseInvalidIgmpMsgReceived() {
        invalidIgmpMsgReceived.incrementAndGet();
    }

    public void increaseTotalMsgReceived() {
        totalMsgReceived.incrementAndGet();
    }

    public Long getValidIgmpPacketCounter() {
        return validIgmpPacketCounter.get();
    }

    public void increaseValidIgmpPacketCounter() {
        validIgmpPacketCounter.incrementAndGet();
    }

    public Long getCurrentGrpNumCounter() {
        return currentGrpNumCounter.get();
    }

    public void increaseCurrentGrpNumCounter() {
        currentGrpNumCounter.incrementAndGet();
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

    public void increaseIgmpValidChecksumCounter() {
        igmpValidChecksumCounter.incrementAndGet();
    }

    public Long getUnconfiguredGroupCounter() {
        return unconfiguredGroupCounter.get();
    }

    public void increaseUnconfiguredGroupCounter() {
        unconfiguredGroupCounter.incrementAndGet();
    }

    public Long getFailJoinReqUnknownMulticastIpCounter() {
        return failJoinReqUnknownMulticastIpCounter.get();
    }

    public void increaseFailJoinReqUnknownMulticastIpCounter() {
        failJoinReqUnknownMulticastIpCounter.incrementAndGet();
    }

    public Long getFailJoinReqInsuffPermissionAccessCounter() {
        return failJoinReqInsuffPermissionAccessCounter.get();
    }

    public void increaseFailJoinReqInsuffPermissionAccessCounter() {
        failJoinReqInsuffPermissionAccessCounter.incrementAndGet();
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

    public void increaseReportsRxWithWrongModeCounter() {
        reportsRxWithWrongModeCounter.incrementAndGet();
    }

    public Long getInvalidIgmpLength() {
        return invalidIgmpLength.get();
    }

    public void increaseInvalidIgmpLength() {
        invalidIgmpLength.incrementAndGet();
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

    public void increaseIgmpGrpSpecificMembershipQuery() {
        igmpGrpSpecificMembershipQuery.incrementAndGet();
    }

    public void increaseIgmpGrpAndSrcSpecificMembershipQuery() {
        igmpGrpAndSrcSpecificMembershipQuery.incrementAndGet();
    }

}
