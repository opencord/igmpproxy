package org.opencord.igmpproxy.impl.store.timer;

import com.google.common.collect.Maps;
import org.opencord.igmpproxy.statemacine.StateMachineId;
import org.opencord.igmpproxy.timer.IgmpTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleIgmpTimerStore extends AbstractIgmpTimerStore {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AtomicLong atomicTimerId;

    @Override
    public void activate() {
        this.timerMap = Maps.newConcurrentMap();
        atomicTimerId = new AtomicLong(INVALID_TIMER_ID + 1L);
        log.info("{} started.", getClass().getSimpleName());
    }

    @Override
    public void deactivate() {
        log.info("{} stopped.", getClass().getSimpleName());
    }


    @Override
    public IgmpTimer createTimer(StateMachineId machineId, int timeout) {
        IgmpTimer igmpTimer = super.createTimer(machineId, timeout);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_ADDED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public IgmpTimer updateTimer(IgmpTimer timer) {
        IgmpTimer igmpTimer = super.updateTimer(timer);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_UPDATED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public IgmpTimer destroyTimer(long timerId) {
        IgmpTimer igmpTimer = super.destroyTimer(timerId);
        notifyDelegate(new InternalIgmpTimerEvent(
                InternalIgmpTimerEvent.Type.IGMP_TIMER_REMOVED, igmpTimer));
        return igmpTimer;
    }

    @Override
    public long getTimerId() {
        return atomicTimerId.getAndIncrement();
    }

}
