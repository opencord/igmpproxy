**Terminology**

| **IGMP**   | **Internet Group Management Protocol**     |
|------------|--------------------------------------------|
| **CPE**    | **Customer Premise Equipment**             |
| **ONOS**   | **Open Network Operation System**          |
| **OF**     | **OpenFlow**                               |
| **vOLT**   | **Virtualized Optical Line Terminal**      |
| **ONT**    | **Optical Network Terminal**               |
| **SD-OLT** | **Software Defined Optical Line Terminal** |

1.  IGMP Overview and feature list

    1.  Support Igmp join

IGMP Proxy app receives and decodes igmp report messega from hosts. if the host
ask to join a group, Igmpproxy will add flow rule for the multicast traffic to
olt .And if it is the **first host join** into the group, Igmpproxy will forward
the message to olt uplink port.

If it is not the first host that joins into the multicast group, Igmpproxy will
add bucket into the flow rule, but will not forward to uplink port.

Support Igmp leave
------------------

IGMP Proxy app receives and decodes igmp report message from host, if the host
ask to leave a group, igmpproxy will raise a query to the group and wait for
hosts’ response. If any host not response for the query, Igmpproxy will remove
the host from this group.

Support Igmp fast leave
-----------------------

IGMP Proxy app receives and decodes igmp report message from host, if the host
ask to leave a group, Igmpproxy will remove bucket from the group’s flow rule.

If it is the latest host that existing in the group, Igmpproxy will forward the
message to olt uplink port.

Support Igmp period query and keep alive
----------------------------------------

Igmpproxy will periodical sent query message to each existing group, If any host
not response for the query, Igmpproxy will remove the host from this group.

1.  Software Architecture

    1.  IGMP Deployment Architecture

        ![cid:image001.png\@01D2036E.8855DCA0](media/72caf6b6c9c6f9a128c5eca8423b3767.png)

    2.  ONOS APP Architecture

*App layer:*

-   *Igmpproxy:* Nokia developed app for igmp (use proxy mode).

-   *IgmpSnoop:* ONOS official app for igmp (use snoop mode).

-   *Cordmcast:* ONOS official app , as the adapter between onos core service
    and specific igmp implements

*Onos Core Layer:*

-   *Flow objective:* for specific device and behavior, to find a driver for
    adaption

-   *Netconfig Service:* Used for our REST interface

-   *Packet Service:* To receive , decode, encode and dispatch openflow messages
    from/to devices

-   *Device Service:* To communicate with device via connection point

*Driver Layer:*

-   *Olt pipeline:* driver for our OLT device.

    1.  Igmp Message Flow

![](media/052750ec8402a955a5b6a9b3e513d9cc.png)

Comment:

For step 9, for performance purpose, is not really removing the rules. Just
remove the output port from the group rule.

IGMP Subsystem Structure
------------------------

*Apps and Sub-systems:*

*Igmpproxy App*

1.  Host state machine（interact with multicast source）, refer to RFC 2236

-   To join group, send **add sink** to Cordmcast

-   To leave group, send **remove sink** to Cordmcast

1.  source host management（interact with hosts）

-   accept and process Igmp join/leave packet form hosts

-   Find first join host and last leave host, inform the state machine

1.  Igmp packet encode/decode and dispatch

-   To decode v2/v3 igmp packet, and dispatch them into different subsystems

-   For v3 join packet, divided it with different group addresses

1.  Rest interface (use onos network configuration service) subsystems, for user
    configuration

    *Cordmcast App:*

    Receive **add sink**/ **remove sink** message, translate into **forward**
    and **next** primitives, and send those primitives to olt pipeline

    *Olt pipeline:*

    Receive **forward/next** primitives, translate to openflow rules, send
    **group add/remove**, **flow add/remove** messages to OLT

2.  Subsystem Design

    1.  host State Machine

        Index： mvlan + group ip + devid , we use this index to indicate a host.
        And for each host, we implement a independent state machine, see the
        chart below, refer to RFC 2236

**Event description：**

1.  join/leave comes from packet-in message , received from the uni port

2.  query comes from packet-in message m received from the uplink port

**Action description：**

1.  send report/leave means send those IGMP packet to the uplink port of device;

2.  Timer:

    for join event, the timer is [0, **Unsolicited Report Interval**],
    **Unsolicited Report Interval** is an user configured value.

    for query event，the timer is [0, **Max Resp Time**], **Max Resp Time** is a
    field in query packet.

**Implementation：**

1.  For general query，retrieve all hosts which belong to the group, and send
    query message.

    For specific query, only send the message to the specific host

2.  If last member leave the group, the state machine will be removed , and a
    remove sink message will be sent to Cordmcast. **For performance purpose,
    ONOS will not really remove the rules. Just remove the output port from the
    group rule.**

3.  South host management

    1.  Description

South host management divided into 2 sub-module：Southbound **Hosts Management**
and **Northbound Reporter**，as the chart below：

### Southbound Hosts Management

Index: mvlan + group ip + devid + port

Action：

1.  When receive join/leave message to a new index, forward message to
    Northbound Reporter;

    Filter messages with same index

2.  When receive the leave message and the fast leave switch is turned off, send
    query to all hosts which belong to the same group. It is because maybe some
    hosts join into same group.

3.  Aging： we will send query message to each hosts in a fixed period, if no
    response, we will remove the host.

    1.  Northbound Reporter

Index：mvlan + groupip + devid

Action：

1.  Receive join leave messages from **Southbound Hosts Management.**

2.  If it is the first host that join into a specific group, send **add sink**
    to Cordmcast;

    If it is not the first host to join , just increase the group’s host
    counter;

    If it is the last host leave from the group, send **remove sink** to
    Cordmcast;

    If it is not the last host to leave, just decrease the group’s host counter.

    1.  IGMP packet encode/decode and dispatch

Action：

1.  Receive igmp message from device, decode it;

2.  If it is IGMP v3 reporter and include multiple groups, divided it into some
    packet with single group;

3.  Dispatch the packet to different subsystems.

The flow chart:

1.  External Interface

    1.  NBI REST-API

Parameter **Unsolicited TimeOut**, **Max Respose times**, **Keep Alive
Interval**, **Last Query Interval**, **Last Query Count**, **Fast Leave flags**
can be configured through REST-API provided by ONOS

Please refer to REST-API for ONOS
APP ([3HH-12876-0211-DDZZA](https://ct.web.alcatel-lucent.com/scm-lib4/show-entry.cgi?number=3HH-12876-0211-DDZZA))
for details

1.  SBI Openflow

    1.  Lift Rule:

| **type**    | **Call situation**                                   | **Match field** | **Match value**                                                                  | **Action field** | **Action value** |
|-------------|------------------------------------------------------|-----------------|----------------------------------------------------------------------------------|------------------|------------------|
| Add flow    | igmp app activate                                    | Ethernet        | IPV4                                                                             | Output port      | controller       |
|             |                                                      | Protocol        | IGMP                                                                             |                  |                  |
|             |                                                      | port            | {all activated port }                                                            |                  |                  |
| Delete flow | igmp app deactivate, or port state change to disable | Ethernet        | IPV4                                                                             |                  |                  |
|             |                                                      | Protocol        | IGMP                                                                             |                  |                  |
|             |                                                      | port            | {All activated port if app goes down, or the port which change state to disable} | N/A              |                  |

This flow rule is used for instructing olt to transmit the received igmp packets
to the controller. Once the igmp app is initialized , the device information is
registered in ONOS, and the port is enabled, Igmp app will send this flow rule
to the device for each of its activated port automatically.

The flow rules would be removed while igmpproxy app deactivated.

### Multicast Rule:

| **type**                | **Call situation** | **Match field**          | **Match value** | **Action field** | **Action value** |
|-------------------------|--------------------|--------------------------|-----------------|------------------|------------------|
| Group add               | Receive igmp join  | With group id as the key | N/A             | Output port      | {host port}      |
| Group mod (delete port) | Receive igmp leave | With group id as the key | N/A             | N/A              | N/A              |
| Add flow                | Receive igmp join  | Ethernet                 | IPV4            | Group            | Group id         |
|                         |                    | In Port                  | {uplink port}   |                  |                  |
|                         |                    | IPDst                    | {group address} |                  |                  |
|                         |                    | Vlan Id                  | mcast vlan      |                  |                  |

This flow rule is used for instructing olt to forward the igmp traffic from
uplink port to the ONU port. Once the **igmp join** packet was received and
processed succesfully , Igmp app will send this flow rule (a **group add**
message and a **flow add** message) to the port which report the igmp join;

If host request to leave a group, a **group mod** message will send to olt, and
the **out port** of group rule would be removed;

If host re-send join message to ONOS, a **group mod** message will send to olt,
which to add the **out port** again.

IPTV STB/Source packet interface
--------------------------------

| Packet Types                        | Description                                                                                                                                                                                     |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Membership Query Message            | Membership Queries are sent by IP multicast routers to query the multicast reception state of neighboring interfaces                                                                            |
| Version 3 Membership Report Message | Version 3 Membership Reports are sent by IP systems to report (to neighboring routers) the current multicast reception state, or changes in the multicast reception state, of their interfaces. |

**For more Igmp packet detail, please refer to RFC 3376**
