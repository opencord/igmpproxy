/*
 * Copyright 2019-present Open Networking Foundation
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

package org.opencord.igmpproxy.config;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import static com.google.common.base.Preconditions.checkNotNull;

public class AggregatorDeviceData {
    private static final String DEVICE_ID_MISSING = "Device ID cannot be null";
    private static final String UPLINK_MISSING = "Uplink cannot be null";

    private final DeviceId deviceId;
    private final PortNumber uplink;

    /**
     * Class constructor.
     *
     * @param deviceId aggregator device ID
     * @param uplink   uplink port number
     */
    public AggregatorDeviceData(DeviceId deviceId, PortNumber uplink) {
        this.deviceId = checkNotNull(deviceId, DEVICE_ID_MISSING);
        this.uplink = checkNotNull(uplink, UPLINK_MISSING);
    }

    /**
     * Retrieves the aggregator device ID.
     *
     * @return device ID
     */
    public DeviceId deviceId() {
        return deviceId;
    }

    /**
     * Retrieves the uplink port number.
     *
     * @return port number
     */
    public PortNumber uplink() {
        return uplink;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("deviceId", deviceId)
                .add("uplink", uplink)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AggregatorDeviceData that = (AggregatorDeviceData) o;
        return Objects.equal(deviceId, that.deviceId) &&
                Objects.equal(uplink, that.uplink);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(deviceId, uplink);
    }
}
