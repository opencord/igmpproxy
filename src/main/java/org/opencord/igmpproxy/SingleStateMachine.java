package org.opencord.igmpproxy;

import org.onlab.packet.Ethernet;
import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;

import java.util.Random;

public class SingleStateMachine {
    static final int STATE_NON = 0;
    static final int STATE_DELAY = 1;
    static final int STATE_IDLE = 2;
    static final int TRANSITION_JOIN = 0;
    static final int TRANSITION_LEAVE = 1;
    static final int TRANSITION_QUERY = 2;
    static final int TRANSITION_TIMEOUT = 3;
    public DeviceId devId;
    public Ip4Address groupIp;
    public int count = 1;
    public Ip4Address srcIp;
    private int timerId = IgmpTimer.INVALID_TIMER_ID;
    private int timeOut = 0xfffffff;
    private Ip4Address queryIp = null;
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

    public void setQueryIp(Ip4Address ip) {
        queryIp = ip;
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

    public void query(int maxResp, Ip4Address queryIp) {
        this.setQueryIp(queryIp);
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
            if (queryIp != null) {
                Ethernet eth = IgmpSender.getInstance().buildIgmpV3ResponseQuery(groupIp, srcIp);
                IgmpSender.getInstance().sendIgmpPacketUplink(eth, devId);
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
