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
package org.opencord.igmpproxy.impl;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IGMP;
import org.onlab.packet.IGMP.IGMPv2;
import org.onlab.packet.IGMP.IGMPv3;
import org.onlab.packet.IGMPMembership;
import org.onlab.packet.IGMPQuery;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketService;
import org.opencord.igmpproxy.IgmpLeadershipService;
import org.opencord.igmpproxy.IgmpStatisticType;
import org.opencord.igmpproxy.IgmpStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 *  Message encode and send interface for igmpproxy.
 */
public final class IgmpSender {
    static final String V3_REPORT_ADDRESS = "224.0.0.22";
    static final String V2_LEAVE_DST = "224.0.0.2";
    static final String MAC_ADDRESS = "DE:AD:BE:EF:BA:11";
    static final short DEFAULT_MVLAN = 4000;
    static final byte DEFAULT_COS = 7;
    static final int DEFAULT_MEX_RESP = 10;
    static final byte[] RA_BYTES = {(byte) 0x94, (byte) 0x04, (byte) 0x00, (byte) 0x00};

    private static IgmpSender instance = null;
    private PacketService packetService;
    private IgmpLeadershipService igmpLeadershipService;
    private IgmpStatisticsService igmpStatisticsService;
    private boolean withRAUplink = true;
    private boolean withRADownlink = false;
    private short mvlan = DEFAULT_MVLAN;
    private short mvlanInner = VlanId.NONE.toShort();
    private byte igmpCos = DEFAULT_COS;
    private byte igmpUniCos = DEFAULT_COS;
    private int maxResp = DEFAULT_MEX_RESP;
    private Logger log = LoggerFactory.getLogger(getClass());

    private IgmpSender(PacketService packetService, IgmpStatisticsService igmpStatisticsService) {
        this.packetService = packetService;
        this.igmpStatisticsService = igmpStatisticsService;
    }

    public static void init(PacketService packetService, IgmpStatisticsService igmpStatisticsService) {
        instance = new IgmpSender(packetService, igmpStatisticsService);
    }

    public static IgmpSender getInstance() {
        return instance;
    }

    public void setWithRAUplink(boolean withRaUplink) {
        this.withRAUplink = withRaUplink;
    }

    public void setWithRADownlink(boolean withRADownlink) {
        this.withRADownlink = withRADownlink;
    }

    public void setMvlan(short mvlan) {
        this.mvlan = mvlan;
    }

    public void setMvlanInner(short mvlanInner) {
        this.mvlanInner = mvlanInner;
    }

    public void setIgmpCos(byte igmpCos) {
        this.igmpCos = igmpCos;
    }
    public void setIgmpUniCos(byte igmpUniCos) {
        this.igmpUniCos = igmpUniCos;
    }

    public void setMaxResp(int maxResp) {
        this.maxResp = maxResp;
    }

    public Ethernet buildIgmpV3Join(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.CHANGE_TO_EXCLUDE_MODE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership,
                sourceIp, false, mvlan, mvlanInner, igmpCos);
    }

    public Ethernet buildIgmpV2Join(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        return buildIgmpPacket(IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT, groupIp, igmpMembership,
                sourceIp, true, mvlan, mvlanInner, igmpCos);
    }

    public Ethernet buildIgmpV2ResponseQuery(Ip4Address groupIp, Ip4Address sourceIp) {
        return buildIgmpV2Join(groupIp, sourceIp);
    }

    public Ethernet buildIgmpV3ResponseQuery(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.MODE_IS_EXCLUDE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership,
                sourceIp, false, mvlan, mvlanInner, igmpCos);
    }

    public Ethernet buildIgmpV3Leave(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.CHANGE_TO_INCLUDE_MODE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership,
                sourceIp, false, mvlan, mvlanInner, igmpCos);
    }

    public Ethernet buildIgmpV2Leave(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        return buildIgmpPacket(IGMP.TYPE_IGMPV2_LEAVE_GROUP, groupIp, igmpMembership,
                sourceIp, true, mvlan, mvlanInner, igmpCos);
    }

    public Ethernet buildIgmpV2Query(Ip4Address groupIp, Ip4Address sourceIp, short vlan) {
        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_QUERY, groupIp, null,
                sourceIp, true, vlan, VlanId.NONE.toShort(), igmpUniCos);
    }

    public Ethernet buildIgmpV3Query(Ip4Address groupIp, Ip4Address sourceIp, short vlan) {
        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_QUERY, groupIp, null,
                sourceIp, false, vlan, VlanId.NONE.toShort(), igmpUniCos);
    }

    protected Ethernet buildIgmpPacket(byte type, Ip4Address groupIp, IGMPMembership igmpMembership,
                                     Ip4Address sourceIp, boolean isV2Query, short vlan,
                                       short innerVlan, byte igmpCos) {

        IGMP igmpPacket;
        if (isV2Query) {
            igmpPacket = new IGMP.IGMPv2();
        } else {
            igmpPacket = new IGMP.IGMPv3();
        }

        IPv4 ip4Packet = new IPv4();
        Ethernet ethPkt = new Ethernet();
        ethPkt.setPad(true);

        igmpPacket.setIgmpType(type);

        switch (type) {
            case IGMP.TYPE_IGMPV3_MEMBERSHIP_QUERY:
                igmpPacket.setMaxRespCode((byte) (maxResp * 10));
                IGMPQuery igmpQuery = new IGMPQuery(groupIp, 0);

                igmpPacket.addGroup(igmpQuery);
                ip4Packet.setDestinationAddress(groupIp.toInt());
                if (withRADownlink) {
                    ip4Packet.setOptions(RA_BYTES);
                }
                break;

            case IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT:
                if (igmpMembership == null) {
                    log.debug("Igmp membership is not found. igmp-type {} ", type);
                    return null;
                }
                igmpPacket.addGroup(igmpMembership);
                ip4Packet.setDestinationAddress(Ip4Address.valueOf(V3_REPORT_ADDRESS).toInt());

                if (withRAUplink) {
                    ip4Packet.setOptions(RA_BYTES);
                }
                break;

            case IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT:
            case IGMP.TYPE_IGMPV2_LEAVE_GROUP:
                if (igmpMembership == null) {
                    log.debug("Igmp membership is not found. igmp-type {} ", type);
                    return null;
                }
                igmpPacket.addGroup(igmpMembership);
                int dst = (type == IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT ?
                        groupIp.toInt() :
                        Ip4Address.valueOf(V2_LEAVE_DST).toInt());
                ip4Packet.setDestinationAddress(dst);
                break;
            default:
                log.debug("Unknown igmp type: {} ", type);
                igmpStatisticsService.increaseStat(IgmpStatisticType.UNKNOWN_IGMP_TYPE_PACKETS_RX_COUNTER);
                return null;
        }

        igmpPacket.setParent(ip4Packet);
        ip4Packet.setSourceAddress(sourceIp.toInt());
        ip4Packet.setProtocol(IPv4.PROTOCOL_IGMP);
        ip4Packet.setPayload(igmpPacket);
        ip4Packet.setParent(ethPkt);
        ip4Packet.setTtl((byte) 0x78);

        ethPkt.setDestinationMACAddress(multiaddToMac(ip4Packet.getDestinationAddress()));
        ethPkt.setSourceMACAddress(MAC_ADDRESS);
        ethPkt.setEtherType(Ethernet.TYPE_IPV4);
        ethPkt.setPayload(ip4Packet);
        ethPkt.setVlanID(vlan);
        ethPkt.setPriorityCode(igmpCos);

        if (innerVlan != VlanId.NONE.toShort()) {
            ethPkt.setQinQTPID(Ethernet.TYPE_VLAN);
            ethPkt.setQinQVID(vlan);
            ethPkt.setVlanID(innerVlan);
            ethPkt.setQinQPriorityCode(igmpCos);
        }

        return ethPkt;
    }

    private MacAddress multiaddToMac(int multiaddress) {
        byte[] b = new byte[3];
        b[0] = (byte) (multiaddress & 0xff);
        b[1] = (byte) (multiaddress >> 8 & 0xff);
        b[2] = (byte) (multiaddress >> 16 & 0x7f);
        byte[] macByte = {0x01, 0x00, 0x5e, b[2], b[1], b[0]};

        MacAddress mac = MacAddress.valueOf(macByte);
        return mac;
    }

    public void sendIgmpPacketUplink(Ethernet ethPkt, DeviceId deviceId, PortNumber upLinkPort) {
        if (IgmpManager.connectPointMode) {
            if (IgmpManager.connectPoint == null) {
                log.warn("cannot find a connectPoint to send the packet uplink");
                return;
            }
            sendIgmpPacket(ethPkt, IgmpManager.connectPoint.deviceId(), IgmpManager.connectPoint.port());
        } else {
            sendIgmpPacket(ethPkt, deviceId, upLinkPort);
        }
    }

    public void sendIgmpPacket(Ethernet ethPkt, DeviceId deviceId, PortNumber portNumber) {
        if (log.isTraceEnabled()) {
            log.trace("Emitting on {}/{} outbound IGMP packet {}", deviceId, portNumber, ethPkt);
        }

        IPv4 ipv4Pkt = (IPv4) ethPkt.getPayload();
        IGMP igmp = (IGMP) ipv4Pkt.getPayload();
        // We are checking the length of packets. Right now the counter value will be 0 because of internal translation
        // As packet length will always be valid
        // This counter will be useful in future if we change the procedure to generate the packets.
        if ((igmp.getIgmpType() == IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT
             || igmp.getIgmpType() == IGMP.TYPE_IGMPV2_LEAVE_GROUP) && igmp.serialize().length < IGMPv2.HEADER_LENGTH) {
                 igmpStatisticsService.increaseStat(IgmpStatisticType.INVALID_IGMP_LENGTH);
        } else if (igmp.getIgmpType() == IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT
            && igmp.serialize().length < IGMPv3.MINIMUM_HEADER_LEN) {
                 igmpStatisticsService.increaseStat(IgmpStatisticType.INVALID_IGMP_LENGTH);
        }
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(portNumber).build();
        OutboundPacket packet = new DefaultOutboundPacket(deviceId,
                treatment, ByteBuffer.wrap(ethPkt.serialize()));
        igmpStatisticsService.increaseStat(IgmpStatisticType.VALID_IGMP_PACKET_COUNTER);
        packetService.emit(packet);
        if (log.isTraceEnabled()) {
            log.trace("Emitted on {}/{} outbound IGMP packet {}", deviceId, portNumber, packet);
        }

    }
}
