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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import at.specure.android.api.jsons.Location;
import at.specure.android.database.Contract;
import at.specure.android.database.DatabaseHelper;

/**
 * Created by michal.cadrik on 8/10/2017.
 */

public class TLocation {

    public Integer id;
    public Long timestamp;
    public Long timeAge;
    public Double latitude;
    public Double longitude;
    public Double accuracy;
    public Double altitude;
    public Double bearing;
    public Double speed;
    public String provider;
    public Integer type;
    public Long refId;


    public TLocation(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        id = DatabaseHelper.getIntCursor(cursor, Contract.LocationsColumns.ID);
        latitude = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.LATITUDE);
        longitude = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.LONGITUDE);
        accuracy = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.ACCURACY);
        altitude = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.ALTITUDE);
        bearing = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.BEARING);
        speed = DatabaseHelper.getDoubleCursor(cursor, Contract.LocationsColumns.SPEED);
        provider = DatabaseHelper.getStringCursor(cursor, Contract.LocationsColumns.PROVIDER);
        timestamp = DatabaseHelper.getLongCursor(cursor, Contract.LocationsColumns.TIME);
        timeAge = DatabaseHelper.getLongCursor(cursor, Contract.LocationsColumns.TIME_AGE);
        type = DatabaseHelper.getIntCursor(cursor, Contract.LocationsColumns.TYPE);
        refId = DatabaseHelper.getLongCursor(cursor, Contract.LocationsColumns.REF_ID);
    }

    public TLocation(Location location, int type) {
        if (location == null) {
            return;
        }
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.bearing = location.getBearing();
        this.speed = location.getSpeed();
        this.provider = location.getProvider();
        this.timestamp = location.getTimestamp();
        this.timeAge = location.getTime();
        this.type = type;
    }

    public TLocation(long time, long elapsedRealtimeNanos, double latitude, double longitude, Double accuracy, double altitude, Double bearing, Double speed, String provider, int type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.provider = provider;
        this.timestamp = time;
        this.timeAge = elapsedRealtimeNanos;
        this.type = type;
    }

    public Uri save(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(Contract.LocationsColumns.LATITUDE, this.latitude);
        cv.put(Contract.LocationsColumns.LONGITUDE, this.longitude);
        cv.put(Contract.LocationsColumns.ACCURACY, this.accuracy);
        cv.put(Contract.LocationsColumns.ALTITUDE, this.altitude);
        cv.put(Contract.LocationsColumns.BEARING, this.bearing);
        cv.put(Contract.LocationsColumns.SPEED, this.speed);
        cv.put(Contract.LocationsColumns.PROVIDER, this.provider);
        cv.put(Contract.LocationsColumns.TIME, this.timestamp);
        cv.put(Contract.LocationsColumns.TIME_AGE, this.timeAge);
        cv.put(Contract.LocationsColumns.TYPE, this.type);
        cv.put(Contract.LocationsColumns.REF_ID, this.refId);
        return contentResolver.insert(Contract.Locations.CONTENT_URI, cv);
    }


//    public static ContentValues getSaveProductContentValues(TSignal signal, Integer zeroMeasurementId, Context context) {
//        ContentValues cv = new ContentValues();
//        cv.put(Contract.ProductColumns.ID, product.getProductUUID());
//        cv.put(Contract.ProductColumns.PRODUCT_UUID, product.getProductUUID());
//        cv.put(Contract.ProductColumns.FIX_PRICE, product.getPrice());
//        cv.put(Contract.ProductColumns.NAME, product.getName());
//        cv.put(Contract.ProductColumns.DURATION, product.getDuration());
//        cv.put(Contract.ProductColumns.DESCRIPTION, product.getDescription());
//        cv.put(Contract.ProductColumns.PHOTO_UUID, product.getPhoto());
//        cv.put(Contract.ProductColumns.CITY_UUID, product.getCity().getUuid());
//        cv.put(Contract.ProductColumns.PRODUCT_CREATED_AT, product.getCreatedAt());
//        cv.put(Contract.ProductColumns.PRODUCT_UPDATED_AT, product.getUpdatedAt());
//        cv.put(Contract.ProductColumns.RATING_AVERAGE, product.getRatingAverage());
//        cv.put(Contract.ProductColumns.RATING_AVERAGE_WEIGHTED, product.getRatingAverageWeighted());
//        cv.put(Contract.ProductColumns.RATING_COUNT, product.getRatingCount());
//        cv.put(Contract.ProductColumns.GUIDE_UUID, product.getProvider().getUuid());
//        cv.put(Contract.ProductColumns.LOCAL_TYPE, bookmarkedProducts.contains(product.getProductUUID()) ? Product.LOCAL_TYPE_OFFLINE_BOOKMARKS : 0);
//        cv.put(Contract.ProductColumns.IS_WEATHER_DEPENDENT, product.getWeatherDependent());
//        cv.put(Contract.ProductColumns.TIMES_BOUGHT, product.getTimesBought());
//        cv.put(Contract.ProductColumns.STATUS, product.getStatus());
//        cv.put(Contract.ProductColumns.START_LATITUDE, product.getGpsLatitude());
//        cv.put(Contract.ProductColumns.START_LONGITUDE, product.getGpsLongitude());
//        cv.put(Contract.ProductColumns.PRODUCT_GROUPS, product.getProductGroups());
//        cv.put(Contract.ProductColumns.MAX_PEOPLE, product.getCapacity());
//
//        Tag[] tags = product.getTags();
//        if (tags != null && tags.length > 0) {
//            for (int i = 0; i < tags.length; i++) {
//                ProductTag.saveProductTags(tags[i], product.getProductUUID(), context);
//            }
//        }
////        insertLanguages(cv, product.getProductGroups());
//        return cv;
//    }*/
//
//  /*  public static void saveProduct(GetProductResponseBody product, Context context) {
//        ContentValues cv = getSaveProductContentValues(product, context);
//        context.getContentResolver().insert(Contract.Products.CONTENT_URI, cv);
//    }*/


}
