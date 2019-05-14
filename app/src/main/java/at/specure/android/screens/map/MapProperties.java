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
package at.specure.android.screens.map;

import android.content.Context;

import com.specure.opennettest.BuildConfig;
import com.specure.opennettest.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @author alladin-IT GmbH
 */
public interface MapProperties {

    public static final String MapServerUrl = "/" + BuildConfig.mapServerUrl;

    /**
     *
     */
    public static final int TILE_SIZE = 256;

    /**
     *
     */
//    public static final LatLng DEFAULT_MAP_CENTER = new LatLng(46.049053, 14.501973);

    public static final float DEFAULT_MAP_ZOOM = 10.5f;

    public static final float DEFAULT_MAP_ZOOM_LOCATION = 12f;

    public static final float POINT_MAP_ZOOM = 14f;

    /**
     *
     */
    /* north, east, south, west */
    // 49.5, 17.5, 46.25, 9.25 -> 49.05, 17.25, 46.35, 9.4
    // http://www.openstreetmap.org/?minlon=9.45&minlat=46.355&maxlon=17.20&maxlat=49.00&box=yes
    // public static final BoundingBoxE6 BOUNDING_BOX = new BoundingBoxE6(49,
    // 17.2, 46.355, 9.45);

    /**
     *
     */
    public static final String MARKER_PATH = MapServerUrl + "/V2/tiles/markers";

    /**
     *
     */
    public static final String MAP_OPTIONS_PATH = MapServerUrl + "/tiles/info";

    public static final String MAP_OPERATORS_FILTER_PATH = MapServerUrl + "/tiles/mapFilterOperators";

    public static final String MAP_OPERATORS_FILTER_PATH_V2 = MapServerUrl + "/V2/tiles/mapFilterOperators";
    public static final String MAP_OPTIONS_PATH_V2 = MapServerUrl + "/V2/tiles/info";
    /**
     *
     */
    public static final String MAP_SAT_KEY = "_SAT";

    public static final String MAP_COUNTRY_KEY = "country";

    /**
     *
     */
    public static final String MAP_SAT_VALUE = "SAT";

    /**
     *
     */
    public static final String MAP_NOSAT_VALUE = "NOSAT";

    /**
     *
     */
    public static final String MAP_OVERLAY_KEY = "_OVERLAY";

    /**
     *
     */
    public static final int MAP_AUTO_SWITCH_VALUE = 14;



    /**
     *
     */
    public static final int POINT_DIAMETER = 12;

    /**
     *
     */
    public static final double TAB_DIAMETER_FACTOR = 2;

}
