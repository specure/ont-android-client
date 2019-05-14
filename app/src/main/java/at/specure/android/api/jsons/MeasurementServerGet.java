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
 * Created by michal.cadrik on 7/31/2017.
 */

public class MeasurementServerGet {


    // API V1
    @SerializedName("location")
    Location location;

    // API V1
    @SerializedName("client")
    String clientName;

    // API V2
    @SerializedName("language")
    String language;

    /**
     * API V1 constructor
     *
     * @param location
     * @param clientName
     */
    public MeasurementServerGet(Location location, String clientName) {
        this.location = location;
        this.clientName = clientName;
    }

    /**
     * API V2 constructor
     *
     * @param location
     * @param clientName
     * @param language
     */
    public MeasurementServerGet(Location location, String clientName, String language) {
        this.location = location;
        this.clientName = clientName;
        this.language = language;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
