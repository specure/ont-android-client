/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
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
 *******************************************************************************/

package at.specure.android.api.jsons;

import android.telephony.TelephonyManager;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import at.specure.android.database.obj.TSignal;
import at.specure.android.util.InformationCollector;

public final class Signal {

    public static final transient int UNKNOWN = Integer.MIN_VALUE;
    public static final transient int NETWORK_WIFI = 99;

    @SerializedName("time")
    private Long time;

    /**
     *
     */
    @SerializedName("network_type_id")
    private Integer networkTypeId;

    @SerializedName("lte_rsrp")
    private Integer lteRsrp = null;           // signal strength value as RSRP, used in LTE

    @SerializedName("lte_rsrq")
    private Integer lteRsrq = null;           // signal quality RSRQ, used in LTE

    @SerializedName("lte_rssnr")
    private Integer lteRssnr = null;

    @SerializedName("lte_cqi")
    private Integer lteCqi = null;

    /**
     * used for wifi only
     */
    @SerializedName("wifi_link_speed")
    Integer wifiLinkSpeed = null; // e.g 702

    /**
     * used for wifi only
     */
    @SerializedName("wifi_rssi")
    Integer wifiRSSI = null; // e.g. -48


    /**
     * used for 2G/3G only
     */
    @SerializedName("signal_strength")
    private Integer signalStrength = null;

    /**
     * used for 2G/3G only
     */
    @SerializedName("gsm_bit_error_rate")
    private Integer gsmBitErrorRate = null;

    @SerializedName("time_ns")
    private Long timeNs;            // relative ts in ns


    /**
     * Use this for mobile network signal
     *
     * @param networkTypeId
     * @param lteRsrp
     * @param lteRsrq
     * @param lteRssnr
     * @param lteCqi
     * @param signalStrength
     * @param gsmBitErrorRate
     */
    public Signal(Integer networkTypeId, Integer lteRsrp, Integer lteRsrq, Integer lteRssnr, Integer lteCqi, Integer signalStrength, Integer gsmBitErrorRate) {
        this.time = System.currentTimeMillis(); // get from old object
        this.timeNs = System.nanoTime(); // get from old object
        if (this.networkTypeId != null && (this.networkTypeId == 40 || this.networkTypeId == 41)) {
            // in the case it is 40 or 41 we force to be LTE signal type (code 13)because of backend has trouble to handle it
            this.networkTypeId = 13;
        } else {
            this.networkTypeId = networkTypeId;
        }
        this.lteRsrp = (lteRsrp != null && lteRsrp != UNKNOWN) ? lteRsrp : null;
        this.lteRsrq = (lteRsrq != null && lteRsrq != UNKNOWN) ? lteRsrq : null;
        this.lteRssnr = (lteRssnr != null && lteRssnr != UNKNOWN) ? lteRssnr : null;
        this.lteCqi = (lteCqi != null && lteCqi != UNKNOWN) ? lteCqi : null;
        this.signalStrength = (signalStrength != null && signalStrength != UNKNOWN) ? signalStrength : null;
        this.gsmBitErrorRate = (gsmBitErrorRate != null && gsmBitErrorRate != UNKNOWN) ? gsmBitErrorRate : null;
    }

    /**
     * Use this for wifi signal
     *
     * @param wifiLinkSpeed
     * @param wifiRssi
     */
    public Signal(Integer wifiLinkSpeed, Integer wifiRssi) {
        this.time = System.currentTimeMillis(); // get from old object
        this.timeNs = System.nanoTime(); // get from old object
        this.networkTypeId = NETWORK_WIFI;
        this.wifiLinkSpeed = (wifiLinkSpeed != null && wifiLinkSpeed != UNKNOWN) ? wifiLinkSpeed : null;
        this.wifiRSSI = (wifiRssi != null && wifiRssi != UNKNOWN) ? wifiRssi : null;
    }


    /**
     * we save only mobile network for zero measurements
     *
     * @param signal
     */
    public Signal(TSignal signal) {
        this.time = signal.time;
        this.networkTypeId = signal.networkTypeId;
        this.lteRsrp = (lteRsrp != null && lteRsrp != UNKNOWN) ? lteRsrp : null;
        this.lteRsrq = (lteRsrq != null && lteRsrq != UNKNOWN) ? lteRsrq : null;
        this.lteRssnr = (lteRssnr != null && lteRssnr != UNKNOWN) ? lteRssnr : null;
        this.lteCqi = (lteCqi != null && lteCqi != UNKNOWN) ? lteCqi : null;
        this.signalStrength = (signalStrength != null && signalStrength != UNKNOWN) ? signalStrength : null;
        this.gsmBitErrorRate = (gsmBitErrorRate != null && gsmBitErrorRate != UNKNOWN) ? gsmBitErrorRate : null;
        this.timeNs = signal.timeAge;

        if (((signalStrength == null) || (signalStrength == 0)) && ((lteRsrp == null) || (lteRsrp == 0))) {
            this.signalStrength = null;
            this.gsmBitErrorRate = null;
            this.lteRsrp = null;
            this.lteRsrq = null;
            this.lteRssnr = null;
            this.lteCqi = null;
        }

        if ((lteRsrp == null) || (lteRsrp == 0)) {
            this.lteRsrp = null;
            this.lteRsrq = null;
            this.lteRssnr = null;
            this.lteCqi = null;
        }

        if (!(signal.networkTypeId == TelephonyManager.NETWORK_TYPE_LTE
                || signal.networkTypeId == 19)) { // NETWORK_TYPE_LTE_CA
            this.lteRsrp = null;
            this.lteRsrq = null;
            this.lteRssnr = null;
            this.lteCqi = null;
        }

        if (signalStrength != null && signalStrength == 0) {
            this.signalStrength = null;
            this.gsmBitErrorRate = null;
        }

    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getNetworkTypeId() {
        return networkTypeId;
    }


    public Integer getLteRsrp() {
        return lteRsrp;
    }


    public Integer getLteRsrq() {
        return lteRsrq;
    }


    public Integer getLteRssnr() {
        return lteRssnr;
    }


    public Integer getLteCqi() {
        return lteCqi;
    }


    public Integer getSignalStrength() {
        return signalStrength;
    }


    public Integer getGsmBitErrorRate() {
        return gsmBitErrorRate;
    }

    public Long getTimeNs() {
        return timeNs;
    }

    public void setTimeNs(Long timeNs) {
        this.timeNs = timeNs;
    }
}
