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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for Opennettest database tasks.
 */
public class DatabaseProvider extends ContentProvider {

    public static final String TAG = DatabaseProvider.class.getName();

    // All codes for getting/editing users are in interval <100,200)
    public static final int ZERO_MEASUREMENT = 100;                 // all zero measurements
    //    public static final int ZERO_MEASUREMENT_BY_ID = 101;     // zero measurement by id
    // All codes for getting/editing signals are in interval <200,300)
    public static final int SIGNAL = 200;                           // all signals
    public static final int SIGNAL_BY_REFERENCE = 201;              // all signals by reference id
    // All codes for getting/editing cell locations are in interval <300,400)
    public static final int CELL_LOCATION = 300;                    // all cell locations
    public static final int CELL_LOCATION_BY_REFERENCE = 301;       // all cell locations by reference id
    // All codes for getting/editing locations are in interval <400,500)
    public static final int LOCATION = 400;                         // all locations
    public static final int LOCATION_BY_REFERENCE = 401;            // all locations by reference id


    private static final UriMatcher uriMatcher = buildUriMatcher();
    private Database databaseHelper;


    /**
     * It can be user also ? and # as wildcard
     * # for number
     **/
    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "zero_measurements", ZERO_MEASUREMENT);

        uriMatcher.addURI(authority, "signals", SIGNAL);
        uriMatcher.addURI(authority, "signals/by_reference/*", SIGNAL_BY_REFERENCE);

        uriMatcher.addURI(authority, "cell_locations", CELL_LOCATION);
        uriMatcher.addURI(authority, "cell_locations/by_reference/*", CELL_LOCATION_BY_REFERENCE);

        uriMatcher.addURI(authority, "locations", LOCATION);
        uriMatcher.addURI(authority, "locations/by_reference/*", LOCATION_BY_REFERENCE);

//        uriMatcher.addURI(authority, "zero_measurements/*", ZERO_MEASUREMENT_BY_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        databaseHelper = new Database(context, Database.DATABASE_NAME);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "querying " + uri.toString());
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String groupBy = null;

        switch (uriMatcher.match(uri)) {
            case ZERO_MEASUREMENT:
                qb.setTables(Database.Tables.ZERO_MEASUREMENTS);
                sortOrder = sortOrder == null ? Contract.ZeroMeasurements.DEFAULT_SORT : sortOrder;
                break;
//            case ZERO_MEASUREMENT_BY_ID:
//                qb.setTables(Database.Tables.ZERO_MEASUREMENTS);
//                selection = Contract.Users.USER_UUID + " = ? ";
//                selectionArgs = new String[]{Contract.Users.getUserId(uri)};
//                break;
            case SIGNAL:
                qb.setTables(Database.Tables.SIGNALS);
                sortOrder = sortOrder == null ? Contract.Signals.DEFAULT_SORT : sortOrder;
                break;
            case SIGNAL_BY_REFERENCE:
                qb.setTables(Database.Tables.SIGNALS);
                selection = Contract.SignalsColumns.REF_ID + " = ? AND " + Contract.SignalsColumns.TYPE + " = ? ";
                selectionArgs = new String[]{Contract.Signals.getReferenceId(uri), selectionArgs[0]};
                sortOrder = sortOrder == null ? Contract.Signals.DEFAULT_SORT : sortOrder;
                break;
            case CELL_LOCATION:
                qb.setTables(Database.Tables.CELL_LOCATIONS);
                sortOrder = sortOrder == null ? Contract.CellLocations.DEFAULT_SORT : sortOrder;
                break;
            case CELL_LOCATION_BY_REFERENCE:
                qb.setTables(Database.Tables.CELL_LOCATIONS);
                selection = Contract.CellLocationsColumns.REF_ID + " = ? AND " + Contract.CellLocationsColumns.TYPE + " = ? ";
                selectionArgs = new String[]{Contract.CellLocations.getReferenceId(uri), selectionArgs[0]};
                sortOrder = sortOrder == null ? Contract.CellLocations.DEFAULT_SORT : sortOrder;
                break;
            case LOCATION:
                qb.setTables(Database.Tables.LOCATIONS);
                sortOrder = sortOrder == null ? Contract.Locations.DEFAULT_SORT : sortOrder;
                break;
            case LOCATION_BY_REFERENCE:
                qb.setTables(Database.Tables.LOCATIONS);
                selection = Contract.LocationsColumns.REF_ID + " = ? AND " + Contract.LocationsColumns.TYPE + " = ? ";
                selectionArgs = new String[]{Contract.Locations.getReferenceId(uri), selectionArgs[0]};
                sortOrder = sortOrder == null ? Contract.Locations.DEFAULT_SORT : sortOrder;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        @SuppressWarnings("ConstantConditions") Cursor cursor = qb.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder);

        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ZERO_MEASUREMENT:
                return Contract.ZeroMeasurements.CONTENT_TYPE;
//            case ZERO_MEASUREMENT_BY_ID:
//                return Contract.Users.CONTENT_ITEM_TYPE;
            case SIGNAL:
                return Contract.Signals.CONTENT_TYPE;
            case SIGNAL_BY_REFERENCE:
                return Contract.Signals.CONTENT_TYPE;
            case CELL_LOCATION:
                return Contract.CellLocations.CONTENT_TYPE;
            case CELL_LOCATION_BY_REFERENCE:
                return Contract.CellLocations.CONTENT_TYPE;
            case LOCATION:
                return Contract.Locations.CONTENT_TYPE;
            case LOCATION_BY_REFERENCE:
                return Contract.Locations.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        Log.d(TAG, "Inserting into " + uri.toString() + " values " + values.toString());

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Long id = null;
        switch (uriMatcher.match(uri)) {
            case ZERO_MEASUREMENT: {
                id = db.insertWithOnConflict(Database.Tables.ZERO_MEASUREMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.withAppendedPath(Contract.ZeroMeasurements.CONTENT_URI, String.valueOf(id));
            }
//            case ZERO_MEASUREMENT_BY_ID: {
//                db.insertWithOnConflict(Database.Tables.USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
//                getContext().getContentResolver().notifyChange(uri, null);
//                String providerUUID = values.getAsString(Contract.UserColumns.USER_UUID);
//                if (providerUUID != null) {
//                    getContext().getContentResolver().notifyChange(Contract.Users.buildUserUri(providerUUID), null);
//                }
//                return Contract.Users.buildUserUri(values.getAsString(values.getAsString(Contract.UserColumns.USER_UUID)));
//            }
            case SIGNAL:
                id = db.insertWithOnConflict(Database.Tables.SIGNALS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.withAppendedPath(Contract.Signals.CONTENT_URI, String.valueOf(id));
            case CELL_LOCATION:
                id = db.insertWithOnConflict(Database.Tables.CELL_LOCATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.withAppendedPath(Contract.CellLocations.CONTENT_URI, String.valueOf(id));
            case LOCATION:
                id = db.insertWithOnConflict(Database.Tables.LOCATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.withAppendedPath(Contract.Locations.CONTENT_URI, String.valueOf(id));
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int result = 0;
        switch (uriMatcher.match(uri)) {
            case ZERO_MEASUREMENT:
                result = db.delete(Database.Tables.ZERO_MEASUREMENTS, selection, selectionArgs);
                break;
            case SIGNAL:
                result = db.delete(Database.Tables.SIGNALS, selection, selectionArgs);
                break;
            case CELL_LOCATION:
                result = db.delete(Database.Tables.CELL_LOCATIONS, selection, selectionArgs);
                break;
            case LOCATION:
                result = db.delete(Database.Tables.LOCATIONS, selection, selectionArgs);
                break;
        }
        return result;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        Log.d(TAG, "Updating " + uri.toString() + " with values " + values.toString() + " (selection = " + selection + ")");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String tableName;

        List<Uri> notificationUris = new ArrayList<>();
//        notificationUris.add(uri);

        switch (uriMatcher.match(uri)) {
            case ZERO_MEASUREMENT:
                tableName = Database.Tables.ZERO_MEASUREMENTS;
                notificationUris.add(uri);
                break;
            case SIGNAL:
                tableName = Database.Tables.SIGNALS;
                notificationUris.add(uri);
                break;
            case CELL_LOCATION:
                tableName = Database.Tables.CELL_LOCATIONS;
                notificationUris.add(uri);
                break;
            case LOCATION:
                tableName = Database.Tables.LOCATIONS;
                notificationUris.add(uri);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int result = db.updateWithOnConflict(tableName, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_IGNORE);

        for (Uri notificationUri : notificationUris) {
            getContext().getContentResolver().notifyChange(notificationUri, null);
        }

        return result;
    }
}
