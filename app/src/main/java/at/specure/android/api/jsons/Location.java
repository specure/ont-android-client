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

import at.specure.android.database.obj.TLocation;

/**
 * Created by michal.cadrik on 7/27/2017.
 */

public class Location {

    @SerializedName("tstamp")
    Long timestamp;

    @SerializedName("time_ns")
    Long time;

    @SerializedName("geo_lat")
    Double latitude;

    @SerializedName("geo_long")
    Double longitude;

    @SerializedName("accuracy")
    Double accuracy;

    @SerializedName("altitude")
    Double altitude;

    @SerializedName("bearing")
    Double bearing;

    @SerializedName("speed")
    Double speed;

    @SerializedName("provider")
    String provider;

    public Location(Long timestamp, Long time, Double latitude, Double longitude, Double accuracy, Double altitude, Double bearing, Double speed, String provider) {
        this.timestamp = timestamp;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.provider = provider;
    }

    public Location(TLocation location) {
        this.timestamp = location.timestamp;
        this.time = location.timeAge;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.accuracy = location.accuracy;
        this.altitude = location.altitude;
        this.bearing = location.bearing;
        this.speed = location.speed;
        this.provider = location.provider;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
