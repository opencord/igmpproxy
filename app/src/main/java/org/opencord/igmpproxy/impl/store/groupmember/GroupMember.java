/*
 * Copyright 2017-2023 Open Networking Foundation (ONF) and the ONF Contributors
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
package org.opencord.igmpproxy.impl.store.groupmember;

import org.onlab.packet.IGMPMembership;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.VlanId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.GroupMemberId;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Date struct to keep Igmp member infomations.
 */
public final class GroupMember {

    private final VlanId vlan;
    private final GroupMemberId groupMemberId;
    private final boolean v2;
    private byte recordType = IGMPMembership.MODE_IS_INCLUDE;
    private ArrayList<Ip4Address> sourceList = new ArrayList<>();
    private int keepAliveQueryInterval = 0;
    private int keepAliveQueryCount = 0;
    private int lastQueryInterval = 0;
    private int lastQueryCount = 0;
    private boolean leave = false;

    public GroupMember(Ip4Address groupIp, VlanId vlan, DeviceId deviceId, PortNumber portNum, boolean isV2) {
        this.vlan = vlan;
        this.groupMemberId = GroupMemberId.of(groupIp, deviceId, portNum);
        v2 = isV2;
    }

    public String getkey() {
        return groupMemberId.toString();
    }

    public GroupMemberId getGroupMemberId() {
        return groupMemberId;
    }

    public VlanId getvlan() {
        return vlan;
    }

    public DeviceId getDeviceId() {
        return groupMemberId.getDeviceId();
    }

    public PortNumber getPortNumber() {
        return groupMemberId.getPortNum();
    }

    public Ip4Address getGroupIp() {
        return groupMemberId.getGroupIp();
    }

    public byte getRecordType() {
        return recordType;
    }

    public boolean getv2() {
        return v2;
    }

    public ArrayList<Ip4Address> getSourceList() {
        return sourceList;
    }


    public void updateList(byte recordType, ArrayList<Ip4Address> newSourceList) {
        this.recordType = recordType;
        this.sourceList.clear();
        this.sourceList.addAll(newSourceList);
    }

    /*join B to A (A+B)*/
    private void join(ArrayList<Integer> listA, ArrayList<Integer> listB) {
        Iterator<Integer> iterA = null;
        Iterator<Integer> iterB = listB.iterator();
        boolean exists;
        while (iterB.hasNext()) {
            iterA = listA.iterator();
            exists = false;
            int ipToAdd = iterB.next();
            while (iterA.hasNext()) {
                if (iterA.next().equals(ipToAdd)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                listA.add(ipToAdd);
            }
        }
    }

    /* include A and B (A*B)*/
    private void intersection(ArrayList<Integer> listA, ArrayList<Integer> listB) {
        Iterator<Integer> iterA = listA.iterator();
        Iterator<Integer> iterB;
        boolean exists;

        while (iterA.hasNext()) {
            iterB = listB.iterator();
            int ipToInclude = iterA.next();
            exists = false;
            while (iterB.hasNext()) {
                if (iterB.next().equals(ipToInclude)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                iterA.remove();
            }
        }
    }

    /*exclude B from A (A-B)*/
    private void exclude(ArrayList<Integer> listA, ArrayList<Integer> listB) {
        Iterator<Integer> iterA = null;
        Iterator<Integer> iterB = listB.iterator();

        while (iterB.hasNext()) {
            iterA = listA.iterator();
            int ipToDel = iterB.next();
            while (iterA.hasNext()) {
                if (iterA.next().equals(ipToDel)) {
                    iterA.remove();
                    break;
                }
            }
        }
    }

    public void setLeave(boolean l) {
        leave = l;
    }

    public boolean isLeave() {
        return leave;
    }

    public int getKeepAliveQueryInterval() {
        return keepAliveQueryInterval;
    }

    public int getKeepAliveQueryCount() {
        return keepAliveQueryCount;
    }

    public int getLastQueryInterval() {
        return lastQueryInterval;
    }

    public int getLastQueryCount() {
        return lastQueryCount;
    }

    public void keepAliveQueryCount(boolean add) {
        if (add) {
            keepAliveQueryCount++;
        } else {
            keepAliveQueryCount = 0;
        }
    }

    public void lastQueryCount(boolean add) {
        if (add) {
            lastQueryCount++;
        } else {
            lastQueryCount = 0;
        }
    }

    public void keepAliveInterval(boolean add) {
        if (add) {
            keepAliveQueryInterval++;
        } else {
            keepAliveQueryInterval = 0;
        }
    }

    public void lastQueryInterval(boolean add) {
        if (add) {
            lastQueryInterval++;
        } else {
            lastQueryInterval = 0;
        }
    }

    public void resetAllTimers() {
        keepAliveQueryInterval = 0;
        keepAliveQueryCount = 0;
        lastQueryInterval = 0;
        lastQueryCount = 0;
    }
}
