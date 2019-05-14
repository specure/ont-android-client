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

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;
import android.text.TextUtils;

/**
 * Utils for work with database. Including safe getting values from database.
 */
public final class DatabaseHelper {

    public static final int UNDEFINED_INT = -1;
    public static final long UNDEFINED_LONG = -1;
    public static final float UNDEFINED_FLOAT = -1;
    public static final double UNDEFINED_DOUBLE = -1;
    public static final String UNDEFINED_STRING = null;
    public static final String[] UNDEFINED_STRING_ARRAY = null;

    private DatabaseHelper() {
    }

    /**
     * Get int value from cursor based on columnName
     *
     * @param cursor     to get int value from
     * @param columnName to get int value from
     * @return int value from cursor or {@link #UNDEFINED_INT} if column doesn't exists
     */
    public static int getIntCursor(Cursor cursor, String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getInt(columnId);
        } else {
            return UNDEFINED_INT;
        }
    }

    /**
     * Get boolean value from cursor based on columnName
     *
     * @param cursor     to get boolean value from
     * @param columnName to get boolean value from
     * @return boolean value from cursor or false if column doesn't exists
     */
    public static boolean getBooleanCursor(Cursor cursor, @SuppressWarnings("SameParameterValue") String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getInt(columnId) > 0;
        } else {
            return false;
        }
    }

    /**
     * Get long value from cursor based on columnName
     *
     * @param cursor     to get long value from
     * @param columnName to get long value from
     * @return long value from cursor or {@link #UNDEFINED_LONG} if column doesn't exists
     */
    public static long getLongCursor(Cursor cursor, String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getLong(columnId);
        } else {
            return UNDEFINED_LONG;
        }
    }

    /**
     * Get float value from cursor based on columnName
     *
     * @param cursor     to get float value from
     * @param columnName to get float value from
     * @return float value from cursor or {@link #UNDEFINED_FLOAT} if column doesn't exists
     */
    public static float getFloatCursor(Cursor cursor, String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getFloat(columnId);
        } else {
            return UNDEFINED_FLOAT;
        }
    }

    /**
     * Get double value from cursor based on columnName
     *
     * @param cursor     to get double value from
     * @param columnName to get double value from
     * @return double value from cursor or {@link #UNDEFINED_DOUBLE} if column doesn't exists
     */
    public static double getDoubleCursor(Cursor cursor, String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getDouble(columnId);
        } else {
            return UNDEFINED_DOUBLE;
        }
    }

    /**
     * Get String value from cursor based on columnName
     *
     * @param cursor     to get String value from
     * @param columnName to get String value from
     * @return String value from cursor or {@link #UNDEFINED_STRING} if column doesn't exists
     */
    public static String getStringCursor(Cursor cursor, String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            return cursor.getString(columnId);
        } else {
            return UNDEFINED_STRING;
        }
    }

    /**
     * Get String[] value from cursor based on columnName
     *
     * @param cursor     to get String[] value from
     * @param columnName to get String[] value from
     * @return String[] value from cursor or {@link #UNDEFINED_STRING_ARRAY} if column doesn't exists or empty
     */
    public static String[] getStringArrayCursor(Cursor cursor, @SuppressWarnings("SameParameterValue") String columnName) {
        int columnId = cursor.getColumnIndex(columnName);
        if (columnId != -1) {
            String all = cursor.getString(columnId);
            if (!TextUtils.isEmpty(all)) {
                return all.split(",");
            }
        }
        return UNDEFINED_STRING_ARRAY;
    }

    /**
     * Puts int to {@link ContentValues} if defined
     *
     * @param contentValues to store int to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, int value, String columnName) {
        if (value != DatabaseHelper.UNDEFINED_INT) {
            contentValues.put(columnName, value);
        }
    }

    /**
     * Puts long to {@link ContentValues} if defined
     *
     * @param contentValues to store long to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, long value, String columnName) {
        if (value != DatabaseHelper.UNDEFINED_LONG) {
            contentValues.put(columnName, value);
        }
    }

    /**
     * Puts float to {@link ContentValues} if defined
     *
     * @param contentValues to store float to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, float value, @SuppressWarnings("SameParameterValue") String columnName) {
        if (value != DatabaseHelper.UNDEFINED_FLOAT) {
            contentValues.put(columnName, value);
        }
    }

    /**
     * Puts String to {@link ContentValues} if defined
     *
     * @param contentValues to store String to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, String value, String columnName) {
        if (value != null) {
            contentValues.put(columnName, value);
        }
    }

    /**
     * Puts String to {@link ContentValues} if defined
     *
     * @param contentValues to store String to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, String[] value, @SuppressWarnings("SameParameterValue") String columnName) {
        if (value != null) {
            StringBuilder transform = new StringBuilder();
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    transform.append(',');
                }
                String act = value[i];
                transform.append(act);
            }
            contentValues.put(columnName, transform.toString());
        }
    }

    /**
     * Puts boolean to {@link ContentValues}
     *
     * @param contentValues to store boolean to
     * @param value         to be stored
     * @param columnName    in database
     */
    public static void putToContentValues(ContentValues contentValues, boolean value, @SuppressWarnings("SameParameterValue") String columnName) {
        contentValues.put(columnName, value);
    }

    /**
     * Returns position in cursor, where row contains exactly the same value in defined column.
     *
     * @param cursor      to compare values in
     * @param columnName  from which value is get
     * @param columnValue that column should have
     * @return position in cursor or -1 if any error
     */
    public static int getPosition(@NonNull Cursor cursor, @NonNull String columnName, @NonNull String columnValue) {
        if (cursor.getCount() == 0) {
            return -1;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String value = cursor.getString(cursor.getColumnIndex(columnName));
            if (value.equals(columnValue)) return i;
        }
        return -1;
    }
}
