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

import java.util.List;

import at.specure.android.database.mappers.CellLocationMapper;
import at.specure.android.database.mappers.GeolocationMapper;
import at.specure.android.database.mappers.SignalMapper;
import at.specure.android.database.obj.TZeroMeasurement;

/**
 * Created by michal.cadrik on 8/9/2017.
 */

@SuppressWarnings("JavadocReference")
public class ZeroMeasurement {

    private transient Long internalId;

    @SerializedName("client_uuid")
    private String clientUuid;

    @SerializedName("client_name")
    private String clientName;

    @SerializedName("client_version")
    private String clientVersion;

    @SerializedName("client_language")
    private String clientLanguage;

    @SerializedName("time")
    private Long time;
    //private String testToken;//will be generated on server side

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("plattform")
    private String platform;

    @SerializedName("product")
    private String product;

    @SerializedName("api_level")
    private String apiLevel;

    @SerializedName("telephony_network_operator")
    private String telephonyNetworkOperator;

    @SerializedName("client_software_version")
    private String clientSoftwareVersion;

    @SerializedName("telephony_network_is_roaming")
    private String telephonyNetworkIsRoaming;

    @SerializedName("os_version")
    private String osVersion;

    @SerializedName("telephony_network_country")
    private String telephonyNetworkCountry;

    @SerializedName("network_type")
    private String networkType;

    @SerializedName("telephony_network_operator_name")
    private String telephonyNetworkOperatorName;

    @SerializedName("telephony_network_sim_operator_name")
    private String telephonyNetworkSimOperatorName;

    @SerializedName("model")
    private String model;

    @SerializedName("telephony_network_sim_operator")
    private String telephonyNetworkSimOperator;

    @SerializedName("device")
    private String device;

    @SerializedName("telephony_phone_type")
    private String telephonyPhoneType;

    @SerializedName("telephony_data_state")
    private String telephonyDataState;

    @SerializedName("telephony_network_sim_country")
    private String telephonyNetworkSimCountry;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("geoLocations")
    private List<Location> geoLocations;

    @SerializedName("cellLocations")
    private List<CellLocation> cellLocations;

    @SerializedName("signals")
    private List<Signal> signals;

    public ZeroMeasurement(Long internalId, String clientUuid, String clientName, String clientVersion, String clientLanguage, Long time, String uuid, String plattform, String product, String apiLevel, String telephonyNetworkOperator, String clientSoftwareVersion, String telephonyNetworkIsRoaming, String osVersion, String telephonyNetworkCountry, String networkType, String telephonyNetworkOperatorName, String telephonyNetworkSimOperatorName, String model, String telephonyNetworkSimOperator, String device, String telephonyPhoneType, String telephonyDataState, String telephonyNetworkSimCountry, String timezone, List<Location> geoLocations, List<CellLocation> cellLocations, List<Signal> signals) {

        this.internalId = internalId;
        this.clientUuid = clientUuid;
        this.clientName = clientName;
        this.clientVersion = clientVersion;
        this.clientLanguage = clientLanguage;
        this.clientSoftwareVersion = clientSoftwareVersion;

        this.time = time;
        this.uuid = uuid;
        this.platform = plattform;
        this.product = product;
        this.apiLevel = apiLevel;
        this.osVersion = osVersion;
        this.networkType = networkType;
        this.model = model;
        this.device = device;

        this.telephonyNetworkOperator = telephonyNetworkOperator;
        this.telephonyNetworkIsRoaming = telephonyNetworkIsRoaming;
        this.telephonyNetworkCountry = telephonyNetworkCountry;
        this.telephonyNetworkOperatorName = telephonyNetworkOperatorName;
        this.telephonyNetworkSimOperatorName = telephonyNetworkSimOperatorName;
        this.telephonyNetworkSimOperator = telephonyNetworkSimOperator;
        this.telephonyPhoneType = telephonyPhoneType;
        this.telephonyDataState = telephonyDataState;
        this.telephonyNetworkSimCountry = telephonyNetworkSimCountry;

        this.geoLocations = geoLocations;
        this.cellLocations = cellLocations;
        this.signals = signals;

        this.timezone = timezone;
    }

    /**
     *
     * Make sure the lists: {@link TZeroMeasurement.signals}, {@link TZeroMeasurement.cellLocations}, {@link TZeroMeasurement.geoLocations} are loaded from database when using this constructor because there is no access to context
     * @param zeroMeasurement
     */
    public ZeroMeasurement(TZeroMeasurement zeroMeasurement) {

        this.internalId = zeroMeasurement.id;
        this.clientUuid = zeroMeasurement.clientUuid;
        this.clientName = zeroMeasurement.clientName;
        this.clientVersion = zeroMeasurement.clientVersion;
        this.clientLanguage = zeroMeasurement.clientLanguage;
        this.clientSoftwareVersion = zeroMeasurement.clientSoftwareVersion;

        this.time = zeroMeasurement.time;
        this.uuid = zeroMeasurement.uuid;
        this.platform = zeroMeasurement.platform;
        this.product = zeroMeasurement.product;
        this.apiLevel = zeroMeasurement.apiLevel;
        this.osVersion = zeroMeasurement.osVersion;
        this.networkType = zeroMeasurement.networkType;
        this.model = zeroMeasurement.model;
        this.device = zeroMeasurement.device;

        this.telephonyNetworkOperator = zeroMeasurement.telephonyNetworkOperator;
        this.telephonyNetworkIsRoaming = zeroMeasurement.telephonyNetworkIsRoaming;
        this.telephonyNetworkCountry = zeroMeasurement.telephonyNetworkCountry;
        this.telephonyNetworkOperatorName = zeroMeasurement.telephonyNetworkOperatorName;
        this.telephonyNetworkSimOperatorName = zeroMeasurement.telephonyNetworkSimOperatorName;
        this.telephonyNetworkSimOperator = zeroMeasurement.telephonyNetworkSimOperator;
        this.telephonyPhoneType = zeroMeasurement.telephonyPhoneType;
        this.telephonyDataState = zeroMeasurement.telephonyDataState;
        this.telephonyNetworkSimCountry = zeroMeasurement.telephonyNetworkSimCountry;

        this.geoLocations = new GeolocationMapper().map(zeroMeasurement.getGeoLocations(null));
        this.cellLocations = new CellLocationMapper().map(zeroMeasurement.getCellLocations(null));
        this.signals = new SignalMapper().map(zeroMeasurement.getSignals(null));

        this.timezone = zeroMeasurement.timezone;
    }

    public Long getInternalId() {
        return internalId;
    }

    public String getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getClientLanguage() {
        return clientLanguage;
    }

    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getTelephonyNetworkOperator() {
        return telephonyNetworkOperator;
    }

    public void setTelephonyNetworkOperator(String telephonyNetworkOperator) {
        this.telephonyNetworkOperator = telephonyNetworkOperator;
    }

    public String getClientSoftwareVersion() {
        return clientSoftwareVersion;
    }

    public void setClientSoftwareVersion(String clientSoftwareVersion) {
        this.clientSoftwareVersion = clientSoftwareVersion;
    }

    public String getTelephonyNetworkIsRoaming() {
        return telephonyNetworkIsRoaming;
    }

    public void setTelephonyNetworkIsRoaming(String telephonyNetworkIsRoaming) {
        this.telephonyNetworkIsRoaming = telephonyNetworkIsRoaming;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getTelephonyNetworkCountry() {
        return telephonyNetworkCountry;
    }

    public void setTelephonyNetworkCountry(String telephonyNetworkCountry) {
        this.telephonyNetworkCountry = telephonyNetworkCountry;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getTelephonyNetworkOperatorName() {
        return telephonyNetworkOperatorName;
    }

    public void setTelephonyNetworkOperatorName(String telephonyNetworkOperatorName) {
        this.telephonyNetworkOperatorName = telephonyNetworkOperatorName;
    }

    public String getTelephonyNetworkSimOperatorName() {
        return telephonyNetworkSimOperatorName;
    }

    public void setTelephonyNetworkSimOperatorName(String telephonyNetworkSimOperatorName) {
        this.telephonyNetworkSimOperatorName = telephonyNetworkSimOperatorName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTelephonyNetworkSimOperator() {
        return telephonyNetworkSimOperator;
    }

    public void setTelephonyNetworkSimOperator(String telephonyNetworkSimOperator) {
        this.telephonyNetworkSimOperator = telephonyNetworkSimOperator;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getTelephonyPhoneType() {
        return telephonyPhoneType;
    }

    public void setTelephonyPhoneType(String telephonyPhoneType) {
        this.telephonyPhoneType = telephonyPhoneType;
    }

    public String getTelephonyDataState() {
        return telephonyDataState;
    }

    public void setTelephonyDataState(String telephonyDataState) {
        this.telephonyDataState = telephonyDataState;
    }

    public String getTelephonyNetworkSimCountry() {
        return telephonyNetworkSimCountry;
    }

    public void setTelephonyNetworkSimCountry(String telephonyNetworkSimCountry) {
        this.telephonyNetworkSimCountry = telephonyNetworkSimCountry;
    }

    public List<Location> getGeoLocations() {
        return geoLocations;
    }

    public void setGeoLocations(List<Location> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public List<CellLocation> getCellLocations() {
        return cellLocations;
    }

    public void setCellLocations(List<CellLocation> cellLocations) {
        this.cellLocations = cellLocations;
    }

    public List<Signal> getSignals() {
        return signals;
    }

    public void setSignals(List<Signal> signals) {
        this.signals = signals;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
