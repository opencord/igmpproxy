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

import com.google.common.collect.ImmutableSet;
import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cfg.ConfigProperty;
import org.onosproject.core.ApplicationId;
import org.onosproject.event.DefaultEventSinkRegistry;
import org.onosproject.event.Event;
import org.onosproject.event.EventDeliveryService;
import org.onosproject.event.EventSink;
import org.onosproject.mastership.MastershipServiceAdapter;
import org.onosproject.mcast.api.McastListener;
import org.onosproject.mcast.api.McastRoute;
import org.onosproject.mcast.api.McastRouteData;
import org.onosproject.mcast.api.MulticastRouteService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.Annotations;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.DefaultPort;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigRegistryAdapter;
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
import org.opencord.sadis.BandwidthProfileInformation;
import org.opencord.sadis.BaseInformationService;
import org.opencord.sadis.SadisService;
import org.opencord.sadis.SubscriberAndDeviceInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    protected static final PortNumber PORT_NNI = PortNumber.portNumber(65536);

    // Connect Point mode for two olts
    protected static final ConnectPoint CONNECT_POINT_A = new ConnectPoint(DEVICE_ID_OF_A, PORT_A);
    protected static final ConnectPoint CONNECT_POINT_B = new ConnectPoint(DEVICE_ID_OF_B, PORT_B);

    protected static final String CLIENT_NAS_PORT_ID = "PON 1/1";
    protected static final String CLIENT_CIRCUIT_ID = "CIR-PON 1/1";
    protected String dsBpId = "HSIA-DS";

    private static final String NNI_PREFIX = "nni";

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

        @Override
        public Port getPort(DeviceId deviceId, PortNumber portNumber) {
            if (portNumber.equals(PORT_NNI)) {
                DefaultAnnotations.Builder annotationsBuilder = DefaultAnnotations.builder()
                        .set(AnnotationKeys.PORT_NAME, NNI_PREFIX);
                Port nni = new DefaultPort(null, portNumber, true, annotationsBuilder.build());
                return nni;
            }
            return super.getPort(deviceId, portNumber);
        }
    }

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


     class TestNetworkConfigRegistry extends NetworkConfigRegistryAdapter {
         Boolean igmpOnPodFlag = false;

         TestNetworkConfigRegistry(Boolean igmpFlag) {
             igmpOnPodFlag = igmpFlag;
         }
        @SuppressWarnings("unchecked")
        @Override
        public <S> Set<S> getSubjects(Class<S> subjectClass) {
            if (subjectClass.getName().equalsIgnoreCase("org.onosproject.net.DeviceId")) {
                return (Set<S>) ImmutableSet.of(DEVICE_ID_OF_A, DEVICE_ID_OF_B);
            }
            return null;
       }

         @SuppressWarnings("unchecked")
         @Override
         public <S, C extends Config<S>> C getConfig(S subject, Class<C> configClass) {
             if (configClass.getName().equalsIgnoreCase("org.opencord.igmpproxy.IgmpproxyConfig")) {
                 IgmpproxyConfig igmpproxyConfig = new MockIgmpProxyConfig(igmpOnPodFlag);
                 return (C) igmpproxyConfig;
             } else {
                 super.getConfig(subject, configClass);
             }
             return null;
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
     * Sends Ethernet packet to the process method of the Packet Processor.
     *
     * @param reply Ethernet packet
     * @throws InterruptedException
     */
   void sendPacket(Ethernet reply, boolean isSingleSend) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(reply.serialize());

        if (isSingleSend) {
            InboundPacket inBoundPacket = new DefaultInboundPacket(CONNECT_POINT_B, reply, byteBuffer);
            context = new TestPacketContext(127L, inBoundPacket, null, false);

            packetProcessor.process(context);
        } else {
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
            this.setUplinkPort((int) PORT_NNI.toLong());
        }
    }

    protected class MockCfgService implements ComponentConfigService {

        @Override
        public Set<String> getComponentNames() {
            return null;
        }

        @Override
        public void registerProperties(Class<?> componentClass) {

        }

        @Override
        public void unregisterProperties(Class<?> componentClass, boolean clear) {

        }

        @Override
        public Set<ConfigProperty> getProperties(String componentName) {
            return null;
        }

        @Override
        public void setProperty(String componentName, String name, String value) {

        }

        @Override
        public void preSetProperty(String componentName, String name, String value) {

        }

        @Override
        public void preSetProperty(String componentName, String name, String value, boolean override) {

        }

        @Override
        public void unsetProperty(String componentName, String name) {

        }

        @Override
        public ConfigProperty getProperty(String componentName, String attribute) {
            return null;
        }

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
