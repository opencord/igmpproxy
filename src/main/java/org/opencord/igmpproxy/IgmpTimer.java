package org.opencord.igmpproxy;

import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implement the timer for igmp state machine.
 */
public final class IgmpTimer {

    public static final int INVALID_TIMER_ID = 0;
    public static int timerId = INVALID_TIMER_ID + 1;
    private static Map<Integer, SingleTimer> igmpTimerMap = Maps.newConcurrentMap();

    private IgmpTimer(){

    }
    private static int getId() {
        return timerId++;
    }

    public static int start(SingleStateMachine machine, int timeOut) {
        int id = getId();
        igmpTimerMap.put(id, new SingleTimer(machine, timeOut));
        return id;
    }

    public static int reset(int oldId, SingleStateMachine machine, int timeOut) {
        igmpTimerMap.remove(new Integer(oldId));
        int id = getId();
        igmpTimerMap.put(new Integer(id), new SingleTimer(machine, timeOut));
        return id;
    }

    public static void cancel(int id) {
        igmpTimerMap.remove(new Integer(id));
    }


    static void timeOut1s() {
        Set mapSet = igmpTimerMap.entrySet();
        Iterator itr = mapSet.iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            SingleTimer single = (SingleTimer) entry.getValue();
            if (single.timeOut > 0) {
                single.timeOut--;
            } else {
                single.machine.timeOut();
                itr.remove();
            }
        }
    }

    static class SingleTimer {

        public int timeOut;  // unit is 1 second
        public SingleStateMachine machine;

        public SingleTimer(SingleStateMachine machine, int timeOut) {
            this.machine = machine;
            this.timeOut = timeOut;
        }

    }
}
