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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.config.NetworkConfigRegistryAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import static org.easymock.EasyMock.expect;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IgmpManager.class)
public class IgmpManagerTest extends IgmpManagerBase {

    @Rule
    public TestName testName = new TestName();

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
        if (testName.getMethodName().equalsIgnoreCase("testIsIgmpOnPodBasisDefaultValue")) {
            igmpManager.networkConfig = new NetworkConfigRegistryAdapter();
        } else {
        igmpManager.networkConfig = new TestNetworkConfigRegistry();
        }
        igmpManager.activate();

    }

    // Tear Down the IGMP application.
    @After
    public void tearDown() {
        igmpManager.deactivate();
    }

    // Checking the default value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisDefaultValue() {
       assertEquals(IgmpManager.isIgmpOnPodBasis(), false);
    }

    // Testing the flow of packet when isIgmpOnPodBasis value is false.
    @Test
    public void testIgmpOnPodBasisFalseValue() throws InterruptedException {

        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);
        mockStatic(IgmpManager.class);
        expect(IgmpManager.getDeviceUplink(DEVICE_ID_OF_A)).andReturn(UPLINK);
        expect(IgmpManager.getDeviceUplink(DEVICE_ID_OF_B)).andReturn(UPLINK);
        expect(IgmpManager.getSource()).andReturn(CONNECT_POINT_OF_A);
        expect(IgmpManager.getSource()).andReturn(CONNECT_POINT_OF_B);
        expect(IgmpManager.isIgmpOnPodBasis()).andReturn(false).atLeastOnce();
        expect(IgmpManager.getUnsolicitedTimeout()).andReturn(3).times(2);
        replayAll();
        IgmpManager.connectPoint = CONNECT_POINT_A;

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
        // ConnectPoint should not be null while sending the packet.
        IgmpManager.connectPoint = CONNECT_POINT_B;

        // Sending the second packet with same group ip address and here shouldSendJoin
        // flag will be false as isIgmpOnPodBasis flag is false.
        sendPacket(secondPacket);

        synchronized (savedPackets) {
            savedPackets.wait();
        }
        assertTrue(savedPackets.size() > 1);
        // Second packet will be added in the list as shouldSendJoin Flag is true.
        assertEquals(outboundPacket.toString().contains("of:2"), true);
        assertEquals(IgmpManager.isIgmpOnPodBasis(), false);

    }

    // Checking the value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasisValue() {
       assertEquals(IgmpManager.isIgmpOnPodBasis(), true);
    }

    // Testing IGMP_ON_POD_BASIS value by sending two packets.
    @Test
    public void testIgmpOnPodBasisTrueValue() throws InterruptedException {
        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);
        mockStatic(IgmpManager.class);
        expect(IgmpManager.getDeviceUplink(DEVICE_ID_OF_A)).andReturn(UPLINK);
        expect(IgmpManager.getDeviceUplink(DEVICE_ID_OF_B)).andReturn(UPLINK);
        expect(IgmpManager.getSource()).andReturn(CONNECT_POINT_OF_A);
        expect(IgmpManager.getSource()).andReturn(CONNECT_POINT_OF_B);
        expect(IgmpManager.isIgmpOnPodBasis()).andReturn(true);
        try {
              PowerMock.expectPrivate(
                     StateMachine.class, "groupListenedByOtherDevices", DEVICE_ID_OF_A, GROUP_IP)
                     .andReturn(true);
        } catch (Exception e) {
           e.printStackTrace();
        }
        expect(IgmpManager.getUnsolicitedTimeout()).andReturn(3);
        replayAll();
        IgmpManager.connectPoint = CONNECT_POINT_A;
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
}
