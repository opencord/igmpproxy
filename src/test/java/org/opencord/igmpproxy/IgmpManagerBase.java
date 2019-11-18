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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.onosproject.net.NetTestTools.connectPoint;

public class IgmpManagerBase {
    DeviceId devIdA = DeviceId.deviceId("of:1");
    DeviceId devIdB = DeviceId.deviceId("of:2");
    Ip4Address groupIp = Ip4Address.valueOf("224.0.0.0");
    Ip4Address srcIp = Ip4Address.valueOf("10.177.125.4");
    Ip4Address srcIp1 = Ip4Address.valueOf("10.177.125.5");
    PortNumber uplink = PortNumber.portNumber(3);
    List<Port> lsPorts = new ArrayList<Port>();
    ConnectPoint cp = new ConnectPoint(devIdA, uplink);
    ConnectPoint cpB = new ConnectPoint(devIdB, uplink);
    Optional<ConnectPoint> connectPoint = Optional.of(cp);
    Optional<ConnectPoint> connectPointB = Optional.of(cpB);
    Set<DeviceId> setOfDevices = new HashSet<DeviceId>();
    boolean flagForDevice = true;
    boolean flag = true;
    IgmpManager igmpManager;

    List<OutboundPacket> savedPackets = new LinkedList<>();
    PacketProcessor packetProcessor;

    static final Class<AccessDeviceConfig> CONFIG_CLASS = AccessDeviceConfig.class;
    ConfigFactory<DeviceId, AccessDeviceConfig> cf = new ConfigFactory<DeviceId, AccessDeviceConfig>(
            SubjectFactories.DEVICE_SUBJECT_FACTORY, CONFIG_CLASS, "accessDevice") {
        @Override
        public AccessDeviceConfig createConfig() {
            return new MockAccessDeviceConfig();
        }
    };

    /**
     * Saves the given packet onto the saved packets list.
     *
     * @param packet
     *            packet to save
     */
    void savePacket(OutboundPacket packet) {
        savedPackets.add(packet);

    }

    // Keeps a reference to the PacketProcessor and saves the OutboundPackets.
    class MockPacketService extends PacketServiceAdapter {
        @Override
        public void addProcessor(PacketProcessor processor, int priority) {
            packetProcessor = processor;
        }

        @Override
        public void emit(OutboundPacket packet) {
            super.emit(packet);
            savePacket(packet);
        }

    }

    class MockMastershipService extends MastershipServiceAdapter {
        @Override
        public boolean isLocalMaster(DeviceId deviceId) {
            return true;
        }
    }

    class MockDeviceService extends DeviceServiceAdapter {

        @Override
        public Device getDevice(DeviceId deviceId) {

            if (flagForDevice) {
                DeviceId devIdA = DeviceId.deviceId("of:1");
                DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder()
                        .set(AnnotationKeys.MANAGEMENT_ADDRESS, "10.177.125.4");
                SparseAnnotations annotations = annotationsBuilder.build();
                Annotations[] da = {annotations };
                Device deviceA = new DefaultDevice(null, devIdA, Device.Type.OTHER, "", "", "", "", null, da);
                flagForDevice = false;
                return deviceA;
            } else {
                DeviceId devIdB = DeviceId.deviceId("of:2");
                DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder()
                        .set(AnnotationKeys.MANAGEMENT_ADDRESS, "10.177.125.5");
                SparseAnnotations annotations = annotationsBuilder.build();
                Annotations[] da = {annotations };
                Device deviceA = new DefaultDevice(null, devIdB, Device.Type.OTHER, "", "", "", "", null, da);
                return deviceA;
            }
        }

    }

    static final Class<IgmpproxyConfig> IGMPPROXY_CONFIG_CLASS = IgmpproxyConfig.class;
    static final Class<IgmpproxySsmTranslateConfig> IGMPPROXY_SSM_CONFIG_CLASS =
                IgmpproxySsmTranslateConfig.class;
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

    class MockIgmpProxyConfig extends IgmpproxyConfig {
        static final String IGMP_ON_POD_BASIS = "true";

        @Override
        public boolean igmpOnPodBasis() {
            return Boolean.parseBoolean(IGMP_ON_POD_BASIS);
        }

        @Override
        public ConnectPoint getSourceDeviceAndPort() {
            return ConnectPoint.deviceConnectPoint("of:0000000001/3");
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

    class TestNetworkConfigRegistry extends NetworkConfigRegistryAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public <S, C extends Config<S>> C getConfig(S subject, Class<C> configClass) {
            if (configClass.getName().equalsIgnoreCase("org.opencord.igmpproxy.IgmpproxyConfig")) {

                IgmpproxyConfig igmpproxyConfig = new MockIgmpProxyConfig();
                return (C) igmpproxyConfig;

            } else {
                super.getConfig(subject, configClass);

            }
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
     * @param reply
     *            Ethernet packet
     * @throws InterruptedException
     */
    void sendPacket(Ethernet reply) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(reply.serialize());
        PacketContext context, context1;
        if (flag) {
            InboundPacket inPacket = new DefaultInboundPacket(connectPoint("1", 1), reply, byteBuffer);
            context = new TestPacketContext(127L, inPacket, null, false);
            flag = false;

            packetProcessor.process(context);

        } else {
            InboundPacket inBoundPacket = new DefaultInboundPacket(connectPoint("2", 2), reply, byteBuffer);
            context1 = new TestPacketContext(127L, inBoundPacket, null, false);
            packetProcessor.process(context1);
        }

    }

}
