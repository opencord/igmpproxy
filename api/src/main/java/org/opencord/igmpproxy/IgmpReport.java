/*
 * Copyright 2017-2021 Open Networking Foundation
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

import com.google.common.base.MoreObjects;
import org.onlab.packet.IGMPMembership;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;

import java.util.Objects;

/**
 * Identification of internal igmp report packet.
 */
public final class IgmpReport {
    private IGMPMembership igmpMembership;
    private VlanId vlanId;
    private ConnectPoint connectPoint;
    private byte igmpType;
    private String packetId;

    /**
     * Constructor for serializer.
     */
    private IgmpReport() {
    }

    private IgmpReport(IGMPMembership igmpMembership, VlanId vlanId,
                       ConnectPoint connectPoint, byte igmpType, String packetId) {
        this.igmpMembership = igmpMembership;
        this.vlanId = vlanId;
        this.connectPoint = connectPoint;
        this.igmpType = igmpType;
        this.packetId = packetId;
    }

    /**
     * Creates new igmp report packet.
     *
     * @param igmpMembership igmp membership
     * @param vlanId         vlan id
     * @param connectPoint   connect point
     * @param igmpType       igmp type
     * @param packetId       packet process entry number
     * @return new identification of group-member
     */
    public static IgmpReport of(IGMPMembership igmpMembership, VlanId vlanId,
                                ConnectPoint connectPoint, byte igmpType, String packetId) {
        return new IgmpReport(igmpMembership, vlanId, connectPoint, igmpType, packetId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("igmpMembership", igmpMembership)
                .add("vlanId", vlanId)
                .add("connectPoint", connectPoint)
                .add("igmpType", igmpType)
                .add("packetId", packetId).toString();
    }

    /**
     * Get igmp membership.
     *
     * @return igmp membership
     */
    public IGMPMembership getIgmpMembership() {
        return igmpMembership;
    }

    /**
     * Get vlan id.
     *
     * @return vlan id
     */
    public VlanId getVlanId() {
        return vlanId;
    }

    /**
     * Get connect point.
     *
     * @return connect point
     */
    public ConnectPoint getConnectPoint() {
        return connectPoint;
    }

    /**
     * Get igmp type.
     *
     * @return igmp type
     */
    public byte getIgmpType() {
        return igmpType;
    }

    /**
     * Get packet process entry number.
     *
     * @return packet id
     */
    public String getPacketId() {
        return packetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IgmpReport that = (IgmpReport) o;
        return Objects.equals(igmpMembership, that.igmpMembership) &&
                Objects.equals(vlanId, that.vlanId) &&
                Objects.equals(connectPoint, that.connectPoint) &&
                Objects.equals(igmpType, that.igmpType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(igmpMembership.getGaddr(), connectPoint);
    }
}

