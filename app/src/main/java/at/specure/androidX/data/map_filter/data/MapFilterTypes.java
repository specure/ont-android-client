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

package at.specure.androidX.data.map_filter.data;

/**
 * Filter types to distinct types in response json object
 * Created by michal.cadrik on 10/25/2017.
 */

@SuppressWarnings("unused")
public class MapFilterTypes {

    public static final String MAP_FILTER_TYPE_UNKNOWN = "MFT_UNKNOWN";
    public static final String MAP_FILTER_TYPE_OPERATOR = "MFT_OPERATOR";
    public static final String MAP_FILTER_TYPE_MAP_APPEARANCE = "MFT_MAP_APPEARANCE";
    public static final String MAP_FILTER_TYPE_MAP_OVERLAY = "MFT_MAP_OVERLAY";
    public static final String MAP_FILTER_TYPE_STATISTIC_TYPE = "MFT_MAP_STATISTIC_TYPE";
    public static final String MAP_FILTER_TYPE_PERIOD = "MFT_PERIOD";
    public static final String MAP_FILTER_TYPE_TECHNOLOGY = "MFT_TECHNOLOGY";
    public static final String MAP_FILTER_TYPE_TYPE = "MFT_TYPE";
    public static final String MAP_FILTER_TYPE_MAP_LAYOUT= "MFT_MAP_LAYOUT";
    public static final String MAP_FILTER_TYPE_COUNTRY= "MFT_COUNTRY";
    // these 4 below are used also for getting providers according to technology
    public static final String MAP_FILTER_TYPE_ALL = "MFT_ALL";
    public static final String MAP_FILTER_TYPE_WLAN = "MFT_WLAN";
    public static final String MAP_FILTER_TYPE_MOBILE = "MFT_MOBILE";
    public static final String MAP_FILTER_TYPE_BROWSER = "MFT_BROWSER";


}
