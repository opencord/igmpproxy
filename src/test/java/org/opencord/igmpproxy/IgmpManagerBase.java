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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.mastership.MastershipServiceAdapter;
import org.onosproject.mcast.api.McastListener;
import org.onosproject.mcast.api.McastRoute;
import org.onosproject.mcast.api.McastRouteData;
import org.onosproject.mcast.api.MulticastRouteService;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigRegistryAdapter;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.Annotations;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.basics.McastConfig;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceServiceAdapter;
import org.onosproject.net.packet.DefaultInboundPacket;
import org.onosproject.net.packet.DefaultPacketContext;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketServiceAdapter;
import org.opencord.cordconfig.access.AccessDeviceConfig;
import org.opencord.cordconfig.access.AccessDeviceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IgmpManagerBase {

    // Device configuration
    protected static final DeviceId DEVICE_ID_OF_A = DeviceId.deviceId("of:1");
    protected static final DeviceId DEVICE_ID_OF_B = DeviceId.deviceId("of:2");

    //Multicast ip address
    protected static final Ip4Address GROUP_IP = Ip4Address.valueOf("224.0.0.0");
    // Source ip address of two different device.
    protected static final Ip4Address SOURCE_IP_OF_A = Ip4Address.valueOf("10.177.125.4");
    protected static final Ip4Address SOURCE_IP_OF_B = Ip4Address.valueOf("10.177.125.5");

    // Common connect point of aggregation switch used by all devices.
    protected static final ConnectPoint COMMON_CONNECT_POINT =
           ConnectPoint.deviceConnectPoint("of:00000000000000003/3");
    // Uplink ports for two olts A and B
    protected static final PortNumber PORT_A = PortNumber.portNumber(1);
    protected static final PortNumber PORT_B = PortNumber.portNumber(2);

    // Connect Point mode for two olts
    protected static final ConnectPoint CONNECT_POINT_A = new ConnectPoint(DEVICE_ID_OF_A, PORT_A);
    protected static final ConnectPoint CONNECT_POINT_B = new ConnectPoint(DEVICE_ID_OF_B, PORT_B);

    // setOfDevices which will store device id of two olts
    protected Set<DeviceId> setOfDevices = new HashSet<DeviceId>(Arrays.asList(DEVICE_ID_OF_A, DEVICE_ID_OF_B));
    protected List<Port> lsPorts = new ArrayList<Port>();
    // Flag for adding two different devices in oltData
    protected boolean flagForDevice = true;
    PacketContext context;
    // Flag for sending two different packets
    protected boolean flagForPacket = true;
    // List to store the packets emitted
    protected List<OutboundPacket> savedPackets;
    protected PacketProcessor packetProcessor;
    private Logger log = LoggerFactory.getLogger(getClass());

    class MockDeviceService extends DeviceServiceAdapter {

        @Override
        public Device getDevice(DeviceId deviceId) {
           if (flagForDevice) {
               DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder()
                           .set(AnnotationKeys.MANAGEMENT_ADDRESS, SOURCE_IP_OF_A.toString());
               SparseAnnotations annotations = annotationsBuilder.build();
               Annotations[] da = {annotations };
               Device deviceA = new DefaultDevice(null, DEVICE_ID_OF_A, Device.Type.OTHER, "", "", "", "", null, da);
               flagForDevice = false;
               return deviceA;
            } else {
               DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder()
                          .set(AnnotationKeys.MANAGEMENT_ADDRESS, SOURCE_IP_OF_B.toString());
               SparseAnnotations annotations = annotationsBuilder.build();
               Annotations[] da = {annotations };
               Device deviceB = new DefaultDevice(null, DEVICE_ID_OF_B, Device.Type.OTHER, "", "", "", "", null, da);
               return deviceB;
           }
        }
        @Override
        public List<Port> getPorts(DeviceId deviceId) {
            return lsPorts;
        }
    }

    static final Class<AccessDeviceConfig> CONFIG_CLASS = AccessDeviceConfig.class;
    static final Class<IgmpproxyConfig> IGMPPROXY_CONFIG_CLASS = IgmpproxyConfig.class;
    static final Class<IgmpproxySsmTranslateConfig> IGMPPROXY_SSM_CONFIG_CLASS = IgmpproxySsmTranslateConfig.class;
    static final Class<McastConfig> MCAST_CONFIG_CLASS = McastConfig.class;
    ConfigFactory<ApplicationId, IgmpproxyConfig> igmpproxyConfigFactory =
         new ConfigFactory<ApplicationId, IgmpproxyConfig>(
        SubjectFactories.APP_SUBJECT_FACTORY, IGMPPROXY_CONFIG_CLASS, "igmpproxy") {
    @Override
    public IgmpproxyConfig createConfig() {
          return new IgmpproxyConfig();
        }
    };

    ConfigFactory<ApplicationId, IgmpproxySsmTranslateConfig> igmpproxySsmConfigFactory =
        new ConfigFactory<ApplicationId, IgmpproxySsmTranslateConfig>(
        SubjectFactories.APP_SUBJECT_FACTORY, IGMPPROXY_SSM_CONFIG_CLASS, "ssmTranslate", true) {

        @Override
        public IgmpproxySsmTranslateConfig createConfig() {
            return new IgmpproxySsmTranslateConfig();
        }
    };


    class MockIgmpProxyConfig extends IgmpproxyConfig {
        boolean igmpOnPodBasis = true;

        MockIgmpProxyConfig(boolean igmpFlagValue) {
           igmpOnPodBasis = igmpFlagValue;
       }
        @Override
        public boolean igmpOnPodBasis() {
            return igmpOnPodBasis;
        }

        @Override
        public ConnectPoint getSourceDeviceAndPort() {
              return COMMON_CONNECT_POINT;
        }

        @Override
        public ConnectPoint connectPoint() {
               return COMMON_CONNECT_POINT;
        }
    }


    static class MockAccessDeviceConfig extends AccessDeviceConfig {

        public MockAccessDeviceConfig() {
            super();
        }

        public MockAccessDeviceConfig(DeviceId id) {
            super();
            subject = id;
        }

        @Override
        public AccessDeviceData getAccessDevice() {
            PortNumber uplink = PortNumber.portNumber(3);
            VlanId vlan = VlanId.vlanId((short) 0);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode defaultVlanNode = null;
            try {
                  defaultVlanNode = (JsonNode) mapper.readTree("{\"driver\":\"pmc-olt\" , \"type \" : \"OLT\"}");
            } catch (IOException e) {
                  e.printStackTrace();
            }

            Optional<VlanId> defaultVlan;
            if (defaultVlanNode.isMissingNode()) {
                defaultVlan = Optional.empty();
            } else {
                defaultVlan = Optional.of(VlanId.vlanId(defaultVlanNode.shortValue()));
            }
            return new AccessDeviceData(subject, uplink, vlan, defaultVlan);
        }
    }

    ConfigFactory<DeviceId, AccessDeviceConfig> cf =
            new ConfigFactory<DeviceId, AccessDeviceConfig>(
               SubjectFactories.DEVICE_SUBJECT_FACTORY, CONFIG_CLASS, "accessDevice") {
        @Override
        public AccessDeviceConfig createConfig() {
            return new MockAccessDeviceConfig();
        }
     };

     class TestNetworkConfigRegistry extends NetworkConfigRegistryAdapter {
         Boolean igmpOnPodFlag = false;
         TestNetworkConfigRegistry(Boolean igmpFlag) {
             igmpOnPodFlag = igmpFlag;
         }
        @SuppressWarnings("unchecked")
        @Override
        public <S, C extends Config<S>> C getConfig(S subject, Class<C> configClass) {
            if (configClass.getName().equalsIgnoreCase("org.opencord.igmpproxy.IgmpproxyConfig")) {
                IgmpproxyConfig igmpproxyConfig = new MockIgmpProxyConfig(igmpOnPodFlag);
                return (C) igmpproxyConfig;
            } else if (configClass.getName().equalsIgnoreCase("org.opencord.cordconfig.access.AccessDeviceConfig")) {

                if (subject.toString().equals(DEVICE_ID_OF_A.toString())) {
                    AccessDeviceConfig accessDeviceConfig = new MockAccessDeviceConfig(DEVICE_ID_OF_A);
                return (C) accessDeviceConfig;
                } else {
                    AccessDeviceConfig accessDeviceConfig = new MockAccessDeviceConfig(DEVICE_ID_OF_B);
                    return (C) accessDeviceConfig;
                }
            } else {
                super.getConfig(subject, configClass);
            }
            return null;
       }

        @SuppressWarnings("unchecked")
        @Override
        public <S, C extends Config<S>> ConfigFactory<S, C> getConfigFactory(Class<C> configClass) {
            return (ConfigFactory<S, C>) cf;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S, C extends Config<S>> Set<S> getSubjects(Class<S> subjectClass, Class<C> configClass) {
            return (Set<S>) setOfDevices;
        }
    }


    /**
     * Keeps a reference to the PacketProcessor and saves the OutboundPackets. Adds
     * the emitted packet in savedPackets list
     */
    class MockPacketService extends PacketServiceAdapter {

        public MockPacketService() {
            savedPackets = new LinkedList<>();
        }

        @Override
        public void addProcessor(PacketProcessor processor, int priority) {
            packetProcessor = processor;
        }

        @Override
        public void emit(OutboundPacket packet) {
            synchronized (savedPackets) {
               savedPackets.add(packet);
               savedPackets.notify();
            }
        }
     }


    class MockMastershipService extends MastershipServiceAdapter {
        @Override
        public boolean isLocalMaster(DeviceId deviceId) {
            return true;
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


    /**
     * Mocks the DefaultPacketContext.
     */
    final class TestPacketContext extends DefaultPacketContext {
       TestPacketContext(long time, InboundPacket inPkt, OutboundPacket outPkt, boolean block) {
         super(time, inPkt, outPkt, block);
         }

        @Override
        public void send() {
            // We don't send anything out.
        }
    }

    /**
     * Sends an Ethernet packet to the process method of the Packet Processor.
     *
     * @param reply Ethernet packet
     * @throws InterruptedException
     */
   void sendPacket(Ethernet reply) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(reply.serialize());

        if (flagForPacket) {
            InboundPacket inPacket = new DefaultInboundPacket(CONNECT_POINT_A, reply, byteBuffer);
            context = new TestPacketContext(127L, inPacket, null, false);
            flagForPacket = false;

            packetProcessor.process(context);
        } else {
            InboundPacket inBoundPacket = new DefaultInboundPacket(CONNECT_POINT_B, reply, byteBuffer);
            context = new TestPacketContext(127L, inBoundPacket, null, false);
            flagForPacket = true;

            packetProcessor.process(context);
        }
   }

}
