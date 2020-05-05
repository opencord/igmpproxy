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
package org.opencord.igmpproxy.statemachine;

/**
 * State of machine.
 */
public interface State {
    /**
     * Default state.
     */
    int STATE_NON = 0;
    /**
     * Delay state.
     */
    int STATE_DELAY = 1;
    /**
     * Idle state.
     */
    int STATE_IDLE = 2;

    /**
     * Transition to join.
     */
    int TRANSITION_JOIN = 0;
    /**
     * Transition to leave.
     */
    int TRANSITION_LEAVE = 1;
    /**
     * Transition to query.
     */
    int TRANSITION_QUERY = 2;
    /**
     * Transition to timeout.
     */
    int TRANSITION_TIMEOUT = 3;

    /**
     * Makes the requirements of join request.
     */
    void join();

    /**
     * Makes the requirements of leave request.
     */
    void leave();

    /**
     * Makes the requirements of query request.
     *
     * @param maxResp maximum resp
     */
    void query(int maxResp);

    /**
     * Makes the requirements of timeout .
     */
    void timeOut();
}
