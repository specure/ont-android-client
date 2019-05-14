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
 * Created by michal.cadrik on 7/27/2017.
 */

public class MeasurementServer {


    // API V1 fields
    @SerializedName("address")
    String address;

    // API V1 fields
    @SerializedName("port")
    Integer port;

    // API V1 fields
    @SerializedName("name")
    String name;

    // API V1 fields
    @SerializedName("id")
    Integer id;

    // API V2 fields
    @SerializedName("sponsor") // this is instead of name of the server
            String sponsor;

    // API V2 fields
    @SerializedName("distance") // as 175 km
            String distance;

    // API V2 fields
    @SerializedName("city")
    String city;

    // API V2 fields
    @SerializedName("country")
    String country;


    /**
     * API V1 constructor
     *
     * @param address
     * @param port
     * @param name
     * @param id
     */
    public MeasurementServer(String address, Integer port, String name, Integer id) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.id = id;
    }

    /**
     * API V2 constructor
     *
     * @param address
     * @param port
     * @param name
     * @param id
     * @param sponsor
     * @param distance
     * @param city
     */
    public MeasurementServer(String address, Integer port, String name, Integer id, String sponsor, String distance, String city, String country) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.id = id;
        this.sponsor = sponsor;
        this.distance = distance;
        this.city = city;
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * to display name use @getDisplayName instead
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * to display name use @getDisplayName instead
     *
     * @return
     */
    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }


    void addToBuilder(StringBuilder builder, String string) {
        if ((string != null) && (!string.isEmpty())) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(string);
        }
    }

    public String getDisplayName(boolean withSponsor) {

        StringBuilder builder = new StringBuilder();
        String country = getCountry();
        String distance = "(" + getDistance() + ")";
        String city = getCity();
        String name = ObjectUtils.firstNonNull(this.name, this.sponsor);

        if (withSponsor) {
            if (country != null) {
                country = country.toUpperCase() + "   ";
                addToBuilder(builder, country + name);
            } else {
                addToBuilder(builder, name);
            }
            addToBuilder(builder, city);
            addToBuilder(builder, distance);
        } else {
            addToBuilder(builder, city);
            if (country != null) {
                country = country.toUpperCase();
                addToBuilder(builder, country);
            }
        }

        String s = builder.toString();
        return s;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
