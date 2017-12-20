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

import com.google.gson.annotations.SerializedName;

import at.specure.android.database.obj.TSignal;

public final class Signal {

    @SerializedName("time")
    private Long time;

    /**
     *
     */
    @SerializedName("network_type_id")
    private Integer networkTypeId;

    @SerializedName("lte_rsrp")
    private Integer lteRsrp;           // signal strength value as RSRP, used in LTE

    @SerializedName("lte_rsrq")
    private Integer lteRsrq;           // signal quality RSRQ, used in LTE

    @SerializedName("lte_rssnr")
    private Integer lteRssnr;

    @SerializedName("lte_cqi")
    private Integer lteCqi;

    /**
     * used for 2G/3G only
     */
    @SerializedName("signal_strength")
    private Integer signalStrength;

    /**
     * used for 2G/3G only
     */
    @SerializedName("gsm_bit_error_rate")
    private Integer gsmBitErrorRate;

    @SerializedName("time_ns")
    private Long timeNs;            // relative ts in ns


    public Signal(Long time, Integer networkTypeId, Integer lteRsrp, Integer lteRsrq, Integer lteRssnr, Integer lteCqi, Integer signalStrength, Integer gsmBitErrorRate, Long timeNs) {
        this.time = time;
        this.networkTypeId = networkTypeId;
        this.lteRsrp = lteRsrp;
        this.lteRsrq = lteRsrq;
        this.lteRssnr = lteRssnr;
        this.lteCqi = lteCqi;
        this.signalStrength = signalStrength;
        this.gsmBitErrorRate = gsmBitErrorRate;
        this.timeNs = timeNs;
    }

    public Signal(TSignal signal) {
        this.time = signal.time;
        this.networkTypeId = signal.networkTypeId;
        this.lteRsrp = signal.lteRSRP;
        this.lteRsrq = signal.lteRSRQ;
        this.lteRssnr = signal.lteRSSNR;
        this.lteCqi = signal.lteCQI;
        this.signalStrength = signal.signalStrength;
        this.gsmBitErrorRate = signal.gsmBitErrorRate;
        this.timeNs = signal.timeAge;

        if ((signalStrength == 0) && (lteRsrp == 0)) {
            this.signalStrength = null;
            this.gsmBitErrorRate = null;
            this.lteRsrp = null;
            this.lteRsrq = null;
            this.lteRssnr = null;
            this.lteCqi = null;
        }

        if (lteRsrp == 0) {
            this.lteRsrp = null;
            this.lteRsrq = null;
            this.lteRssnr = null;
            this.lteCqi = null;
        }

        if (signalStrength == 0) {
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

    public void setNetworkTypeId(Integer networkTypeId) {
        this.networkTypeId = networkTypeId;
    }

    public Integer getLteRsrp() {
        return lteRsrp;
    }

    public void setLteRsrp(Integer lteRsrp) {
        this.lteRsrp = lteRsrp;
    }

    public Integer getLteRsrq() {
        return lteRsrq;
    }

    public void setLteRsrq(Integer lteRsrq) {
        this.lteRsrq = lteRsrq;
    }

    public Integer getLteRssnr() {
        return lteRssnr;
    }

    public void setLteRssnr(Integer lteRssnr) {
        this.lteRssnr = lteRssnr;
    }

    public Integer getLteCqi() {
        return lteCqi;
    }

    public void setLteCqi(Integer lteCqi) {
        this.lteCqi = lteCqi;
    }

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public Integer getGsmBitErrorRate() {
        return gsmBitErrorRate;
    }

    public void setGsmBitErrorRate(Integer gsmBitErrorRate) {
        this.gsmBitErrorRate = gsmBitErrorRate;
    }

    public Long getTimeNs() {
        return timeNs;
    }

    public void setTimeNs(Long timeNs) {
        this.timeNs = timeNs;
    }
}
