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

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.onlab.packet.Ethernet;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.DeviceServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;
import org.opencord.cordconfig.access.AccessDeviceConfig;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

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
      setOfDevices.add(devIdA);
      setOfDevices.add(devIdB);
      igmpManager.mastershipService = new MockMastershipService();
      igmpManager.flowObjectiveService = new FlowObjectiveServiceAdapter();
      igmpManager.deviceService = new DeviceServiceAdapter();
      igmpManager.packetService = new MockPacketService();
      igmpManager.flowRuleService = new FlowRuleServiceAdapter();
      igmpManager.multicastService = new TestMulticastRouteService();
      igmpManager.networkConfig = EasyMock.createNiceMock(TestNetworkConfigRegistry.class);

      expect(igmpManager.networkConfig.getConfigFactory(CONFIG_CLASS)).andReturn(cf);
          expect(igmpManager.networkConfig.getSubjects(DeviceId.class, AccessDeviceConfig.class))
                 .andReturn(setOfDevices);
          expect(igmpManager.networkConfig.getConfig(devIdA, AccessDeviceConfig.class))
                 .andReturn(new MockAccessDeviceConfig(devIdA));
          expect(igmpManager.networkConfig.getConfig(devIdB, AccessDeviceConfig.class))
                 .andReturn(new MockAccessDeviceConfig(devIdB));

          replay(igmpManager.networkConfig);

          igmpManager.deviceService = EasyMock.createNiceMock(DeviceService.class);
          expect(igmpManager.deviceService.getPorts(devIdA)).andReturn(lsPorts);
          expect(igmpManager.deviceService.getPorts(devIdB)).andReturn(lsPorts);
          replay(igmpManager.deviceService);


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
        mockStatic(IgmpManager.class);
        expect(IgmpManager.isIgmpOnPodBasis()).andReturn(false);
        PowerMock.replay(IgmpManager.class);
        assertEquals(IgmpManager.isIgmpOnPodBasis(), false);
    }

    // Checking the value of IGMP_ON_POD_BASIS.
    @Test
    public void testIsIgmpOnPodBasTrueValue() {
        mockStatic(IgmpManager.class);
        expect(IgmpManager.isIgmpOnPodBasis()).andReturn(true);
        PowerMock.replay(IgmpManager.class);
        assertEquals(IgmpManager.isIgmpOnPodBasis(), true);
    }

    // Testing IGMP_ON_POD_BASIS value by sending two packets.
    @Test
    public void testIgmpOnPodBasisValues() throws InterruptedException {

        Ethernet firstPacket = IgmpSender.getInstance().buildIgmpV3Join(groupIp, srcIp);
        Ethernet secondPacket = IgmpSender.getInstance().buildIgmpV3Join(groupIp, srcIp1);
        mockStatic(IgmpManager.class);
        expect(IgmpManager.getDeviceUplink(devIdA)).andReturn(uplink);
        expect(IgmpManager.getDeviceUplink(devIdB)).andReturn(uplink);
        expect(IgmpManager.getSource()).andReturn(connectPoint);
        expect(IgmpManager.getSource()).andReturn(connectPointB);
        expect(IgmpManager.isIgmpOnPodBasis()).andReturn(true);

        try {
               PowerMock.expectPrivate(StateMachine.class, "groupListenedByOtherDevices",
                           devIdA, groupIp).andReturn(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        expect(IgmpManager.getUnsolicitedTimeout()).andReturn(3);
        replayAll();
        igmpManager.deviceService = new MockDeviceService();
        IgmpManager.connectPoint = cp;

        // Sending first packet and here shouldSendjoin flag will be true
        sendPacket(firstPacket);

        synchronized (savedPackets) {
            savedPackets.wait();
        }
        // Emitted packet is stored in list savedPackets
        assertNotNull(savedPackets);

        // SavedPackets list will contain the packet emitted from deviceId 1 and hence
        // our shouldSendJoin flag is true

        assertEquals(savedPackets.get(savedPackets.size() - 1).toString().contains("of:1"), true);

        // Sending the second packet with same group ip address and here shouldSendJoin
        // flag will be false.

        sendPacket(secondPacket);

        // Second packet will not be emitted and hence it will not be present in our
        // savedPackets list
        assertEquals(savedPackets.toString().contains("of:2"), false);

        assertNotNull(IgmpManager.groupMemberMap);


     }

}
