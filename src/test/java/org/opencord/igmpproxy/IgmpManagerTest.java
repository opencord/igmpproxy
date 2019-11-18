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

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.mastership.MastershipServiceAdapter;
import org.onosproject.mcast.api.McastListener;
import org.onosproject.mcast.api.McastRoute;
import org.onosproject.mcast.api.McastRouteData;
import org.onosproject.mcast.api.MulticastRouteService;
import org.onosproject.net.Annotations;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Element;
import org.onosproject.net.HostId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigRegistryAdapter;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceServiceAdapter;
import org.onosproject.net.flow.FlowRuleServiceAdapter;
import org.onosproject.net.flowobjective.FlowObjectiveServiceAdapter;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketServiceAdapter;


public class IgmpManagerTest {

        private IgmpManager igmpManager;
        PacketProcessor packetProcessor;
        private static final Class<IgmpproxyConfig> IGMPPROXY_CONFIG_CLASS = IgmpproxyConfig.class;
        private static final Class<IgmpproxySsmTranslateConfig> IGMPPROXY_SSM_CONFIG_CLASS =
                IgmpproxySsmTranslateConfig.class;

        private ConfigFactory<ApplicationId, IgmpproxyConfig> igmpproxyConfigFactory =
                new ConfigFactory<ApplicationId, IgmpproxyConfig>(
                        SubjectFactories.APP_SUBJECT_FACTORY, IGMPPROXY_CONFIG_CLASS, "igmpproxy") {
                @Override
                public IgmpproxyConfig createConfig() {
                        return new IgmpproxyConfig();
                }
        };
        private ConfigFactory<ApplicationId, IgmpproxySsmTranslateConfig> igmpproxySsmConfigFactory =
                new ConfigFactory<ApplicationId, IgmpproxySsmTranslateConfig>(
                        SubjectFactories.APP_SUBJECT_FACTORY, IGMPPROXY_SSM_CONFIG_CLASS, "ssmTranslate", true) {
                @Override
                public IgmpproxySsmTranslateConfig createConfig() {
                        return new IgmpproxySsmTranslateConfig();
                }
        };

        /**
         * Mocks the network config registry.
         */
        private static final class TestNetworkConfigRegistry extends NetworkConfigRegistryAdapter {

        }

        /**
         * Keeps a reference to the PacketProcessor and saves the OutboundPackets.
         */
        class MockPacketService extends PacketServiceAdapter {

                @Override
                public void addProcessor(PacketProcessor processor, int priority) {
                        packetProcessor = processor;
                }

        }

        class MockFlowObjectiveService extends FlowObjectiveServiceAdapter {

        }

        class MockFlowRuleService extends FlowRuleServiceAdapter {

        }

        /**
         * Mocks the DeviceService.
         */
        final class TestDeviceService extends DeviceServiceAdapter {
                @Override
                public Port getPort(ConnectPoint cp) {
                        return new MockPort();
                }
        }

        final class TestMulticastRouteService implements MulticastRouteService {

                @Override
                public void addListener(McastListener listener) {

                }

                @Override
                public void removeListener(McastListener listener) {

                }

                @Override
                public void add(McastRoute route) {

                }

                @Override
                public void remove(McastRoute route) {

                }

                @Override
                public Set<McastRoute> getRoutes() {
                        return null;
                }

                @Override
                public Set<McastRoute> getRoute(IpAddress groupIp, IpAddress sourceIp) {
                        return null;
                }

                @Override
                public void addSource(McastRoute route, HostId source) {

                }

                @Override
                public void addSources(McastRoute route, HostId hostId, Set<ConnectPoint> connectPoints) {

                }

                @Override
                public void addSources(McastRoute route, Set<ConnectPoint> sources) {

                }

                @Override
                public void removeSources(McastRoute route) {

                }

                @Override
                public void removeSource(McastRoute route, HostId source) {

                }

                @Override
                public void addSink(McastRoute route, HostId hostId) {

                }

                @Override
                public void addSinks(McastRoute route, HostId hostId, Set<ConnectPoint> connectPoints) {

                }

                @Override
                public void addSinks(McastRoute route, Set<ConnectPoint> sinks) {

                }

                @Override
                public void removeSinks(McastRoute route) {

                }

                @Override
                public void removeSink(McastRoute route, HostId hostId) {

                }

                @Override
                public void removeSinks(McastRoute route, Set<ConnectPoint> sink) {

                }

                @Override
                public McastRouteData routeData(McastRoute route) {
                        return null;
                }

                @Override
                public Set<ConnectPoint> sources(McastRoute route) {
                        return null;
                }

                @Override
                public Set<ConnectPoint> sources(McastRoute route, HostId hostId) {
                        return null;
                }

                @Override
                public Set<ConnectPoint> sinks(McastRoute route) {
                        return null;
                }


                @Override
                public Set<ConnectPoint> sinks(McastRoute route, HostId hostId) {
                        return null;
                }

                @Override
                public Set<ConnectPoint> nonHostSinks(McastRoute route) {
                        return null;
                }

        }

        private class MockPort implements Port {

                @Override
                public boolean isEnabled() {
                        return true;
                }

                @Override
                public long portSpeed() {
                        return 1000;
                }

                @Override
                public Element element() {
                        return null;
                }

                @Override
                public PortNumber number() {
                        return null;
                }

                @Override
                public Annotations annotations() {
                        return new MockAnnotations();
                }

                @Override
                public Type type() {
                        return Port.Type.FIBER;
                }
        }

        private class MockAnnotations implements Annotations {

                @Override
                public String value(String val) {
                        return "PON 1/1";
                }

                @Override
                public Set<String> keys() {
                        return null;
                }
        }

        final class TestFlowObjectiveService extends FlowObjectiveServiceAdapter {

        }

        final class TestMastershipService extends MastershipServiceAdapter {

        }

        /**
         * Set up the IGMP application.
         */
        @Before
        public void setUp() {
                igmpManager = new IgmpManager();
                igmpManager.coreService = new CoreServiceAdapter();
                igmpManager.networkConfig = new TestNetworkConfigRegistry();
                igmpManager.packetService = new MockPacketService();
                igmpManager.flowRuleService = new MockFlowRuleService();
                igmpManager.deviceService = new TestDeviceService();
                igmpManager.mastershipService = new TestMastershipService();
                igmpManager.flowObjectiveService = new TestFlowObjectiveService();
                igmpManager.multicastService = new TestMulticastRouteService();

                igmpManager.activate();
        }

        /**
         * Tear Down the IGMP application.
         */
        @After
        public void tearDown() {
                igmpManager.deactivate();
        }

        /*
         * Checking the default value of IGMP_ON_POD_BASIS.
         */
        @SuppressWarnings("static-access")
        @Test
        public void testIsIgmpOnPodBasis() {
                assertEquals(igmpManager.isIgmpOnPodBasis(), false);
        }

        /**
         * To test whether the count value which is incremented.
         * on each call to join method and decremented
         * on each call to leave method
         */
        @Test
        public void testIgmpProxyCount() {
                Boolean incrementingCount, decrementingCount;
                String deviceIdA = "of:0000000000000004";
                String deviceIdB = "of:0000000000000005";
                DeviceId devIdA =  DeviceId.deviceId(deviceIdA);
                DeviceId devIdB =  DeviceId.deviceId(deviceIdB);
                Ip4Address groupIp = Ip4Address.valueOf("10.177.25.5");
                Ip4Address srcIp = Ip4Address.valueOf("10.177.125.4");
                // StateMachine.join will return true when a new StateMachine is created.
                incrementingCount = StateMachine.join(devIdA, groupIp, srcIp);
                assertEquals(incrementingCount, true);
                // StateMachine.join will return false, when join request is sent for an existing machine.
                incrementingCount = StateMachine.join(devIdA, groupIp, srcIp);
                assertEquals(incrementingCount, false);
                incrementingCount = StateMachine.join(devIdB, groupIp, srcIp);
                //when a leave request is sent for existing machine, counter is decremented and false is returned.
                decrementingCount = StateMachine.leave(devIdA, groupIp);
                assertEquals(decrementingCount, false);
        }
}

