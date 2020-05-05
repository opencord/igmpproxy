package org.opencord.igmpproxy.impl.store.timer;

import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.timer.IgmpTimer;

public class SingleTimer implements IgmpTimer {
    private int timeOut;  // unit is 1 second
    private StateMachineId stateMachineId;
    private final long timerId;

    public SingleTimer(long timerId, int timeOut, StateMachineId stateMachineId) {
        this.timerId = timerId;
        this.timeOut = timeOut;
        this.stateMachineId = stateMachineId;
    }

    @Override
    public long getId() {
        return timerId;
    }

    public int getTimeOut() {
        return timeOut;
    }

    @Override
    public void increaseTimeOut() {
        timeOut++;
    }

    @Override
    public void decreaseTimeOut() {
        timeOut--;
    }

    @Override
    public StateMachineId getStateMachineId() {
        return stateMachineId;
    }

}
