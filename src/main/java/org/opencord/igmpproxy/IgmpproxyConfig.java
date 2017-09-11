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
package org.opencord.igmpproxy;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.basics.BasicElementConfig;

/**
 * Net configuration class for igmpproxy.
 */
public class IgmpproxyConfig extends Config<ApplicationId> {
    protected static final String DEFAULT_UNSOLICITED_TIMEOUT = "2";
    protected static final String DEFAULT_MAX_RESP = "10";
    protected static final String DEFAULT_KEEP_ALIVE_INTERVAL = "120";
    protected static final String DEFAULT_KEEP_ALIVE_COUNT = "3";
    protected static final String DEFAULT_LAST_QUERY_INTERVAL = "2";
    protected static final String DEFAULT_LAST_QUERY_COUNT = "2";
    protected static final String DEFAULT_IGMP_COS = "7";
    protected static final Boolean DEFAULT_FAST_LEAVE = false;
    protected static final Boolean DEFAULT_PERIODIC_QUERY = true;
    protected static final String DEFAULT_WITH_RA_UPLINK = "true";
    protected static final String DEFAULT_WITH_RA_DOWNLINK = "true";
    private static final Boolean DEFAULT_CONNECT_POINT_MODE = true;
    private static final Boolean DEFAULT_PIMSSM_INTERWORKING = false;

    protected static final String CONNECT_POINT_MODE = "globalConnectPointMode";
    protected static final String CONNECT_POINT = "globalConnectPoint";
    private static final String UNSOLICITED_TIMEOUT = "UnsolicitedTimeOut";
    private static final String MAX_RESP = "MaxResp";
    private static final String KEEP_ALIVE_INTERVAL = "KeepAliveInterval";
    private static final String KEEP_ALIVE_COUNT = "KeepAliveCount";
    private static final String LAST_QUERY_INTERVAL = "LastQueryInterval";
    private static final String LAST_QUERY_COUNT = "LastQueryCount";
    private static final String FAST_LEAVE = "FastLeave";
    private static final String PERIODIC_QUERY = "PeriodicQuery";
    private static final String IGMP_COS = "IgmpCos";
    private static final String WITH_RA_UPLINK = "withRAUpLink";
    private static final String WITH_RA_DOWN_LINK = "withRADownLink";
    private static final String PIMSSM_INTERWORKING = "pimSSmInterworking";

    /**
     * Gets the value of a string property, protecting for an empty
     * JSON object.
     *
     * @param name name of the property
     * @param defaultValue default value if none has been specified
     * @return String value if one os found, default value otherwise
     */
    private String getStringProperty(String name, String defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        return get(name, defaultValue);
    }

    public int unsolicitedTimeOut() {
        return Integer.parseInt(getStringProperty(UNSOLICITED_TIMEOUT, DEFAULT_UNSOLICITED_TIMEOUT));
    }

    public BasicElementConfig unsolicitedTimeOut(int timeout) {
        return (BasicElementConfig) setOrClear(UNSOLICITED_TIMEOUT, timeout);
    }

    public int maxResp() {
        return Integer.parseInt(getStringProperty(MAX_RESP, DEFAULT_MAX_RESP));
    }

    public BasicElementConfig maxResp(int maxResp) {
        return (BasicElementConfig) setOrClear(MAX_RESP, maxResp);
    }

    public int keepAliveInterval() {
        return Integer.parseInt(getStringProperty(KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_INTERVAL));
    }

    public BasicElementConfig keepAliveInterval(int interval) {
        return (BasicElementConfig) setOrClear(KEEP_ALIVE_INTERVAL, interval);
    }

    public int keepAliveCount() {
        return Integer.parseInt(getStringProperty(KEEP_ALIVE_COUNT, DEFAULT_KEEP_ALIVE_COUNT));
    }

    public BasicElementConfig keepAliveCount(int count) {
        return (BasicElementConfig) setOrClear(KEEP_ALIVE_COUNT, count);
    }

    public int lastQueryInterval() {
        return Integer.parseInt(getStringProperty(LAST_QUERY_INTERVAL, DEFAULT_LAST_QUERY_INTERVAL));
    }

    public BasicElementConfig lastQueryInterval(int interval) {
        return (BasicElementConfig) setOrClear(LAST_QUERY_INTERVAL, interval);
    }

    public int lastQueryCount() {
        return Integer.parseInt(getStringProperty(LAST_QUERY_COUNT, DEFAULT_LAST_QUERY_COUNT));
    }

    public BasicElementConfig lastQueryCount(int count) {
        return (BasicElementConfig) setOrClear(LAST_QUERY_COUNT, count);
    }

    public boolean fastLeave() {
        if (object == null || object.path(FAST_LEAVE) == null) {
            return DEFAULT_FAST_LEAVE;
        }
        return Boolean.parseBoolean(getStringProperty(FAST_LEAVE, DEFAULT_FAST_LEAVE.toString()));
    }

    public BasicElementConfig fastLeave(boolean fastLeave) {
        return (BasicElementConfig) setOrClear(FAST_LEAVE, fastLeave);
    }

    public boolean periodicQuery() {
        if (object == null || object.path(PERIODIC_QUERY) == null) {
            return DEFAULT_PERIODIC_QUERY;
        }
        return Boolean.parseBoolean(getStringProperty(PERIODIC_QUERY, DEFAULT_PERIODIC_QUERY.toString()));
    }

    public BasicElementConfig periodicQuery(boolean periodicQuery) {
        return (BasicElementConfig) setOrClear(PERIODIC_QUERY, periodicQuery);
    }

    public byte igmpCos() {
        return Byte.parseByte(getStringProperty(IGMP_COS, DEFAULT_IGMP_COS));
    }

    public boolean withRAUplink() {
        if (object == null || object.path(WITH_RA_UPLINK) == null) {
            return true;
        }
        return Boolean.parseBoolean(getStringProperty(WITH_RA_UPLINK, DEFAULT_WITH_RA_UPLINK));
    }

    public boolean withRADownlink() {
        if (object == null || object.path(WITH_RA_DOWN_LINK) == null) {
            return false;
        }
        return Boolean.parseBoolean(getStringProperty(WITH_RA_DOWN_LINK, DEFAULT_WITH_RA_DOWNLINK));
    }

    public boolean connectPointMode() {
        if (object == null || object.path(CONNECT_POINT_MODE) == null) {
            return DEFAULT_CONNECT_POINT_MODE;
        }
        return Boolean.parseBoolean(getStringProperty(CONNECT_POINT_MODE, DEFAULT_CONNECT_POINT_MODE.toString()));
    }

    public ConnectPoint connectPoint() {
        if (object == null || object.path(CONNECT_POINT) == null) {
            return null;
        }

        try {
            return ConnectPoint.deviceConnectPoint(getStringProperty(CONNECT_POINT, ""));
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean pimSsmInterworking() {
        if (object == null || object.path(PIMSSM_INTERWORKING) == null) {
            return DEFAULT_PIMSSM_INTERWORKING;
        }
        return Boolean.parseBoolean(getStringProperty(PIMSSM_INTERWORKING, DEFAULT_PIMSSM_INTERWORKING.toString()));
    }
}
