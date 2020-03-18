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
package org.opencord.igmpproxy;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IGMP;
import org.onlab.packet.IGMP.IGMPv2;
import org.onlab.packet.IGMP.IGMPv3;
import org.onlab.packet.IGMPMembership;
import org.onlab.packet.IGMPQuery;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 *  Message encode and send interface for igmpproxy.
 */
public final class IgmpSender {
    static final String V3_REPORT_ADDRESS = "224.0.0.22";
    static final String MAC_ADDRESS = "DE:AD:BE:EF:BA:11";
    static final short DEFAULT_MVLAN = 4000;
    static final byte DEFAULT_COS = 7;
    static final int DEFAULT_MEX_RESP = 10;
    static final byte[] RA_BYTES = {(byte) 0x94, (byte) 0x04, (byte) 0x00, (byte) 0x00};

    private static IgmpSender instance = null;
    private PacketService packetService;
    private MastershipService mastershipService;
    private IgmpStatisticsService igmpStatisticsService;
    private boolean withRAUplink = true;
    private boolean withRADownlink = false;
    private short mvlan = DEFAULT_MVLAN;
    private byte igmpCos = DEFAULT_COS;
    private int maxResp = DEFAULT_MEX_RESP;
    private Logger log = LoggerFactory.getLogger(getClass());

    private IgmpSender(PacketService packetService, MastershipService mastershipService,
             IgmpStatisticsService igmpStatisticsService) {
        this.packetService = packetService;
        this.mastershipService = mastershipService;
        this.igmpStatisticsService = igmpStatisticsService;
    }

    public static void init(PacketService packetService, MastershipService mastershipService,
            IgmpStatisticsService igmpStatisticsService) {
        instance = new IgmpSender(packetService, mastershipService, igmpStatisticsService);
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

    public void setIgmpCos(byte igmpCos) {
        this.igmpCos = igmpCos;
    }

    public void setMaxResp(int maxResp) {
        this.maxResp = maxResp;
    }

    public Ethernet buildIgmpV3Join(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.CHANGE_TO_EXCLUDE_MODE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership, sourceIp, false);
    }

    public Ethernet buildIgmpV3ResponseQuery(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.MODE_IS_EXCLUDE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership, sourceIp, false);
    }

    public Ethernet buildIgmpV3Leave(Ip4Address groupIp, Ip4Address sourceIp) {
        IGMPMembership igmpMembership = new IGMPMembership(groupIp);
        igmpMembership.setRecordType(IGMPMembership.CHANGE_TO_INCLUDE_MODE);

        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT, groupIp, igmpMembership, sourceIp, false);
    }

    public Ethernet buildIgmpV2Query(Ip4Address groupIp, Ip4Address sourceIp) {
        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_QUERY, groupIp, null, sourceIp, true);
    }

    public Ethernet buildIgmpV3Query(Ip4Address groupIp, Ip4Address sourceIp) {
        return buildIgmpPacket(IGMP.TYPE_IGMPV3_MEMBERSHIP_QUERY, groupIp, null, sourceIp, false);
    }

    private Ethernet buildIgmpPacket(byte type, Ip4Address groupIp, IGMPMembership igmpMembership,
                                     Ip4Address sourceIp, boolean isV2Query) {

        IGMP igmpPacket;
        if (isV2Query) {
            igmpPacket = new IGMP.IGMPv2();
        } else {
            igmpPacket = new IGMP.IGMPv3();
        }

        IPv4 ip4Packet = new IPv4();
        Ethernet ethPkt = new Ethernet();

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
                    return null;
                }
                igmpPacket.addGroup(igmpMembership);
                if (type == IGMP.TYPE_IGMPV3_MEMBERSHIP_REPORT) {
                    ip4Packet.setDestinationAddress(Ip4Address.valueOf(V3_REPORT_ADDRESS).toInt());
                } else {
                    ip4Packet.setDestinationAddress(groupIp.toInt());
                }
                if (withRAUplink) {
                    ip4Packet.setOptions(RA_BYTES);
                }
                break;

            case IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT:
            case IGMP.TYPE_IGMPV2_LEAVE_GROUP:
                return null;
            default:
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
        ethPkt.setVlanID(mvlan);
        ethPkt.setPriorityCode(igmpCos);

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
        if (!mastershipService.isLocalMaster(deviceId)) {
            return;
        }

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
        if (!mastershipService.isLocalMaster(deviceId)) {
            return;
        }

        IPv4 ipv4Pkt = (IPv4) ethPkt.getPayload();
        IGMP igmp = (IGMP) ipv4Pkt.getPayload();
        if (igmp.getIgmpType() == IGMP.TYPE_IGMPV2_MEMBERSHIP_REPORT
             || igmp.getIgmpType() == IGMP.TYPE_IGMPV2_LEAVE_GROUP) {
             if (igmp.serialize().length < IGMPv2.HEADER_LENGTH) {
                 igmpStatisticsService.getIgmpStats().increaseInvalidIgmpLength();
             }
        } else {
                 if (igmp.serialize().length < IGMPv3.MINIMUM_HEADER_LEN) {
                     igmpStatisticsService.getIgmpStats().increaseInvalidIgmpLength();
                 }
        }
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(portNumber).build();
        OutboundPacket packet = new DefaultOutboundPacket(deviceId,
                treatment, ByteBuffer.wrap(ethPkt.serialize()));
        igmpStatisticsService.getIgmpStats().increaseValidIgmpPacketCounter();
        packetService.emit(packet);

    }
}
