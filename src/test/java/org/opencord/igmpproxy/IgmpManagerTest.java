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
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;
import org.opencord.sadis.BandwidthProfileInformation;
import org.opencord.sadis.BaseInformationService;
import org.opencord.sadis.SadisService;
import org.opencord.sadis.SubscriberAndDeviceInformation;

import static org.junit.Assert.*;

public class IgmpManagerTest extends IgmpManagerBase {

    private static final int WAIT_TIMEOUT = 1000;
    protected static final String CLIENT_NAS_PORT_ID = "PON 1/1";
    protected static final String CLIENT_CIRCUIT_ID = "CIR-PON 1/1";
    protected String dsBpId = "HSIA-DS";

    private IgmpManager igmpManager;

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
    protected class MockSadisService implements SadisService {

        @Override
        public BaseInformationService<SubscriberAndDeviceInformation> getSubscriberInfoService() {
            return new MockSubService();
        }

        @Override
        public BaseInformationService<BandwidthProfileInformation> getBandwidthProfileService() {
            return new MockBpService();
        }
    }

    private class MockBpService implements BaseInformationService<BandwidthProfileInformation> {

        @Override
        public void invalidateAll() {

        }

        @Override
        public void invalidateId(String id) {

        }

        @Override
        public BandwidthProfileInformation get(String id) {
            if (id.equals(dsBpId)) {
                BandwidthProfileInformation bpInfo = new BandwidthProfileInformation();
                bpInfo.setAssuredInformationRate(0);
                bpInfo.setCommittedInformationRate(10000);
                bpInfo.setCommittedBurstSize(1000L);
                bpInfo.setExceededBurstSize(2000L);
                bpInfo.setExceededInformationRate(20000);
                return bpInfo;
            }
            return null;
        }

        @Override
        public BandwidthProfileInformation getfromCache(String id) {
            return null;
        }
    }

    private class MockSubService implements BaseInformationService<SubscriberAndDeviceInformation> {
        MockSubscriberAndDeviceInformation sub =
                new MockSubscriberAndDeviceInformation(CLIENT_NAS_PORT_ID,
                                                       CLIENT_NAS_PORT_ID, CLIENT_CIRCUIT_ID, null, null);

        @Override
        public SubscriberAndDeviceInformation get(String id) {
            return sub;
        }

        @Override
        public void invalidateAll() {
        }

        @Override
        public void invalidateId(String id) {
        }

        @Override
        public SubscriberAndDeviceInformation getfromCache(String id) {
            return null;
        }
    }

    private class MockSubscriberAndDeviceInformation extends SubscriberAndDeviceInformation {

        MockSubscriberAndDeviceInformation(String id, String nasPortId,
                                           String circuitId, MacAddress hardId,
                                           Ip4Address ipAddress) {
            this.setHardwareIdentifier(hardId);
            this.setId(id);
            this.setIPAddress(ipAddress);
            this.setNasPortId(nasPortId);
            this.setCircuitId(circuitId);
        }
    }
}
