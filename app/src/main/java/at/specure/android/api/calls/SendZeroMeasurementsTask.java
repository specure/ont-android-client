/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
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
 ******************************************************************************/
package at.specure.android.api.calls;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.CellLocation;
import at.specure.android.api.jsons.Location;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.api.jsons.Signal;
import at.specure.android.api.jsons.ZeroMeasurement;
import at.specure.android.database.Contract;
import at.specure.android.database.enums.CellLocationType;
import at.specure.android.database.enums.LocationType;
import at.specure.android.database.enums.SignalType;
import at.specure.android.database.enums.ZeroMeasurementState;
import at.specure.android.database.obj.TZeroMeasurement;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndBooleanTaskListener;
import timber.log.Timber;

public class SendZeroMeasurementsTask extends AsyncTask<Void, Void, Boolean> {

    private final MainActivity activity;
    private List<MeasurementServer> servers;
    private ControlServerConnection serverConn;
    private EndBooleanTaskListener endTaskListener;
    private long timestamp = new Date().getTime();

    private static final String DEBUG_TAG = "GetMeasurementServTask";

    public SendZeroMeasurementsTask(final MainActivity activity) {
        this.activity = activity;
        this.timestamp = new Date().getTime();
        this.servers = new ArrayList<>();
    }

    public void setOnCompleteListener(EndBooleanTaskListener listener) {
        this.endTaskListener = listener;
    }

    @Override
    protected Boolean doInBackground(final Void... params) {

        boolean result = false;
        boolean partialResult = true;
        try {
            serverConn = new ControlServerConnection(activity);

            ContentResolver contentResolver = activity.getContentResolver();
            Cursor cursor = contentResolver.query(Contract.ZeroMeasurements.CONTENT_URI, null, Contract.ZeroMeasurementsColumns.STATE + " = ? ",
                    new String[]{String.valueOf(ZeroMeasurementState.NOT_SENT)}, null);
            ArrayList<ZeroMeasurement> zeroMeasurementsToSend;
            if ((cursor != null) && (cursor.moveToFirst())) {
                zeroMeasurementsToSend = new ArrayList<>();
                Timber.e( "ZERO MEASUREMENTS TO SEND: %s", cursor.getCount());
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    TZeroMeasurement zeroMeasurement = new TZeroMeasurement(cursor);
                    // we must load values from database because of lazy loading of lists
                    zeroMeasurement.getCellLocations(activity);
                    zeroMeasurement.getGeoLocations(activity);
                    zeroMeasurement.getSignals(activity);
                    zeroMeasurementsToSend.add(new ZeroMeasurement(zeroMeasurement));
                    if (zeroMeasurementsToSend.size() > 9) {
                        boolean parresult = sendBunchOfZeroMeasurements(contentResolver, zeroMeasurementsToSend);
                        if (!parresult) {
                            partialResult = false;
                        }
                    }
                }
                boolean parresult = sendBunchOfZeroMeasurements(contentResolver, zeroMeasurementsToSend);
                if (!parresult) {
                    partialResult = false;
                }
            }

            if (!partialResult) {
                result = false;
            } else {
                result = true;
            }
            return result;
        } catch (Exception e) {
            Timber.e( "ERROR SENDING ZERO MEASUREMENTS");
            return false;
        }
    }

    private boolean sendBunchOfZeroMeasurements(ContentResolver contentResolver, ArrayList<ZeroMeasurement> zeroMeasurementsToSend) {
        boolean result;
        Gson gson = new Gson();
        Timber.e("ZERO MEASUREMENTS TO SEND 2: %s", zeroMeasurementsToSend.size());
        JsonElement response = serverConn.requestSendZeroMeasurements(zeroMeasurementsToSend);

        result = response.getAsBoolean();

        if (result) {
            for (ZeroMeasurement measurement : zeroMeasurementsToSend) {
                if (measurement != null) {
                    contentResolver.delete(Contract.ZeroMeasurements.CONTENT_URI, Contract.ZeroMeasurementsColumns.ID + " = ? ", new String[]{String.valueOf(measurement.getInternalId())});
                    if (measurement.getCellLocations() != null) {
                        List<CellLocation> cellLocations = measurement.getCellLocations();
                        for (CellLocation cellLocation : cellLocations) {
                            if (cellLocation != null) {
                                contentResolver.delete(Contract.CellLocations.CONTENT_URI, Contract.CellLocationsColumns.TYPE + " = ?  AND " + Contract.CellLocationsColumns.REF_ID + " = ?", new String[]{String.valueOf(CellLocationType.ZERO_MEASUREMENT_CELL_LOCATION), String.valueOf(measurement.getInternalId())});
                            }
                        }
                    }
                    List<Location> geoLocations = measurement.getGeoLocations();
                    if (geoLocations != null) {
                        for (Location location : geoLocations) {
                            if (location != null) {
                                contentResolver.delete(Contract.Locations.CONTENT_URI, Contract.LocationsColumns.TYPE + " = ? AND " + Contract.LocationsColumns.REF_ID, new String[]{String.valueOf(LocationType.ZERO_MEASUREMENT_LOCATION), String.valueOf(measurement.getInternalId())});
                            }
                        }
                    }
                    List<Signal> signals = measurement.getSignals();
                    if (signals != null) {
                        for (Signal signal : signals) {
                            if (signal != null) {
                                contentResolver.delete(Contract.Signals.CONTENT_URI, Contract.SignalsColumns.TYPE + " = ? ",new String[]{String.valueOf(SignalType.ZERO_MEASUREMENT_SIGNAL), String.valueOf(measurement.getInternalId())});
                            }
                        }
                    }
                }
            }
            zeroMeasurementsToSend.clear();
        }
        return result;
    }

    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {


        if (endTaskListener != null) {
            endTaskListener.taskEnded(result);
        }

    }
}
