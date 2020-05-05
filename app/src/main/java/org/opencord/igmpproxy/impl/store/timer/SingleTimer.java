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
