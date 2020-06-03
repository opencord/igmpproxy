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

/**
 * Types of igmp-statistics.
 */
public enum IgmpStatisticType {
    /**
     * Join request.
     */
    IGMP_JOIN_REQ,
    /**
     * Success re-join.
     */
    IGMP_SUCCESS_JOIN_RE_JOIN_REQ,
    /**
     * Fail join request.
     */
    IGMP_FAIL_JOIN_REQ,
    /**
     * Leave request.
     */
    IGMP_LEAVE_REQ,
    /**
     * Igmp disconnect.
     */
    IGMP_DISCONNECT,
    /**
     * Igmp v3 membership query.
     */
    IGMP_V3_MEMBERSHIP_QUERY,
    /**
     * Igmp v1 membership report.
     */
    IGMP_V1_MEMBERSHIP_REPORT,
    /**
     * Igmp v2 membeship report.
     */
    IGMP_V2_MEMBERSHIP_REPORT,
    /**
     * Igmp v3 membeship report.
     */
    IGMP_V3_MEMBERSHIP_REPORT,
    /**
     * Igmp v2 leave group.
     */
    IGMP_V2_LEAVE_GROUP,
    /**
     * Received total message.
     */
    TOTAL_MSG_RECEIVED,
    /**
     * Received igmp-message.
     */
    IGMP_MSG_RECEIVED,
    /**
     * Received invalid igmp-message.
     */
    INVALID_IGMP_MSG_RECEIVED,
    /**
     * Unknown igmp rx packets counter.
     */
    UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER,
    /**
     * Wrong mode rx counter reports.
     */
    REPORTS_RX_WITH_WRONG_MODE_COUNTER,
    /**
     * Insuff permission access counter of fail join request.
     */
    FAIL_JOIN_REQ_INSUFF_PERMISSION_ACCESS_COUNTER,
    /**
     * Unknown mcast ip counter of fail join.
     */
    FAIL_JOIN_REQ_UNKNOWN_MULTICAST_IP_COUNTER,
    /**
     * Unconfigured group counter.
     */
    UNCONFIGURED_GROUP_COUNTER,
    /**
     * Valid igmp packet counter.
     */
    VALID_IGMP_PACKET_COUNTER,
    /**
     * Igmp channel join counter.
     */
    IGMP_CHANNEL_JOIN_COUNTER,
    /**
     * Current grp number counter.
     */
    CURRENT_GRP_NUMBER_COUNTER,
    /**
     * Igmp valid checksum counter.
     */
    IGMP_VALID_CHECKSUM_COUNTER,
    /**
     * Invalid igmp length.
     */
    INVALID_IGMP_LENGTH,
    /**
     * Igmp general membership query.
     */
    IGMP_GENERAL_MEMBERSHIP_QUERY,
    /**
     * Igmp grp specific membership query.
     */
    IGMP_GRP_SPECIFIC_MEMBERSHIP_QUERY,
    /**
     * Igmp grp and src spesific membership query.
     */
    IGMP_GRP_AND_SRC_SPESIFIC_MEMBERSHIP_QUERY;
}
