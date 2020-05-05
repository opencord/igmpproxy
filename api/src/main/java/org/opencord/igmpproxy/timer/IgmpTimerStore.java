package org.opencord.igmpproxy.timer;

import org.opencord.igmpproxy.statemacine.StateMachineId;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IgmpTimerStore {
    long INVALID_TIMER_ID = 0L;

    IgmpTimer createTimer(StateMachineId machineId, int timeout);

    IgmpTimer updateTimer(IgmpTimer timer);

    IgmpTimer destroyTimer(long timerId);

    long resetTimer(long oldTimerId, StateMachineId stateMachineId, int timeout);

    long getTimerId();

    Collection<IgmpTimer> getAll();

    Set<Map.Entry<Long, IgmpTimer>> entrySet();
}
