/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.specure.android.screens.main;

import android.location.Location;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.specure.android.util.net.InterfaceTrafficGatherer.TrafficClassificationEnum;
import timber.log.Timber;

/**
 * This class should contains global informations about phone state and network state
 */
public class InfoCollector implements Serializable {

    private Integer cpuCoresCount;
    private Long memTotal;
    private Long memFree;
    private Long loopModeRemainingTimeToNextTest;

    public void setSignalType(Integer signalType) {
        if (this.signalType != null && listener != null && !this.signalType.equals(signalType) || (this.signalType == null && signalType != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.SIGNAL_TYPE, this.signalType, signalType);
        }
        this.signalType = signalType;
    }

    public void setCpuCoresCount(Integer cpuCoresCount) {
        if (this.cpuCoresCount != null && listener != null && !(this.cpuCoresCount.intValue() == cpuCoresCount.intValue()) || (this.cpuCoresCount == null && cpuCoresCount != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.CPUS_COUNT, this.cpuCoresCount, cpuCoresCount);
        }
        this.cpuCoresCount = cpuCoresCount;
    }

    public void setMemTotal(Long memTotal) {
        if (this.memTotal != null && listener != null && !(this.memTotal.longValue() == memTotal.longValue()) || (this.memTotal == null && memTotal != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.MEMORY_TOTAL, this.memTotal, memTotal);
        }
        this.memTotal = memTotal;
    }

    public void setMemFree(Long memFree) {
        if (this.memFree != null && listener != null && !(this.memFree.longValue() == memFree.longValue()) || (this.memFree == null && memFree != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.MEMORY_FREE, this.memFree, memFree);
        }
        this.memFree = memFree;
    }

    public Integer getCpuCoresCount() {
        return cpuCoresCount;
    }

    public Long getMemTotal() {
        return memTotal;
    }

    public Long getMemFree() {
        return memFree;
    }

    public static enum InfoCollectorType {
        SIGNAL, SIGNAL_RSRQ, NETWORK_FAMILY, NETWORK_TYPE, NETWORK_NAME, LOCATION, IPV4, IPV6, UL_TRAFFIC, DL_TRAFFIC, CONTROL_SERVER_CONNECTION,
        CAPTIVE_PORTAL_STATUS, CONNECTION_STATUS, CPU, MEMORY, CELL_ID, SIGNAL_TYPE, LOOP_MODE, LOOP_MODE_FINISHED, CPUS_COUNT, MEMORY_TOTAL, MEMORY_FREE;
    }

    public static interface OnInformationChangedListener {
        void onInformationChanged(InfoCollectorType type, Object oldValue, Object newValue);
    }

    private static InfoCollector instance;

    /**
     *
     */
    private static final String DEBUG_TAG = "InfoCollector";

    private static final long serialVersionUID = 1L;
    private Integer signalRsrq;
    private Integer signal;
    private boolean isRsrqSignal = false;
    private boolean hasControlServerConnection;
    private boolean captivePortalFound = false;
    private float cpuUsage = 0f;
    private float memUsage = 0f;
    private String networkTypeString;
    private String networkFamily;
    private String networkName;
    private String ipv4;
    private String ipv6;
    private TrafficClassificationEnum ulTraffic;
    private TrafficClassificationEnum dlTraffic;
    private Location location;
    private List<OnInformationChangedListener> listener = new ArrayList<OnInformationChangedListener>();
    private String cellId;
    private Integer signalType;
    private Integer loopModeMax = 1;
    private Integer loopModeCurrent = 0;

    private InfoCollector() {
        //private Constructor
    }

    public static InfoCollector getInstance() {
        if (instance == null) {
            Timber.d("new Instance");
            instance = new InfoCollector();
        }
        return instance;
    }

    public Integer getSignalRsrq() {
        return signalRsrq;
    }

    public void setSignalRsrq(Integer signalRsrq) {
        if (this.signalRsrq != null && listener != null && !this.signalRsrq.equals(signalRsrq) || (this.signalRsrq == null && signalRsrq != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.SIGNAL_RSRQ, this.signalRsrq, signalRsrq);
        }
        if (signalRsrq != null) {
            isRsrqSignal = true;
        } else {
            isRsrqSignal = false;
        }
        this.signalRsrq = signalRsrq;
    }

    public void setLoopModeMaxTests(Integer loopModeMax) {
        if (this.loopModeMax != null && listener != null && !this.loopModeMax.equals(loopModeMax) || (this.loopModeMax == null && loopModeMax != null)) {
            this.loopModeMax = loopModeMax;
            dispatchInfoChangedEvent(InfoCollectorType.LOOP_MODE, this.loopModeMax, loopModeMax);
        } else {
            this.loopModeMax = loopModeMax;
        }
    }

    public void notifyLoopModeFinished() {
        dispatchInfoChangedEvent(InfoCollectorType.LOOP_MODE_FINISHED, 0, 0);
    }

    public void setLoopModeCurrentTest(Integer loopModeCurrent) {
        if (this.loopModeCurrent != null && listener != null && !this.loopModeCurrent.equals(loopModeCurrent) || (this.loopModeCurrent == null && loopModeCurrent != null)) {
            this.loopModeCurrent = loopModeCurrent;
            dispatchInfoChangedEvent(InfoCollectorType.LOOP_MODE, this.loopModeCurrent, loopModeCurrent);
        } else {
            this.loopModeCurrent = loopModeCurrent;
        }
    }

    public void setLoopModeRemainingTimeToNextTest(Long remainingTime) {
        if (this.loopModeRemainingTimeToNextTest != null && listener != null && !this.loopModeRemainingTimeToNextTest.equals(remainingTime) || (this.loopModeRemainingTimeToNextTest == null && remainingTime != null)) {
            this.loopModeRemainingTimeToNextTest = remainingTime;
            dispatchInfoChangedEvent(InfoCollectorType.LOOP_MODE, this.loopModeRemainingTimeToNextTest, remainingTime);
        } else {
            this.loopModeRemainingTimeToNextTest = remainingTime;
        }
    }

    public Integer getLoopModeMax() {
        return loopModeMax;
    }

    public Long getLoopModeRemainingTimeToNextTest() {
        return loopModeRemainingTimeToNextTest;
    }

    public Integer getLoopModeCurrent() {
        return loopModeCurrent;
    }

    public Integer getSignalType() {
        return signalType;
    }

    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        if (this.signal != null && listener != null && !this.signal.equals(signal) || (this.signal == null && signal != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.SIGNAL, this.signal, signal);
        }
        this.signal = signal;
    }

    public void setCellId(String cellId) {
        if (this.cellId != null && listener != null && !this.cellId.equals(cellId) || (this.cellId == null && signal != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.CELL_ID, this.cellId, cellId);
        }
        this.cellId = cellId;
    }

    public boolean isRsrqSignal() {
        return isRsrqSignal;
    }

    public String getNetworkTypeString() {
        return networkTypeString;
    }

    public void setNetworkTypeString(String networkTypeString) {
        if (this.networkTypeString != null && listener != null && !this.networkTypeString.equals(networkTypeString) || (this.networkTypeString == null && networkTypeString != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.NETWORK_TYPE, this.networkTypeString, networkTypeString);
        }
        this.networkTypeString = networkTypeString;
    }

    public String getNetworkFamily() {
        return networkFamily;
    }

    public void setNetworkFamily(String networkFamily) {
        if (this.networkFamily != null && listener != null && !this.networkFamily.equals(networkFamily) || (this.networkFamily == null && networkFamily != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.NETWORK_FAMILY, this.networkFamily, networkFamily);
        }
        this.networkFamily = networkFamily;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        if (this.networkName != null && listener != null && !this.networkName.equals(networkName) || (this.networkName == null && networkName != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.NETWORK_NAME, this.networkName, networkName);
        }
        this.networkName = networkName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        if (this.location != null && listener != null && !this.location.equals(location) || (this.location == null && location != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.LOCATION, this.location, location);
        }
        this.location = location;
    }

    public void setIpv4(String ip) {
        if (this.ipv4 != null && listener != null && !this.ipv4.equals(ip) || (this.ipv4 == null && ip != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.IPV4, this.ipv4, ip);
        }
        this.ipv4 = ip;
    }

    public void setIpv6(String ip) {
        if (this.ipv6 != null && listener != null && !this.ipv6.equals(ip) || (this.ipv6 == null && ip != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.IPV6, this.ipv6, ip);
        }
        this.ipv6 = ip;
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        if (listener != null && this.cpuUsage != cpuUsage) {
            dispatchInfoChangedEvent(InfoCollectorType.CPU, this.cpuUsage, cpuUsage);
        }
        this.cpuUsage = cpuUsage;
    }

    public float getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(float memUsage) {
        if (listener != null && this.memUsage != memUsage) {
            dispatchInfoChangedEvent(InfoCollectorType.MEMORY, this.memUsage, memUsage);
        }
        this.memUsage = memUsage;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public TrafficClassificationEnum getUlTraffic() {
        return ulTraffic;
    }

    public void setUlTraffic(TrafficClassificationEnum ulTraffic) {
        if (this.ulTraffic != null && listener != null && !this.ulTraffic.equals(ulTraffic) || (this.ulTraffic == null && ulTraffic != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.UL_TRAFFIC, this.ulTraffic, ulTraffic);
        }
        this.ulTraffic = ulTraffic;
    }

    public TrafficClassificationEnum getDlTraffic() {
        return dlTraffic;
    }

    public void setDlTraffic(TrafficClassificationEnum dlTraffic) {
        if (this.dlTraffic != null && listener != null && !this.dlTraffic.equals(dlTraffic) || (this.dlTraffic == null && dlTraffic != null)) {
            dispatchInfoChangedEvent(InfoCollectorType.DL_TRAFFIC, this.dlTraffic, dlTraffic);
        }
        this.dlTraffic = dlTraffic;
    }

    public boolean isHasControlServerConnection() {
        return hasControlServerConnection;
    }

    public void setHasControlServerConnection(boolean hasControlServerConnection) {
        if (listener != null && this.hasControlServerConnection != hasControlServerConnection) {
            dispatchInfoChangedEvent(InfoCollectorType.CONTROL_SERVER_CONNECTION, this.hasControlServerConnection, hasControlServerConnection);
        }
        this.hasControlServerConnection = hasControlServerConnection;
    }

    public boolean isCaptivePortalFound() {
        return captivePortalFound;
    }

    public void setCaptivePortalFound(boolean captivePortalFound) {
        if (listener != null && this.captivePortalFound != captivePortalFound) {
            dispatchInfoChangedEvent(InfoCollectorType.CAPTIVE_PORTAL_STATUS, this.captivePortalFound, captivePortalFound);
        }
        this.captivePortalFound = captivePortalFound;
    }

    public List<OnInformationChangedListener> getListenerList() {
        return listener;
    }

    public void addListener(OnInformationChangedListener listener) {
        if (!this.listener.contains(listener)) {
            this.listener.add(listener);
        }
    }

    public void removeListener(OnInformationChangedListener listener) {
        this.listener.remove(listener);
    }

    public void removeAllListeners() {
        this.listener.clear();
    }

    /**
     * @param type
     * @param oldValue
     * @param newValue
     */
    public void dispatchInfoChangedEvent(InfoCollectorType type, Object oldValue, Object newValue) {
        Timber.d("Dispatching Event: %s, Listeners: %s", type, listener.size());
        for (OnInformationChangedListener l : listener) {
            if (l != null) {
                l.onInformationChanged(type, oldValue, newValue);
            }
        }
    }

    /**
     *
     */
    public void refresh() {
        dispatchInfoChangedEvent(InfoCollectorType.LOCATION, null, getLocation());
        dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, getIpv4());
        dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, getIpv6());
    }

    /**
     *
     */
    public void refreshIpAndAntenna() {
        dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, getIpv4());
        dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, getIpv6());
    }
}
