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

import java.util.ArrayList;
import java.util.List;

import at.specure.android.database.Contract;
import at.specure.android.database.DatabaseHelper;
import at.specure.android.util.InformationCollector;

/**
 * Created by michal.cadrik on 8/10/2017.
 */

public class TCellLocation {

    public String id;
    public Long primaryScramblingCode;
    public Long areaCode;
    public Long locationId;
    public Integer type;
    public Long refId;
    public Long time;
    public Long timeAge;


    public TCellLocation(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        id = DatabaseHelper.getStringCursor(cursor, Contract.CellLocationsColumns.ID);
        primaryScramblingCode = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.PRIMARY_SCRAMBLING_CODE);
        areaCode = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.AREA_CODE);
        locationId = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.LOCATION_ID);
        time = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.TIME);
        timeAge = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.TIME_AGE);
        type = DatabaseHelper.getIntCursor(cursor, Contract.CellLocationsColumns.TYPE);
        refId = DatabaseHelper.getLongCursor(cursor, Contract.CellLocationsColumns.REF_ID);
    }

    public TCellLocation(Long primaryScramblingCode, Long areaCode, Long locationId, Integer type, Long time, Long timeAge) {
        this.primaryScramblingCode = primaryScramblingCode;
        this.areaCode = areaCode;
        this.locationId = locationId;
        this.type = type;
        this.time = time;
        this.timeAge = timeAge;
    }

    public static List<TCellLocation> convertToTs(List<InformationCollector.CellLocationItem> cellLocations, int type) {
        ArrayList<TCellLocation> result = new ArrayList<>();
        if ((cellLocations != null) && (!cellLocations.isEmpty())) {
            for (InformationCollector.CellLocationItem cellLocation : cellLocations) {
                TCellLocation tCellLocation = new TCellLocation((long) cellLocation.scramblingCode, (long) cellLocation.areaCode,
                        (long) cellLocation.locationId, type, cellLocation.tstamp, cellLocation.tstampNano);
                result.add(tCellLocation);
            }
        }
        return result;
    }

    public Uri save(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(Contract.CellLocationsColumns.PRIMARY_SCRAMBLING_CODE, this.primaryScramblingCode);
        cv.put(Contract.CellLocationsColumns.AREA_CODE, this.areaCode);
        cv.put(Contract.CellLocationsColumns.LOCATION_ID, this.locationId);
        cv.put(Contract.CellLocationsColumns.TIME, this.time);
        cv.put(Contract.CellLocationsColumns.TIME_AGE, this.timeAge);
        cv.put(Contract.CellLocationsColumns.TYPE, this.type);
        cv.put(Contract.CellLocationsColumns.REF_ID, this.refId);
        return contentResolver.insert(Contract.CellLocations.CONTENT_URI, cv);

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
