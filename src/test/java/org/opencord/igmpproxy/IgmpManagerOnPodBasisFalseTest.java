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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;

public class IgmpManagerOnPodBasisFalseTest extends IgmpManagerOnPodBasisFalseBase {
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
        igmpManager.networkConfig = new TestNetworkConfigRegistry(false);
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
       assertEquals(false, IgmpManager.isIgmpOnPodBasis());
    }

    // Testing the flow of packet when isIgmpOnPodBasis value is false.
    @Test
    public void testIgmpOnPodBasisFalseValue() throws InterruptedException {
        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_A);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(GROUP_IP, SOURCE_IP_OF_B);
        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);
        // Emitted packet is stored in list savedPackets
        assertNotNull(savedPacketsList);
        synchronized (savedPacketsList) {
            savedPacketsList.wait();
        }
        // Sending the second packet with same group ip address
        sendPacket(secondPacket);
        synchronized (savedPacketsList) {
            savedPacketsList.wait();
        }
     // Emitted packet is stored in list savedPackets as shouldSendJoin flag is true
        assertEquals(2, savedPacketsList.size());
    }
}
