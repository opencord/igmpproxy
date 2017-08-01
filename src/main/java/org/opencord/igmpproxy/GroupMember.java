/*
 * Copyright 2017-present Open Networking Laboratory
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

import org.onlab.packet.IGMPMembership;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.VlanId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Date struct to keep Igmp member infomations.
 */
public final class  GroupMember {

    private final VlanId vlan;
    private final DeviceId deviceId;
    private final PortNumber portNumber;
    private final Ip4Address groupIp;
    private final boolean v2;
    private byte recordType = IGMPMembership.MODE_IS_INCLUDE;
    private ArrayList<Ip4Address> sourceList = new ArrayList<>();
    private int keepAliveQueryInterval = 0;
    private int keepAliveQueryCount = 0;
    private int lastQueryInterval = 0;
    private int lastQueryCount = 0;
    private boolean leave = false;

    public GroupMember(Ip4Address groupIp, VlanId vlan, DeviceId deviceId, PortNumber portNum, boolean isV2) {
        this.groupIp = groupIp;
        this.vlan = vlan;
        this.deviceId = deviceId;
        this.portNumber = portNum;
        v2 = isV2;
    }

    static String getkey(Ip4Address groupIp, DeviceId deviceId, PortNumber portNum) {
        return groupIp.toString() + deviceId.toString() + portNum.toString();
    }

    public String getkey() {
        return GroupMember.getkey(groupIp, deviceId, portNumber);
    }

    public String getId() {
        return getkey();
    }

    public VlanId getvlan() {
        return vlan;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public PortNumber getPortNumber() {
        return portNumber;
    }

    public Ip4Address getGroupIp() {
        return groupIp;
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

        /*TODO : support SSM
        if (this.recordType == IGMPMembership.MODE_IS_INCLUDE) {
            switch (recordType) {
                case IGMPMembership.MODE_IS_INCLUDE:
                case IGMPMembership.CHANGE_TO_INCLUDE_MODE:
                    //however , set to include<B> anyway
                    this.sourceList = sourceList;
                    this.recordType = IGMPMembership.MODE_IS_INCLUDE;
                    break;
                case IGMPMembership.MODE_IS_EXCLUDE:
                case IGMPMembership.CHANGE_TO_EXCLUDE_MODE:
                    //set to exclude<B>
                    this.sourceList = sourceList;
                    this.recordType = IGMPMembership.MODE_IS_EXCLUDE;
                    break;
                case IGMPMembership.ALLOW_NEW_SOURCES:
                    //set to include <A+B>
                    join(this.sourceList, sourceList);
                    break;
                case IGMPMembership.BLOCK_OLD_SOURCES:
                    //set to include <A-B>
                    exclude(this.sourceList, sourceList);
                    break;
                default:
                    break;
            }
        } else if (this.recordType == IGMPMembership.MODE_IS_EXCLUDE) {
            switch (recordType) {
                case IGMPMembership.MODE_IS_INCLUDE:
                case IGMPMembership.CHANGE_TO_INCLUDE_MODE:
                    //set to include<B>
                    this.recordType = IGMPMembership.MODE_IS_INCLUDE;
                    this.sourceList = sourceList;
                    break;
                case IGMPMembership.MODE_IS_EXCLUDE:
                case IGMPMembership.CHANGE_TO_EXCLUDE_MODE:
                    this.sourceList = sourceList;
                    this.recordType = IGMPMembership.MODE_IS_EXCLUDE;
                    break;
                case IGMPMembership.ALLOW_NEW_SOURCES:
                    //set to exclude <A-B>
                    exclude(this.sourceList, sourceList);
                    break;
                case IGMPMembership.BLOCK_OLD_SOURCES:
                    //set to exclude <A+B>
                    join(this.sourceList, sourceList);
                    break;
                default:
                    break;
            }
        }*/

        return;
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
