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

import at.specure.android.database.obj.TCellLocation;
import at.specure.android.support.telephony.CellInfoPreV18;
import at.specure.android.support.telephony.CellInfoSupport;

public final class CellLocation {

    @SerializedName("time")
    private Long time;

    @SerializedName("time_ns")
    private Long timeNs;

    @SerializedName("location_id")
    private Long locationId;

    @SerializedName("area_code")
    private Long areaCode;

    @SerializedName("primary_scrambling_code")
    private Long primaryScramblingCode;


    public CellLocation(Long time, Long timeNs, Long locationId, Long areaCode, Long primaryScramblingCode) {
        this.time = time;
        this.timeNs = timeNs;
        this.locationId = locationId;
        this.areaCode = areaCode;
        this.primaryScramblingCode = primaryScramblingCode;
    }

    public CellLocation(TCellLocation location) {
        this.time = location.time;
        this.timeNs = location.timeAge;
        this.locationId = location.locationId;
        this.areaCode = location.areaCode;
        this.primaryScramblingCode = location.primaryScramblingCode;
    }

    public CellLocation(CellInfoPreV18 cellInfoPreV18) {
        this.time = System.currentTimeMillis(); //add for backward compatibility
        this.timeNs = System.nanoTime(); //this is - test start time when it is sent to server
        this.locationId = (long) cellInfoPreV18.getCellId();
        this.areaCode = (long) cellInfoPreV18.getAreaCode();
        this.primaryScramblingCode = (long) cellInfoPreV18.getPrimaryScramblingCode();
    }

    public CellLocation(CellInfoSupport cellInfo) {
        this.time = System.currentTimeMillis(); //add for backward compatibility
        this.timeNs = System.nanoTime(); //this is - test start time when it is sent to server
        this.locationId = (long) cellInfo.getCellId();
        this.areaCode = (long) cellInfo.getAreaCode();
        this.primaryScramblingCode = (long) cellInfo.getPrimaryScramblingCode();
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTimeNs() {
        return timeNs;
    }

    public void setTimeNs(Long timeNs) {
        this.timeNs = timeNs;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Long areaCode) {
        this.areaCode = areaCode;
    }

    public Long getPrimaryScramblingCode() {
        return primaryScramblingCode;
    }

    public void setPrimaryScramblingCode(Long primaryScramblingCode) {
        this.primaryScramblingCode = primaryScramblingCode;
    }
}
