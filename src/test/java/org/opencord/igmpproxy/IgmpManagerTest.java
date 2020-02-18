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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.junit.TestUtils;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.event.DefaultEventSinkRegistry;
import org.onosproject.event.Event;
import org.onosproject.event.EventDeliveryService;
import org.onosproject.event.EventSink;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;

import static org.junit.Assert.*;

import static com.google.common.base.Preconditions.checkState;
public class IgmpManagerTest extends IgmpManagerBase {

    private static final int WAIT_TIMEOUT = 1000;

    private IgmpManager igmpManager;

    private IgmpStatisticsManager igmpStatisticsManager;

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
        TestUtils.setField(igmpStatisticsManager, "eventDispatcher", new TestEventDispatcher());
        igmpStatisticsManager.activate();
        igmpManager.igmpStatisticsManager = this.igmpStatisticsManager;
        // By default - we send query messages
        SingleStateMachine.sendQuery = true;
    }

    // Tear Down the IGMP application.
    @After
    public void tearDown() {
        igmpManager.deactivate();
        IgmpManager.groupMemberMap.clear();
        StateMachine.clearMap();
    }

    // Checking the Default value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisDefaultValue() {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();
        assertFalse(IgmpManager.isIgmpOnPodBasis());
    }


    // Checking the value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisTrueValue() {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(true);
        igmpManager.activate();
        assertTrue(IgmpManager.isIgmpOnPodBasis());
    }

    // Testing the flow of packet when isIgmpOnPodBasis value is false.
    @Test
    public void testIgmpOnPodBasisDefaultValue() throws InterruptedException {
        // We need to count join messages sent on the upstream
        SingleStateMachine.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();

        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);
        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);
        // Emitted packet is stored in list savedPackets
        assertNotNull(savedPackets);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }

        assertNotNull(savedPackets);
        assertEquals(1, savedPackets.size());
        // Sending the second packet with same group ip address
        sendPacket(secondPacket);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }
        // Emitted packet is stored in list savedPackets as shouldSendJoin flag is true
        assertEquals(2, savedPackets.size());
    }

    // Testing IGMP_ON_POD_BASIS value by sending two packets.
    @Test
    public void testIgmpOnPodBasisTrueValue() throws InterruptedException {
        // We need to count join messages
        SingleStateMachine.sendQuery = false;

        igmpManager.networkConfig = new TestNetworkConfigRegistry(true);
        igmpManager.activate();

        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);
        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);
        // Emitted packet is stored in list savedPackets
        synchronized (savedPackets) {
          savedPackets.wait(WAIT_TIMEOUT);
        }
        assertNotNull(savedPackets);
        assertEquals(1, savedPackets.size());
        // Sending the second packet with same group ip address which will not be emitted
        // shouldSendJoin flag will be false.
        sendPacket(secondPacket);
        synchronized (savedPackets) {
            savedPackets.wait(WAIT_TIMEOUT);
        }
        assertEquals(1, savedPackets.size());
    }

    public static class TestEventDispatcher extends DefaultEventSinkRegistry implements EventDeliveryService {

        @Override
        @SuppressWarnings("unchecked")
        public synchronized void post(Event event) {
            EventSink sink = getSink(event.getClass());
            checkState(sink != null, "No sink for event %s", event);
            sink.process(event);
        }

        @Override
        public void setDispatchTimeLimit(long millis) {
        }

        @Override
        public long getDispatchTimeLimit() {
            return 0;
        }
    }
}
