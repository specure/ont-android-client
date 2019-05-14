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

package at.specure.android.database.obj;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import at.specure.android.database.Contract;
import at.specure.android.database.DatabaseHelper;
import at.specure.android.database.enums.CellLocationType;
import at.specure.android.database.enums.LocationType;
import at.specure.android.database.enums.SignalType;

/**
 * Created by michal.cadrik on 8/10/2017.
 */

public class TZeroMeasurement {

    public Long id;
    public String clientUuid;
    public String clientName;
    public String clientVersion;
    public String clientLanguage;
    public Long time;
    public String uuid;
    public String platform;
    public String product;
    public String apiLevel;
    public String telephonyNetworkOperator;
    public String clientSoftwareVersion;
    public String telephonyNetworkIsRoaming;
    public String osVersion;
    public String telephonyNetworkCountry;
    public String networkType;
    public String telephonyNetworkOperatorName;
    public String telephonyNetworkSimOperatorName;
    public String model;
    public String telephonyNetworkSimOperator;
    public String device;
    public String telephonyPhoneType;
    public String telephonyDataState;
    public String telephonyNetworkSimCountry;
    private List<TCellLocation> cellLocations;
    private List<TLocation> geoLocations;
    private List<TSignal> signals;
    public Integer state;


    public TZeroMeasurement(Long id, String clientUuid, String clientName, String clientVersion, String clientLanguage, Long time, String uuid, String platform, String product, String apiLevel,
                            String telephonyNetworkOperator, String clientSoftwareVersion, String telephonyNetworkIsRoaming, String osVersion,
                            String telephonyNetworkCountry, String networkType, String telephonyNetworkOperatorName, String telephonyNetworkSimOperatorName,
                            String model, String telephonyNetworkSimOperator, String device, String telephonyPhoneType, String telephonyDataState, String telephonyNetworkSimCountry,
                            List<TCellLocation> cellLocations, List<TLocation> geoLocations, List<TSignal> signals, Integer state) {
        this.id = id;
        this.clientUuid = clientUuid;
        this.clientName = clientName;
        this.clientVersion = clientVersion;
        this.clientLanguage = clientLanguage;
        this.time = time;
        this.uuid = uuid;
        this.platform = platform;
        this.product = product;
        this.apiLevel = apiLevel;
        this.telephonyNetworkOperator = telephonyNetworkOperator;
        this.clientSoftwareVersion = clientSoftwareVersion;
        this.telephonyNetworkIsRoaming = telephonyNetworkIsRoaming;
        this.osVersion = osVersion;
        this.telephonyNetworkCountry = telephonyNetworkCountry;
        this.networkType = networkType;
        this.telephonyNetworkOperatorName = telephonyNetworkOperatorName;
        this.telephonyNetworkSimOperatorName = telephonyNetworkSimOperatorName;
        this.model = model;
        this.telephonyNetworkSimOperator = telephonyNetworkSimOperator;
        this.device = device;
        this.telephonyPhoneType = telephonyPhoneType;
        this.telephonyDataState = telephonyDataState;
        this.telephonyNetworkSimCountry = telephonyNetworkSimCountry;
        this.cellLocations = cellLocations;
        this.geoLocations = geoLocations;
        this.signals = signals;
        this.state = state;
    }

    public TZeroMeasurement(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        id = DatabaseHelper.getLongCursor(cursor, Contract.ZeroMeasurementsColumns.ID);
        clientUuid = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.CLIENT_UUID);
        clientName = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.CLIENT_NAME);
        clientVersion = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.CLIENT_VERSION);
        clientLanguage = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.CLIENT_LANG);
        time = DatabaseHelper.getLongCursor(cursor, Contract.ZeroMeasurementsColumns.TIME);
        uuid = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.UUID);
        platform = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.PLATFORM);
        product = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TIME);
        apiLevel = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.API_LEVEL);
        telephonyNetworkOperator = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR);
        clientSoftwareVersion = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.CLIENT_SOFT_VERSION);
        telephonyNetworkIsRoaming = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_IS_ROAMING);
        osVersion = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.OS_VERSION);
        telephonyNetworkCountry = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_COUNTRY);
        networkType = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.NETWORK_TYPE);
        telephonyNetworkOperatorName = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR_NAME);
        telephonyNetworkSimOperatorName = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR_NAME);
        model = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.MODEL);
        telephonyNetworkSimOperator = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR);
        device = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.DEVICE);
        telephonyPhoneType = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_PHONE_TYPE);
        telephonyDataState = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_DATA_STATE);
        telephonyNetworkSimCountry = DatabaseHelper.getStringCursor(cursor, Contract.ZeroMeasurementsColumns.TEL_NET_SIM_COUNTRY);
        state = DatabaseHelper.getIntCursor(cursor, Contract.ZeroMeasurementsColumns.STATE);
    }

    //TODO: Dorob vyberanie z databazy ak nie su nacitane - lazy loading


    public List<TCellLocation> getCellLocations(Context context) {
        if (context != null) {
            if ((cellLocations == null) || (cellLocations.isEmpty())) {
                ContentResolver contentResolver = context.getContentResolver();
                if (id != null) {
                    Cursor query = contentResolver.query(Contract.CellLocations.buildCellLocationForStg(id), null, null, new String[]{String.valueOf(CellLocationType.ZERO_MEASUREMENT_CELL_LOCATION)}, null);
                    if ((query != null) && (query.moveToFirst())) {
                        ArrayList<TCellLocation> tCellLocations = new ArrayList<>();
                        for (int i = 0; i < query.getCount(); i++) {
                            query.moveToPosition(i);
                            tCellLocations.add(new TCellLocation(query));
                        }
                        cellLocations = tCellLocations;
                    }
                    if (query != null) {
                        query.close();
                    }
                }
            }
        }
        return cellLocations;
    }

    public void setCellLocations(List<TCellLocation> cellLocations) {
        this.cellLocations = cellLocations;
    }

    public List<TLocation> getGeoLocations(Context context) {
        if (context != null) {
            if ((geoLocations == null) || (geoLocations.isEmpty())) {
                ContentResolver contentResolver = context.getContentResolver();
                if (id != null) {
                    Cursor query = contentResolver.query(Contract.Locations.buildLocationsForStg(id), null, null, new String[]{String.valueOf(LocationType.ZERO_MEASUREMENT_LOCATION)}, null);
                    if ((query != null) && (query.moveToFirst())) {
                        ArrayList<TLocation> tLocations = new ArrayList<>();
                        for (int i = 0; i < query.getCount(); i++) {
                            query.moveToPosition(i);
                            tLocations.add(new TLocation(query));
                        }
                        geoLocations = tLocations;
                    }
                    if (query != null) {
                        query.close();
                    }
                }
            }
        }
        return geoLocations;
    }

    public void setGeoLocations(List<TLocation> geoLocations) {
        this.geoLocations = geoLocations;
    }

    public List<TSignal> getSignals(Context context) {
        if (context != null) {
            if ((signals == null) || (signals.isEmpty())) {
                ContentResolver contentResolver = context.getContentResolver();
                if (id != null) {
                    Cursor query = contentResolver.query(Contract.Signals.buildSignalsForStg(id), null, null, new String[]{String.valueOf(SignalType.ZERO_MEASUREMENT_SIGNAL)}, null);
                    if ((query != null) && (query.moveToFirst())) {
                        ArrayList<TSignal> tSignals = new ArrayList<>();
                        for (int i = 0; i < query.getCount(); i++) {
                            query.moveToPosition(i);
                            tSignals.add(new TSignal(query));
                        }
                        signals = tSignals;
                    }
                    if (query != null) {
                        query.close();
                    }
                }
            }
        }
        return signals;
    }

    public void setSignals(List<TSignal> signals) {
        this.signals = signals;
    }

    public void save(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(Contract.ZeroMeasurementsColumns.API_LEVEL, this.apiLevel);
        cv.put(Contract.ZeroMeasurementsColumns.CLIENT_LANG, this.clientLanguage);
        cv.put(Contract.ZeroMeasurementsColumns.CLIENT_NAME, this.clientName);
        cv.put(Contract.ZeroMeasurementsColumns.CLIENT_SOFT_VERSION, this.clientSoftwareVersion);
        cv.put(Contract.ZeroMeasurementsColumns.CLIENT_UUID, this.clientUuid);
        cv.put(Contract.ZeroMeasurementsColumns.UUID, this.uuid);
        cv.put(Contract.ZeroMeasurementsColumns.DEVICE, this.device);
        cv.put(Contract.ZeroMeasurementsColumns.MODEL, this.model);
        cv.put(Contract.ZeroMeasurementsColumns.CLIENT_VERSION, this.clientVersion);
        cv.put(Contract.ZeroMeasurementsColumns.TIME, this.time);
        cv.put(Contract.ZeroMeasurementsColumns.PLATFORM, this.platform);
        cv.put(Contract.ZeroMeasurementsColumns.PRODUCT, this.product);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR, this.telephonyNetworkOperator);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_IS_ROAMING, this.telephonyNetworkIsRoaming);
        cv.put(Contract.ZeroMeasurementsColumns.OS_VERSION, this.osVersion);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_COUNTRY, this.telephonyNetworkCountry);
        cv.put(Contract.ZeroMeasurementsColumns.NETWORK_TYPE, this.networkType);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR_NAME, this.telephonyNetworkOperatorName);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR_NAME, this.telephonyNetworkSimOperatorName);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR, this.telephonyNetworkSimOperator);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_PHONE_TYPE, this.telephonyPhoneType);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_DATA_STATE, this.telephonyDataState);
        cv.put(Contract.ZeroMeasurementsColumns.TEL_NET_SIM_COUNTRY, this.telephonyNetworkSimCountry);
        cv.put(Contract.ZeroMeasurementsColumns.STATE, this.state);

        Uri insert = contentResolver.insert(Contract.ZeroMeasurements.CONTENT_URI, cv);
        Long newId = ContentUris.parseId(insert);

        List<TCellLocation> cellLocations = getCellLocations(context);
        for (TCellLocation cellLocation : cellLocations) {
            cellLocation.refId = newId;
            cellLocation.save(context);
        }

        List<TLocation> locations = getGeoLocations(context);
        for (TLocation location : locations) {
            location.refId = newId;
            location.save(context);
        }

        List<TSignal> signals = getSignals(context);
        for (TSignal signal : signals) {
            signal.refId = newId;
            signal.save(context);
        }

    }

}
