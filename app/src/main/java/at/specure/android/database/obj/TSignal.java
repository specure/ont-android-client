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

import at.specure.android.database.Contract;
import at.specure.android.database.DatabaseHelper;
import at.specure.android.util.InformationCollector;

/**
 * Created by michal.cadrik on 8/10/2017.
 */

public class TSignal {

    public Integer id;
    public Integer lteCQI;
    public Integer lteRSRP;
    public Integer lteRSRQ;
    public Integer lteRSSNR;
    public Integer gsmBitErrorRate;
    public Integer signalStrength;
    public Integer networkTypeId;
    public Integer type;
    public Long refId;
    public Long time;
    public Long timeAge;


    public TSignal(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        id = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.ID);
        lteCQI = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.LTE_CQI);
        lteRSRP = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.LTE_RSRP);
        lteRSRQ = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.LTE_RSRQ);
        lteRSSNR = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.LTE_RSSNR);
        gsmBitErrorRate = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.GSM_BIT_ERROR_RATE);
        networkTypeId = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.NETWORK_TYPE_ID);
        signalStrength = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.SIGNAL_STRENGTH);
        time = DatabaseHelper.getLongCursor(cursor, Contract.SignalsColumns.TIME);
        timeAge = DatabaseHelper.getLongCursor(cursor, Contract.SignalsColumns.TIME_AGE);
        type = DatabaseHelper.getIntCursor(cursor, Contract.SignalsColumns.TYPE);
        refId = DatabaseHelper.getLongCursor(cursor, Contract.SignalsColumns.REF_ID);
    }


    public TSignal(Integer lteCQI, Integer lteRSRP, Integer lteRSRQ, Integer lteRSSNR, Integer gsmBitErrorRate, Integer signalStrength, Integer networkTypeId, Integer type, Long time, Long timeAge) {
        this.lteCQI = lteCQI;
        this.lteRSRP = lteRSRP;
        this.lteRSRQ = lteRSRQ;
        this.lteRSSNR = lteRSSNR;
        this.gsmBitErrorRate = gsmBitErrorRate;
        this.signalStrength = signalStrength;
        this.networkTypeId = networkTypeId;
        this.type = type;
        this.time = time;
        this.timeAge = timeAge;
    }



    public TSignal(InformationCollector.SignalItem lastSignalItem, int unknown, int type) {

        if (lastSignalItem.gsmBitErrorRate != unknown) {
            this.gsmBitErrorRate = lastSignalItem.gsmBitErrorRate;
        }

        if (lastSignalItem.lteCqi != unknown) {
            this.lteCQI = lastSignalItem.lteCqi;
        }

        if (lastSignalItem.lteRsrp != unknown) {
            this.lteRSRP = lastSignalItem.lteRsrp;
        }

        if (lastSignalItem.lteRsrq != unknown) {
            this.lteRSRQ = lastSignalItem.lteRsrq;
        }

        if (lastSignalItem.lteRssnr != unknown) {
            this.lteRSSNR = lastSignalItem.lteRssnr;
        }

        if (lastSignalItem.signalStrength != unknown) {
            this.signalStrength = lastSignalItem.signalStrength;
        }

        if (lastSignalItem.networkId != unknown) {
            this.networkTypeId = lastSignalItem.networkId;
        }

        if (lastSignalItem.tstamp != unknown) {
            this.time = lastSignalItem.tstamp;
        }

        if (lastSignalItem.tstampNano != unknown) {
            this.timeAge = lastSignalItem.tstampNano;
        }
        this.type = type;
    }

    public Uri save(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(Contract.SignalsColumns.LTE_CQI, this.lteCQI);
        cv.put(Contract.SignalsColumns.LTE_RSRP, this.lteRSRP);
        cv.put(Contract.SignalsColumns.LTE_RSRQ, this.lteRSRQ);
        cv.put(Contract.SignalsColumns.LTE_RSSNR, this.lteRSSNR);
        cv.put(Contract.SignalsColumns.GSM_BIT_ERROR_RATE, this.gsmBitErrorRate);
        cv.put(Contract.SignalsColumns.SIGNAL_STRENGTH, this.signalStrength);
        cv.put(Contract.SignalsColumns.NETWORK_TYPE_ID, this.networkTypeId);
        cv.put(Contract.SignalsColumns.TYPE, this.type);
        cv.put(Contract.SignalsColumns.TIME, this.time);
        cv.put(Contract.SignalsColumns.TIME_AGE, this.timeAge);
        cv.put(Contract.SignalsColumns.TYPE, this.type);
        cv.put(Contract.SignalsColumns.REF_ID, this.refId);
        return contentResolver.insert(Contract.Signals.CONTENT_URI, cv);
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
