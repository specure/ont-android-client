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

package at.specure.android.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.specure.opennettest.BuildConfig;

import timber.log.Timber;


/**
 * Created by michal.cadrik on 8/9/2017.
 */

public class Database extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public  static final String DATABASE_NAME = BuildConfig.databaseName;


    public static final String LOG_TAG = "Database";

    public static final String CREATE_TABLE_ZERO_MEASUREMENTS =
            " CREATE TABLE IF NOT EXISTS " + Tables.ZERO_MEASUREMENTS + " ( " +
                    Contract.ZeroMeasurementsColumns.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Contract.ZeroMeasurementsColumns.CLIENT_NAME + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.CLIENT_UUID + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.CLIENT_VERSION + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.CLIENT_LANG + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.CLIENT_SOFT_VERSION + " TEXT, " +

                    Contract.ZeroMeasurementsColumns.TIME + " INTEGER, " +
                    Contract.ZeroMeasurementsColumns.UUID + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.PLATFORM + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.PRODUCT + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.API_LEVEL + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.OS_VERSION + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.NETWORK_TYPE + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.MODEL + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.DEVICE + " TEXT, " +

                    Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_IS_ROAMING + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_COUNTRY + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_OPERATOR_NAME + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR_NAME + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_SIM_OPERATOR + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_PHONE_TYPE + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_DATA_STATE + " TEXT, " +
                    Contract.ZeroMeasurementsColumns.TEL_NET_SIM_COUNTRY + " TEXT, " +

                    Contract.ZeroMeasurementsColumns.STATE + " INTEGER, " +
                    Contract.ZeroMeasurementsColumns.TIMEZONE + " TEXT " +
                    " ) ";

    public static final String CREATE_TABLE_SIGNALS = " CREATE TABLE IF NOT EXISTS " + Tables.SIGNALS + " ( " +
                                            Contract.SignalsColumns.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                            Contract.SignalsColumns.TIME + " INTEGER, " +
                                            Contract.SignalsColumns.TIME_AGE + " INTEGER, " +
                                            Contract.SignalsColumns.GSM_BIT_ERROR_RATE + " INTEGER, " +
                                            Contract.SignalsColumns.NETWORK_TYPE_ID + " INTEGER, " +
                                            Contract.SignalsColumns.SIGNAL_STRENGTH + " INTEGER, " +
                                            Contract.SignalsColumns.LTE_CQI + " INTEGER, " +
                                            Contract.SignalsColumns.LTE_RSRP + " INTEGER, " +
                                            Contract.SignalsColumns.LTE_RSRQ + " INTEGER, " +
                                            Contract.SignalsColumns.LTE_RSSNR + " INTEGER, " +
                                            Contract.SignalsColumns.REF_ID + " INTEGER, " +
                                            Contract.SignalsColumns.TYPE + " INTEGER " +
                                        " ) ";

    public static final String CREATE_TABLE_LOCATIONS = " CREATE TABLE IF NOT EXISTS " + Tables.LOCATIONS + " ( " +
                                            Contract.LocationsColumns.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                            Contract.LocationsColumns.TIME + " INTEGER, " +
                                            Contract.LocationsColumns.TIME_AGE + " INTEGER, " +
                                            Contract.LocationsColumns.ACCURACY + " REAL, " +
                                            Contract.LocationsColumns.ALTITUDE + " REAL, " +
                                            Contract.LocationsColumns.BEARING + " REAL, " +
                                            Contract.LocationsColumns.LATITUDE + " REAL, " +
                                            Contract.LocationsColumns.LONGITUDE + " REAL, " +
                                            Contract.LocationsColumns.PROVIDER + " TEXT, " +
                                            Contract.LocationsColumns.SPEED + " INTEGER, " +
                                            Contract.LocationsColumns.REF_ID + " INTEGER, " +
                                            Contract.LocationsColumns.TYPE + " INTEGER " +
                                            " ) ";

    public static final String CREATE_TABLE_CELL_LOCATIONS = " CREATE TABLE IF NOT EXISTS " + Tables.CELL_LOCATIONS + " ( " +
            Contract.CellLocationsColumns.ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Contract.CellLocationsColumns.TIME + " INTEGER, " +
            Contract.CellLocationsColumns.TIME_AGE + " INTEGER, " +
            Contract.CellLocationsColumns.LOCATION_ID + " INTEGER, " +
            Contract.CellLocationsColumns.AREA_CODE + " INTEGER, " +
            Contract.CellLocationsColumns.PRIMARY_SCRAMBLING_CODE + " INTEGER, " +
            Contract.CellLocationsColumns.REF_ID + " INTEGER, " +
            Contract.CellLocationsColumns.TYPE + " INTEGER " +
            " ) ";

    public Database(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    public Database(Context context, String databaseName, DatabaseErrorHandler errorHandler) {
        super(context, databaseName, null, DATABASE_VERSION, errorHandler);
    }


    public static String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    private static void execSql(SQLiteDatabase db, String command) {
        Timber.d(command);
        db.execSQL(command);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d( "Creating database");
        execSql(db, CREATE_TABLE_ZERO_MEASUREMENTS);
        execSql(db, CREATE_TABLE_CELL_LOCATIONS);
        execSql(db, CREATE_TABLE_SIGNALS);
        execSql(db, CREATE_TABLE_LOCATIONS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w(Database.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        switch (oldVersion) {
            default:
                execSql(db, dropTable(Tables.ZERO_MEASUREMENTS));
                execSql(db, dropTable(Tables.SIGNALS));
                execSql(db, dropTable(Tables.LOCATIONS));
                execSql(db, dropTable(Tables.CELL_LOCATIONS));
        }
        onCreate(db);
    }

    public interface Tables {
        String ZERO_MEASUREMENTS = "zero_measurements";
        String SIGNALS = "signals";
        String LOCATIONS = "locations";
        String CELL_LOCATIONS = "cell_locations";
    }

    public interface References {
      /*  String USER_ID = "REFERENCES " + Tables.USERS + "(" + Contract.UserColumns.ID + ") ON DELETE CASCADE"; */
    }




}
