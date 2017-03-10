package org.opencord.igmpproxy;

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.Random;

/**
 * State machine for single IGMP group member. The state machine is implemented on 
 * RFC 2236 "6. Host State Diagram".
 */
public class SingleStateMachine {
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

    private int count = DEFAULT_COUNT;
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


    public DeviceId getDeviceId() {
        return devId;
    }
    public boolean increaseCounter() {
        count++;
        return true;
    }

    public boolean decreaseCounter() {
        if (count > 0) {
            count--;
            return true;
        } else {
            return false;
        }
    }

    public int getCounter() {
        return count;
    }
    public int currentState() {
        return currentState;
    }

    private void next(int msg) {
        currentState = transition[currentState][msg];
    }

    public void join() {
        states[currentState].join();
        next(TRANSITION_JOIN);
    }

    public void leave() {
        states[currentState].leave();
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
        public void join() {
        }

        public void leave() {
            Ethernet eth = IgmpSender.getInstance().buildIgmpV3Leave(groupIp, srcIp);
            IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
        }

        public void query(int maxResp) {
        }

        public void timeOut() {
        }

    }

    class NonMember extends State {
        public void join() {
            Ethernet eth = IgmpSender.getInstance().buildIgmpV3Join(groupIp, srcIp);
            IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
            timeOut = getTimeOut(IgmpManager.getUnsolicitedTimeout());
            timerId = IgmpTimer.start(SingleStateMachine.this, timeOut);
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
            Ethernet eth = IgmpSender.getInstance().buildIgmpV3ResponseQuery(groupIp, srcIp);
            IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
            timeOut = DEFAULT_MAX_RESP;
        }

    }

    class IdleMember extends State {
        public void query(int maxResp) {
            timeOut = getTimeOut(maxResp);
            timerId = IgmpTimer.start(SingleStateMachine.this, timeOut);
        }
    }
}
