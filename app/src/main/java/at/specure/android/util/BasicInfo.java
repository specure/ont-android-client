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

package at.specure.android.util;

import at.specure.android.configs.Config;
import at.specure.android.database.enums.LocationType;
import at.specure.android.database.obj.TLocation;

/**
 * Created by michal.cadrik on 8/14/2017.
 */

public class BasicInfo {

    public String platform;
    public String osVersion;
    public int apiLevel;
    public String device;
    public String model;
    public String product;
    public String language;
    public String timezone;
    public String softwareRevision;
    public String type = Config.RMBT_CLIENT_TYPE;

    //optional
    public int codeVersion;
    public String softwareVersionName;
    public at.specure.android.api.jsons.Location location;



    public BasicInfo(String platform, String osVersion, int apiLevel, String device, String model, String product, String language, String timezone, String softwareRevision) {
        this.platform = platform;
        this.osVersion = osVersion;
        this.apiLevel = apiLevel;
        this.device = device;
        this.model = model;
        this.product = product;
        this.language = language;
        this.timezone = timezone;
        this.softwareRevision = softwareRevision;
    }

    public int getCodeVersion() {
        return codeVersion;
    }

    public void setCodeVersion(int codeVersion) {
        this.codeVersion = codeVersion;
    }

    public String getSoftwareVersionName() {
        return softwareVersionName;
    }

    public void setSoftwareVersionName(String softwareVersionName) {
        this.softwareVersionName = softwareVersionName;
    }

    public at.specure.android.api.jsons.Location getLocation() {
        return location;
    }

    public TLocation getTLocation() {
        return new TLocation(location, LocationType.UNKNOWN);
    }

    public void setLocation(at.specure.android.api.jsons.Location location) {
        this.location = location;
    }



}
