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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;

public class IgmpManagerTest extends IgmpManagerBase {


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

    }

    // Tear Down the IGMP application.
    @After
    public void tearDown() {
        igmpManager.deactivate();
    }

    // Checking the default value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisDefaultValue() {
       igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
       igmpManager.activate();
       assertEquals(IgmpManager.isIgmpOnPodBasis(), false);
    }

    // Checking the value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisValue() {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(true);
        igmpManager.activate();
        assertEquals(IgmpManager.isIgmpOnPodBasis(), true);
    }

    // Testing IGMP_ON_POD_BASIS value by sending two packets.
    @Test
    public void testIgmpOnPodBasisTrueValue() throws InterruptedException {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(true);
        igmpManager.activate();
        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);

        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);

        // Emitted packet is stored in list savedPackets
        assertNotNull(savedPackets);
        synchronized (savedPackets) {
            savedPackets.wait();
        }
        // Sending the second packet with same group ip address and here shouldSendJoin
        // flag will be false.
        sendPacket(secondPacket);
        // Second packet will not be emitted and hence it will not be present in our savedPackets list
        // SavedPackets list will contain the packet emitted from deviceId 1 and hence
        // our shouldSendJoin flag is true
        assertEquals(outboundPacket.toString().contains("of:1"), true);
        assertEquals(outboundPacket.toString().contains("of:2"), false);

    }

    // Testing the flow of packet when isIgmpOnPodBasis value is false.
    @Test
    public void testIgmpOnPodBasisFalseValue() throws InterruptedException {
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
        igmpManager.activate();
        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);

        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);

        // Emitted packet is stored in list savedPackets
        assertNotNull(savedPackets);
        synchronized (savedPackets) {
            savedPackets.wait();
        }
        // SavedPackets list will contain the packet emitted from deviceId 1 and hence
        // our shouldSendJoin flag is true
        assertEquals(outboundPacket.toString().contains("of:1"), true);
        // Reconfiguring the network
        MockNetworkConfigListener newListener = new MockNetworkConfigListener();
        igmpManager.networkConfig.addListener(newListener);
        NetworkConfigEvent event =
             new NetworkConfigEvent(NetworkConfigEvent.Type.CONFIG_UPDATED, null, IGMPPROXY_CONFIG_CLASS);
        newListener.event(event);
        // Sending the second packet with same group ip address and here shouldSendJoin
        // flag will be false as isIgmpOnPodBasis flag is false.
        sendPacket(secondPacket);

        synchronized (savedPackets) {
            savedPackets.wait();
        }
        assertTrue(savedPackets.size() == 2);
        // Second packet will be added in the list as shouldSendJoin Flag is true.
        assertEquals(outboundPacket.toString().contains("of:2"), true);

    }

}
