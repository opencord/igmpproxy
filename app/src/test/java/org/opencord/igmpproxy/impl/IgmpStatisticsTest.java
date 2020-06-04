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

import static org.junit.Assert.assertEquals;
import static org.onlab.junit.TestTools.assertAfter;

import java.util.List;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.junit.TestUtils;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.VlanId;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;

import org.opencord.igmpproxy.IgmpStatisticsEvent;

import com.google.common.collect.Lists;
import org.opencord.igmpproxy.IgmpStatisticsEventListener;

/**
 * Set of tests of the ONOS application component for IGMP Statistics.
 */
public class IgmpStatisticsTest extends IgmpManagerBase {

    private static final int WAIT_TIMEOUT = 500;

    private IgmpManager igmpManager;

    private IgmpStatisticsManager igmpStatisticsManager;

    private MockIgmpStatisticsEventListener mockListener = new MockIgmpStatisticsEventListener();

    // Set up the IGMP application.
    @Before
    public void setUp() {
        igmpManager = new IgmpManager();
        igmpManager.igmpLeadershipService = new TestIgmpLeaderShipService();
        igmpManager.coreService = new CoreServiceAdapter();
        igmpManager.mastershipService = new MockMastershipService();
        igmpManager.flowObjectiveService = new FlowObjectiveServiceAdapter();
        igmpManager.deviceService = new MockDeviceService();
        igmpManager.packetService = new MockPacketService();
        igmpManager.flowRuleService = new FlowRuleServiceAdapter();
        igmpManager.multicastService = new TestMulticastRouteService();
        igmpManager.sadisService = new MockSadisService();
        igmpManager.groupMemberStore = new TestGroupMemberStoreService();
        StateMachineManager stateMachineService = new StateMachineManager();
        stateMachineService.stateMachineStore = new TestStateMachineStoreService(Maps.newConcurrentMap());
        stateMachineService.activate(new MockComponentContext());
        igmpManager.stateMachineService = stateMachineService;
        igmpStatisticsManager = new IgmpStatisticsManager();
        igmpStatisticsManager.cfgService = new MockCfgService();
        igmpStatisticsManager.addListener(mockListener);
        TestUtils.setField(igmpStatisticsManager, "eventDispatcher", new TestEventDispatcher());
        igmpStatisticsManager.activate(new MockComponentContext());
        igmpManager.igmpStatisticsManager = this.igmpStatisticsManager;
        // By default - we send query messages
        StateMachineManager.sendQuery = true;
    }

    // Tear Down the IGMP application.
    @After
    public void tearDown() {
        igmpStatisticsManager.removeListener(mockListener);
        igmpStatisticsManager.deactivate();
        igmpManager.stateMachineService.clearAllMaps();
    }

    //Test Igmp Statistics.
    @Test
    public void testIgmpStatistics() throws InterruptedException {
        StateMachineManager.sendQuery = false;
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        //IGMPv3 Join
        flagForPacket = false;
        Ethernet igmpv3MembershipReportPkt = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        sendPacket(igmpv3MembershipReportPkt);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }
        //Leave
        flagForPacket = false;
        Ethernet igmpv3LeavePkt = IgmpSender.getInstance().buildIgmpV3Leave(GROUP_IP, SOURCE_IP_OF_A);
        sendPacket(igmpv3LeavePkt);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }

        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getTotalMsgReceived().longValue()));
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpJoinReq().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getIgmpv3MembershipReport().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpSuccessJoinRejoinReq().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getUnconfiguredGroupCounter().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getValidIgmpPacketCounter().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpChannelJoinCounter().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpLeaveReq().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getIgmpMsgReceived().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getIgmpValidChecksumCounter().longValue());

    }

    //Test packet with Unknown Multicast IpAddress
    @Test
    public void testIgmpUnknownMulticastIpAddress() throws InterruptedException {
        StateMachineManager.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket =
                IgmpSender.getInstance().buildIgmpV3Join(UNKNOWN_GRP_IP, SOURCE_IP_OF_A);
        // Sending first packet
        sendPacket(firstPacket);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals((long) 1,
                        igmpStatisticsManager.getIgmpStats().getFailJoinReqUnknownMulticastIpCounter().longValue()));
    }

    //Test Igmp Query Statistics.
    @Test
    public void testIgmpQueryStatistics() throws InterruptedException {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        flagForQueryPacket = true;
        //IGMPV3 Group Specific Membership Query packet
        Ethernet igmpv3MembershipQueryPkt = IgmpSender.getInstance().
                buildIgmpV3Query(GROUP_IP, SOURCE_IP_OF_A, VlanId.MAX_VLAN);
        sendPacket(igmpv3MembershipQueryPkt);

        //IGMPV3 General Membership Query packet
        Ethernet igmpv3MembershipQueryPkt1 =
                IgmpSender.getInstance().buildIgmpV3Query(Ip4Address.valueOf(0), SOURCE_IP_OF_A, VlanId.MAX_VLAN);
        sendPacket(igmpv3MembershipQueryPkt1);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals(igmpStatisticsManager.getIgmpStats()
                        .getIgmpGrpAndSrcSpecificMembershipQuery().longValue(), 1));
        assertEquals(igmpStatisticsManager.getIgmpStats()
                .getIgmpGeneralMembershipQuery().longValue(), 1);
        assertEquals(igmpStatisticsManager.getIgmpStats()
                .getCurrentGrpNumCounter().longValue(), 1);
    }

    //Test Events
    @Test
    public void testIgmpStatisticsEvent() {
        final int waitEventGeneration = igmpStatisticsManager.statisticsGenerationPeriodInSeconds * 1000;
        //assert that event listened as the app activates
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals(mockListener.events.size(), 1));

        assertAfter(waitEventGeneration / 2, waitEventGeneration, () ->
                assertEquals(mockListener.events.size(), 2));

        for (IgmpStatisticsEvent event : mockListener.events) {
            assertEquals(event.type(), IgmpStatisticsEvent.Type.STATS_UPDATE);
        }
    }

    //Test packet with Unknown Wrong Membership mode
    @Test
    public void testWrongIgmpPacket() throws InterruptedException {
        StateMachineManager.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket = buildWrongIgmpPacket(GROUP_IP, SOURCE_IP_OF_A);
        // Sending first packet
        sendPacket(firstPacket);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals((long) 1,
                        igmpStatisticsManager.getIgmpStats().getReportsRxWithWrongModeCounter().longValue()));
    }

    //Test packet with Unknown IGMP type.
    @Test
    public void testUnknownIgmpPacket() throws InterruptedException {
        StateMachineManager.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket = buildUnknownIgmpPacket(GROUP_IP, SOURCE_IP_OF_A);
        // Sending first packet
        sendPacket(firstPacket);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals((long) 1,
                        igmpStatisticsManager.getIgmpStats().getUnknownIgmpTypePacketsRxCounter().longValue()));
    }

    //Test packet with Insufficient Permission.
    @Test
    public void testSufficientPermission() throws InterruptedException {
        StateMachineManager.sendQuery = false;

        flagForPermission = true;
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        // Sending first packet
        sendPacket(firstPacket);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
                assertEquals((long) 1,
                        igmpStatisticsManager.getIgmpStats()
                                .getFailJoinReqInsuffPermissionAccessCounter().longValue()));
    }

    public class MockIgmpStatisticsEventListener implements IgmpStatisticsEventListener {
        protected List<IgmpStatisticsEvent> events = Lists.newArrayList();

        @Override
        public void event(IgmpStatisticsEvent event) {
            events.add(event);
        }

    }
}
