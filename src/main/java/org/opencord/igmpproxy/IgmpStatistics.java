/*
 * Copyright 2018-present Open Networking Foundation
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
package org.opencord.igmpproxy;

import java.util.concurrent.atomic.AtomicLong;

public class IgmpStatistics {

     //Counter for unknown igmp type
     private AtomicLong unknownIgmpTypePacketsRxCounter = new AtomicLong();
     // Counter for igmp report with wrong mode.
     private AtomicLong reportsRxWithWrongModeCounter = new AtomicLong();
     // Counter for failed join due to insufficient permission access
     private AtomicLong failJoinReqInsuffPermissionAccessCounter = new AtomicLong();
     // Counter for invalid group ip address i.e not a valid multicast address.
     private AtomicLong failJoinReqUnknownMulticastIpCounter = new AtomicLong();
     // Counter for unconfigured group
     private AtomicLong unconfiguredGroupCounter = new AtomicLong();
     // Counter for valid igmp packet
     private AtomicLong validIgmpPacketCounter = new AtomicLong();
     // Counter for current number of igmp channel joins
     private AtomicLong igmpChannelJoinCounter = new AtomicLong();
     // Counter for current group number
     private AtomicLong currentGrpNumCounter = new AtomicLong();

     public Long getValidIgmpPacketCounter() {
          return validIgmpPacketCounter.get();
     }

     public void incrementValidIgmpPacketCounter() {
          validIgmpPacketCounter.incrementAndGet();
     }

     public Long getCurrentGrpNumCounter() {
         return currentGrpNumCounter.get();
     }

     public void incrementCurrentGrpNumCounter() {
          currentGrpNumCounter.incrementAndGet();
     }

     public Long getIgmpChannelJoinCounter() {
          return igmpChannelJoinCounter.get();
     }

     public void incrementIgmpChannelJoinCounter() {
          igmpChannelJoinCounter.incrementAndGet();
     }

     public Long getUnconfiguredGroupCounter() {
          return unconfiguredGroupCounter.get();
     }

     public void incrementUnconfiguredGroupCounter() {
          unconfiguredGroupCounter.incrementAndGet();
     }

     public Long getFailJoinReqUnknownMulticastIpCounter() {
          return failJoinReqUnknownMulticastIpCounter.get();
     }

     public void incrementFailJoinReqUnknownMulticastIpCounter() {
          failJoinReqUnknownMulticastIpCounter.incrementAndGet();
     }

     public Long getFailJoinReqInsuffPermissionAccessCounter() {
          return failJoinReqInsuffPermissionAccessCounter.get();
     }

     public void incrementFailJoinReqInsuffPermissionAccessCounter() {
          failJoinReqInsuffPermissionAccessCounter.incrementAndGet();
     }

     public Long getReportsRxWithWrongModeCounter() {
          return reportsRxWithWrongModeCounter.get();
     }

     public Long getUnknownIgmpTypePacketsRxCounter() {
          return unknownIgmpTypePacketsRxCounter.get();
     }

     public void incrementUnknownIgmpTypePacketsRxCounter() {
          unknownIgmpTypePacketsRxCounter.incrementAndGet();
     }

     public void incrementReportsRxWithWrongModeCounter() {
          reportsRxWithWrongModeCounter.incrementAndGet();
     }
}