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

/**
 * Created by michal.cadrik on 8/9/2017.
 * Nominatim reverse geocoding response
 *
 *
 * @link https://github.com/OpenCageData/address-formatting/blob/master/conf/components.yaml
 *
 * @link https://help.openstreetmap.org/questions/55051/reverse-geocoding-json-format-difference/55053
 */



public class LocalizedGeopositionGet {

    final String quotes = "^\"|\"$";

    @SerializedName("place_id")
    private String placeId;

    @SerializedName("licence")
    private String license;

    @SerializedName("osm_type")
    private String osmType;

    @SerializedName("osm_id")
    private String osmId;

    @SerializedName("lat")
    private String latitude;

    @SerializedName("lon")
    private String longtitude;

    @SerializedName("display_name")
    private String longDisplayName;

    @SerializedName("address")
    private Address address;

    @SerializedName("boundingbox")
    private String[] boundingbox;


    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLicense() {
        return license.replaceAll(quotes, "");
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getOsmType() {
        return osmType;
    }

    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    public String getOsmId() {
        return osmId;
    }

    public void setOsmId(String osmId) {
        this.osmId = osmId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getLongDisplayName() {
        return longDisplayName.replaceAll(quotes, "");
    }

    public void setLongDisplayName(String longDisplayName) {
        this.longDisplayName = longDisplayName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String[] getBoundingbox() {
        return boundingbox;
    }

    public void setBoundingbox(String[] boundingbox) {
        this.boundingbox = boundingbox;
    }
}
