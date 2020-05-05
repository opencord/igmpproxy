package org.opencord.igmpproxy.timer;

import org.opencord.igmpproxy.statemacine.StateMachineId;

public interface IgmpTimer {
    long getId();

    int getTimeOut();

    void increaseTimeOut();

    void decreaseTimeOut();

    StateMachineId getStateMachineId();
}
