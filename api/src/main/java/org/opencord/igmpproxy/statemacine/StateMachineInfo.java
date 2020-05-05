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
package org.opencord.igmpproxy.statemacine;

import org.onlab.packet.Ip4Address;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.opencord.igmpproxy.timer.IgmpTimerStore;

public class StateMachineInfo {
    public static final int DEFAULT_MAX_RESP = 0xfffffff;

    private StateMachineId stateMachineId;
    private int currentState;

    private long timerId;
    private Ip4Address srcIp;
    private PortNumber upLinkPort;
    private int timeOut;

    private StateMachineInfo() {
    }

    public StateMachineInfo(DeviceId deviceId, Ip4Address groupIp, Ip4Address srcIp, PortNumber upLinkPort) {
        this.stateMachineId = StateMachineId.of(deviceId, groupIp);
        this.srcIp = srcIp;
        this.upLinkPort = upLinkPort;
        this.currentState = StateConstants.STATE_NON;
        this.timerId = IgmpTimerStore.INVALID_TIMER_ID;
        this.timeOut = DEFAULT_MAX_RESP;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public long getTimerId() {
        return timerId;
    }

    public void setTimerId(long timerId) {
        this.timerId = timerId;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public StateMachineId getStateMachineId() {
        return stateMachineId;
    }

    public Ip4Address getSrcIp() {
        return srcIp;
    }

    public PortNumber getUpLinkPort() {
        return upLinkPort;
    }
}
