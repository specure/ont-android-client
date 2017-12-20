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

import org.apache.commons.lang3.ObjectUtils;

/**
 * Parsing class for reverse geocoding for nominatim response
 * Created by michal.cadrik on 8/9/2017.
 */

@SuppressWarnings("ALL")
public class Address {

    final String quotes = "^\"|\"$";

    @SerializedName("house_number")
    private String houseNumber;

    @SerializedName("street_number")
    private String streetNumber;

    // HOUSE ALIASES
    @SerializedName("house")
    private String house;

    @SerializedName("building")
    private String building;

    @SerializedName("public_building")
    private String publicBuilding;
    //

    // ROAD ALIASES
    @SerializedName("road")
    private String road;

    @SerializedName("footway")
    private String footway;

    @SerializedName("street")
    private String street;

    @SerializedName("street_name")
    private String streetName;

    @SerializedName("residential")
    private String residential;

    @SerializedName("path")
    private String path;

    @SerializedName("pedestrian")
    private String pedestrian;

    @SerializedName("road_reference")
    private String roadReference;

    @SerializedName("road_reference_intl")
    private String roadReferenceIntl;
    //

    // VILLAGE ALIASES
    @SerializedName("village")
    private String village;

    @SerializedName("hamlet")
    private String hamlet;

    @SerializedName("locality")
    private String locality;
    //

    // NEIGHBOURHOOD ALIASES
    @SerializedName("neighbourhood")
    private String neighbourhood;

    @SerializedName("suburb")
    private String suburb;

    @SerializedName("city_district")
    private String cityDistrict;
    //


    // CITY ALIASES
    @SerializedName("city")
    private String city;

    @SerializedName("town")
    private String town;
    //

    @SerializedName("county")
    private String county;

    @SerializedName("postcode")
    private String postcode;

    @SerializedName("state_district")
    private String stateDistrict;

    // STATE ALIASES
    @SerializedName("state")
    private String state;

    @SerializedName("province")
    private String province;

    @SerializedName("state_code")
    private String stateCode;
    //

    @SerializedName("region")
    private String region;

    @SerializedName("island")
    private String island;

    // COUNTRY ALIASES
    @SerializedName("country")
    private String country;

    @SerializedName("country_name")
    private String countryName;

    @SerializedName("country_code")
    private String countryCode;

    @SerializedName("continent")
    private String continent;

    public Address(String houseNumber, String road, String suburb, String village, String cityDistrict, String city, String state, String postcode, String country, String countryCode) {
        this.houseNumber = houseNumber;
        this.road = road;
        this.suburb = suburb;
        this.village = village;
        this.cityDistrict = cityDistrict;
        this.city = city;
        this.state = state;
        this.postcode = postcode;
        this.country = country;
        this.countryCode = countryCode;
    }

   public String getCityAlias() {
       return ObjectUtils.firstNonNull(city, town);
   }

   public String getCityDistrictAlias() {
        return ObjectUtils.firstNonNull(suburb, cityDistrict, neighbourhood);
   }

    public String getVillageAlias() {
        return ObjectUtils.firstNonNull(village, hamlet, locality);
    }


}
