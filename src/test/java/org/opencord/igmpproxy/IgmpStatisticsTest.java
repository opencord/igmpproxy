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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onlab.junit.TestTools.assertAfter;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.junit.TestUtils;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;

import com.google.common.collect.Lists;

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
        igmpManager.coreService = new CoreServiceAdapter();
        igmpManager.mastershipService = new MockMastershipService();
        igmpManager.flowObjectiveService = new FlowObjectiveServiceAdapter();
        igmpManager.deviceService = new MockDeviceService();
        igmpManager.packetService = new MockPacketService();
        igmpManager.flowRuleService = new FlowRuleServiceAdapter();
        igmpManager.multicastService = new TestMulticastRouteService();
        igmpManager.sadisService = new MockSadisService();
        igmpStatisticsManager = new IgmpStatisticsManager();
        igmpStatisticsManager.cfgService = new MockCfgService();
        igmpStatisticsManager.addListener(mockListener);
        TestUtils.setField(igmpStatisticsManager, "eventDispatcher", new TestEventDispatcher());
        igmpStatisticsManager.activate(new MockComponentContext());
        igmpManager.igmpStatisticsManager = this.igmpStatisticsManager;
        // By default - we send query messages
        SingleStateMachine.sendQuery = true;
    }

    // Tear Down the IGMP application.
    @After
    public void tearDown() {
        igmpStatisticsManager.removeListener(mockListener);
        igmpStatisticsManager.deactivate();
        IgmpManager.groupMemberMap.clear();
        StateMachine.clearMap();
    }

    //Test Igmp Statistics.
    @Test
    public void testIgmpStatistics() throws InterruptedException {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();
        //IGMPv3 Join
        Ethernet igmpv3MembershipReportPkt = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        sendPacket(igmpv3MembershipReportPkt, true);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }
        //Leave
        Ethernet igmpv3LeavePkt = IgmpSender.getInstance().buildIgmpV3Leave(GROUP_IP, SOURCE_IP_OF_A);
        sendPacket(igmpv3LeavePkt, true);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }

        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
            assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getTotalMsgReceived().longValue()));
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpJoinReq().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getIgmpv3MembershipReport().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpSuccessJoinRejoinReq().longValue());
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getUnconfiguredGroupCounter().longValue());
        assertTrue(igmpStatisticsManager.getIgmpStats().getValidIgmpPacketCounter().longValue() > 1);
        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpChannelJoinCounter().longValue());

        assertEquals((long) 1, igmpStatisticsManager.getIgmpStats().getIgmpLeaveReq().longValue());
        assertEquals((long) 2, igmpStatisticsManager.getIgmpStats().getIgmpMsgReceived().longValue());

    }

    //Test packet with Unknown Multicast IpAddress
    @Test
    public void testIgmpUnknownMulticastIpAddress() throws InterruptedException {
        SingleStateMachine.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket =
             IgmpSender.getInstance().buildIgmpV3Join(Ip4Address.valueOf("124.0.0.0"), SOURCE_IP_OF_A);
        // Sending first packet
        sendPacket(firstPacket, false);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
        assertEquals((long) 1,
             igmpStatisticsManager.getIgmpStats().getFailJoinReqUnknownMulticastIpCounter().longValue()));
    }

    //Test Igmp Statistics.
    @Test
    public void testIgmpQueryStatistics() throws InterruptedException {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        //IGMPV3Query
        Ethernet igmpv3MembershipQueryPkt = IgmpSender.getInstance().buildIgmpV3Query(GROUP_IP, SOURCE_IP_OF_A);
        sendQueryPacket(igmpv3MembershipQueryPkt);

        Ethernet igmpv3MembershipQueryPkt1 =
              IgmpSender.getInstance().buildIgmpV3Query(Ip4Address.valueOf(0), SOURCE_IP_OF_A);
        sendQueryPacket(igmpv3MembershipQueryPkt1);
        assertAfter(WAIT_TIMEOUT, WAIT_TIMEOUT * 2, () ->
        assertTrue(igmpStatisticsManager.getIgmpStats()
            .getIgmpGrpAndSrcSpecificMembershipQuery().longValue() > 0));
        assertEquals(igmpStatisticsManager.getIgmpStats()
            .getIgmpGeneralMembershipQuery().longValue(), 1);
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

    public class MockIgmpStatisticsEventListener implements IgmpStatisticsEventListener {
        protected List<IgmpStatisticsEvent> events = Lists.newArrayList();

        @Override
        public void event(IgmpStatisticsEvent event) {
            events.add(event);
        }

    }
}
