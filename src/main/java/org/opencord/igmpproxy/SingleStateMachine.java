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

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * State machine for single IGMP group member. The state machine is implemented on
 * RFC 2236 "6. Host State Diagram".
 */
public class SingleStateMachine {
    // Only for tests purposes
    static boolean sendQuery = true;

    static final int STATE_NON = 0;
    static final int STATE_DELAY = 1;
    static final int STATE_IDLE = 2;
    static final int TRANSITION_JOIN = 0;
    static final int TRANSITION_LEAVE = 1;
    static final int TRANSITION_QUERY = 2;
    static final int TRANSITION_TIMEOUT = 3;
    static final int DEFAULT_MAX_RESP = 0xfffffff;
    static final int DEFAULT_COUNT = 1;
    private DeviceId devId;
    private Ip4Address groupIp;
    private Ip4Address srcIp;

    private AtomicInteger count = new AtomicInteger(DEFAULT_COUNT);
    private int timerId = IgmpTimer.INVALID_TIMER_ID;
    private int timeOut = DEFAULT_MAX_RESP;
    private State[] states =
            {
                    new NonMember(), new DelayMember(), new IdleMember()
            };
    private int[] nonTransition =
            {STATE_DELAY, STATE_NON, STATE_NON, STATE_NON};
    private int[] delayTransition =
            {STATE_DELAY, STATE_NON, STATE_DELAY, STATE_IDLE};
    private int[] idleTransition =
            {STATE_IDLE, STATE_NON, STATE_DELAY, STATE_IDLE};
    //THE TRANSITION TABLE
    private int[][] transition =
            {nonTransition, delayTransition, idleTransition};
    private int currentState = STATE_NON;

    public SingleStateMachine(DeviceId devId, Ip4Address groupIp, Ip4Address src) {
        this.devId = devId;
        this.groupIp = groupIp;
        this.srcIp = src;
    }

    public Ip4Address getGroupIp() {
        return groupIp;
    }

    public DeviceId getDeviceId() {
        return devId;
    }
    public boolean increaseCounter() {
        count.incrementAndGet();
        return true;
    }

    public boolean decreaseCounter() {
        if (count.get() > 0) {
            count.decrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    public int getCounter() {
        return count.get();
    }
    public int currentState() {
        return currentState;
    }

    private void next(int msg) {
        currentState = transition[currentState][msg];
    }

    public void join(boolean messageOutAllowed) {
        states[currentState].join(messageOutAllowed);
        next(TRANSITION_JOIN);
    }

    public void leave(boolean messageOutAllowed) {
        states[currentState].leave(messageOutAllowed);
        next(TRANSITION_LEAVE);
    }

    public void query(int maxResp) {
        states[currentState].query(maxResp);
        next(TRANSITION_QUERY);
    }

    public void timeOut() {
        states[currentState].timeOut();
        next(TRANSITION_TIMEOUT);
    }

    int getTimeOut(int maxTimeOut) {
        Random random = new Random();
        return Math.abs(random.nextInt()) % maxTimeOut;
    }

    protected void cancelTimer() {
        if (IgmpTimer.INVALID_TIMER_ID != timerId) {
            IgmpTimer.cancel(timerId);
        }
    }

    class State {
        public void join(boolean messageOutAllowed) {
        }

        public void leave(boolean messageOutAllowed) {
            if (messageOutAllowed) {
                Ethernet eth = IgmpSender.getInstance().buildIgmpV3Leave(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
            }
        }

        public void query(int maxResp) {
        }

        public void timeOut() {
        }

    }

    class NonMember extends State {
        public void join(boolean messageOutAllowed) {
            if (messageOutAllowed) {
                Ethernet eth = IgmpSender.getInstance().buildIgmpV3Join(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
                timeOut = getTimeOut(IgmpManager.getUnsolicitedTimeout());
                timerId = IgmpTimer.start(SingleStateMachine.this, timeOut);
            }
        }
    }

    class DelayMember extends State {
        public void query(int maxResp) {
            if (maxResp < timeOut) {
                timeOut = getTimeOut(maxResp);
                timerId = IgmpTimer.reset(timerId, SingleStateMachine.this, timeOut);
            }
        }

        public void timeOut() {
            if (sendQuery) {
                Ethernet eth = IgmpSender.getInstance().buildIgmpV3ResponseQuery(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
                timeOut = DEFAULT_MAX_RESP;
            }
        }

    }

    class IdleMember extends State {
        public void query(int maxResp) {
            timeOut = getTimeOut(maxResp);
            timerId = IgmpTimer.start(SingleStateMachine.this, timeOut);
        }
    }
}
