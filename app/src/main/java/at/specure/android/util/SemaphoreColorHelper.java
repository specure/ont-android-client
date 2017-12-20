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

import android.content.Context;

import com.specure.opennettest.R;


/**
 * Created by michal.cadrik on 8/30/2017.
 */

public class SemaphoreColorHelper {

    public static final int SEMAPHORE_TYPE_DOWNLOAD_WIFI = 0;
    public static final int SEMAPHORE_TYPE_DOWNLOAD_LTE = 1;
    public static final int SEMAPHORE_TYPE_DOWNLOAD_GSM = 2;

    public static final int SEMAPHORE_TYPE_UPLOAD_WIFI = 3;
    public static final int SEMAPHORE_TYPE_UPLOAD_LTE = 4;
    public static final int SEMAPHORE_TYPE_UPLOAD_GSM = 5;

    public static final int SEMAPHORE_TYPE_PING_WIFI = 6;
    public static final int SEMAPHORE_TYPE_PING_LTE = 7;
    public static final int SEMAPHORE_TYPE_PING_GSM = 8;

    public static final int SEMAPHORE_TYPE_SIGNAL_WIFI = 9;
    public static final int SEMAPHORE_TYPE_SIGNAL_LTE = 10;
    public static final int SEMAPHORE_TYPE_SIGNAL_GSM = 11;

    /**
     * Method to get color id of semaphore for particular value
     * @param context
     * @param value signal value in dbm
     * @param type see @{@link SemaphoreColorHelper} constants
     * @return id of color resource
     */
    public static int resolveSemaphoreColor(Context context, int value, int type) {

        switch (type) {
            case SEMAPHORE_TYPE_SIGNAL_GSM:
                if (value > context.getResources().getInteger(R.integer.signal_semaphore_gms_green)) {
                    return R.color.classification_green;
                } else if (value > context.getResources().getInteger(R.integer.signal_semaphore_gms_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }
            case SEMAPHORE_TYPE_SIGNAL_LTE:
                if (value > context.getResources().getInteger(R.integer.signal_semaphore_lte_green)) {
                    return R.color.classification_green;
                } else if (value > context.getResources().getInteger(R.integer.signal_semaphore_lte_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }
            case SEMAPHORE_TYPE_SIGNAL_WIFI:
                if (value > context.getResources().getInteger(R.integer.signal_semaphore_wifi_green)) {
                    return R.color.classification_green;
                } else if (value > context.getResources().getInteger(R.integer.signal_semaphore_wifi_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }


            case SEMAPHORE_TYPE_DOWNLOAD_WIFI:
            case SEMAPHORE_TYPE_DOWNLOAD_GSM:
            case SEMAPHORE_TYPE_DOWNLOAD_LTE:
                if (value > context.getResources().getInteger(R.integer.download_semaphore_green)) {
                    return R.color.classification_green;
                } else if (value > context.getResources().getInteger(R.integer.download_semaphore_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }


            case SEMAPHORE_TYPE_UPLOAD_GSM:
            case SEMAPHORE_TYPE_UPLOAD_LTE:
            case SEMAPHORE_TYPE_UPLOAD_WIFI:
                if (value > context.getResources().getInteger(R.integer.upload_semaphore_green)) {
                    return R.color.classification_green;
                } else if (value > context.getResources().getInteger(R.integer.upload_semaphore_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }


            case SEMAPHORE_TYPE_PING_GSM:
            case SEMAPHORE_TYPE_PING_LTE:
            case SEMAPHORE_TYPE_PING_WIFI:
                if (value < context.getResources().getInteger(R.integer.ping_semaphore_green)) {
                    return R.color.classification_green;
                } else if (value < context.getResources().getInteger(R.integer.ping_semaphore_orange)) {
                    return R.color.classification_yellow;
                } else {
                    return R.color.classification_red;
                }

            default:
                return R.color.classification_none;
        }
    }

    /**
     *
     * @param classificationClass int from server
     */
    public static int resolveSemaphoreColor(int classificationClass) {
        switch (classificationClass) {
            case -1:
                return R.color.classification_none;
            case 0:
                return R.color.classification_grey;
            case 1:
                return R.color.classification_red;
            case 2:
                return R.color.classification_yellow;
            case 3:
                return R.color.classification_green;
            default:
                return  R.color.classification_none;

        }
    }

    /**
     *
     * @param classificationClass int from server
     */
    public static int resolveSemaphoreImage(int classificationClass) {
        switch (classificationClass) {
            case -1:
                return R.drawable.traffic_lights_none;
            case 0:
                return R.drawable.traffic_lights_grey;
            case 1:
                return R.drawable.traffic_lights_red;
            case 2:
                return R.drawable.traffic_lights_yellow;
            case 3:
                return R.drawable.traffic_lights_green;
            default:
                return R.drawable.traffic_lights_none;
        }
    }

}
