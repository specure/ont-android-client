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

import android.net.Uri;
import android.provider.BaseColumns;

import com.specure.opennettest.BuildConfig;


/**
 * Handles all uri creation codes
 */
public class Contract {

    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;
    //GENERAL
    public static final String PATH_BY_REFERENCE = "by_reference";

    // URI Paths
    public static final String PATH_ZERO_MEASUREMENTS = "zero_measurements";
    public static final String PATH_SIGNALS = "signals";
    public static final String PATH_CELL_LOCATIONS = "cell_locations";
    public static final String PATH_LOCATIONS = "locations";


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public interface ZeroMeasurementsColumns {
        String ID = BaseColumns._ID;
        String SERVER_ID = "zm_server_id";
        /**
         * @see at.specure.android.database.enums.ZeroMeasurementState
         **/
        String STATE = "zm_state";

        String UUID = "zm_uuid";
        String TIME = "zm_time";
        String PLATFORM = "zm_platform";
        String PRODUCT = "zm_product";
        String API_LEVEL = "zm_api_level";
        String OS_VERSION = "zm_os_version";
        String NETWORK_TYPE = "zm_network_type";
        String MODEL = "zm_model";
        String DEVICE = "zm_device";

        String CLIENT_UUID = "zm_client_uuid";
        String CLIENT_NAME = "zm_client_name";
        String CLIENT_VERSION = "zm_client_version";
        String CLIENT_LANG = "zm_client_lang";
        String CLIENT_SOFT_VERSION = "zm_client_soft_version";

        String TEL_NET_OPERATOR = "zm_tel_net_operator";
        String TEL_NET_IS_ROAMING = "zm_tel_net_is_roaming";
        String TEL_NET_COUNTRY = "zm_tel_net_country";
        String TEL_NET_OPERATOR_NAME = "zm_tel_net_operator_name";
        String TEL_NET_SIM_OPERATOR_NAME = "zm_tel_net_sim_operator_name";
        String TEL_NET_SIM_OPERATOR = "zm_tel_net_sim_operator";
        String TEL_NET_SIM_COUNTRY = "zm_tel_net_sim_country";
        String TEL_PHONE_TYPE = "zm_tel_phone_type";
        String TEL_DATA_STATE = "zm_tel_data_state";
        String TIMEZONE = "zm_timezone";
    }


    public interface SignalsColumns {
        String ID = BaseColumns._ID;
        String TIME = "signal_timestamp";
        String NETWORK_TYPE_ID = "signal_network_id";
        String LTE_RSRP = "signal_lte_rsrp";
        String LTE_RSRQ = "signal_lte_rsrq";
        String LTE_RSSNR = "signal_lte_rssnr";
        String LTE_CQI = "signal_lte_cqi";
        String SIGNAL_STRENGTH = "signal_strength";
        String GSM_BIT_ERROR_RATE = "signal_gsm_bit_error_rate";
        String TIME_AGE = "signal_time_age";
        /**
         * @see at.specure.android.database.enums.SignalType
         **/
        String TYPE = "signal_type";
        String REF_ID = "signal_reference_id";
    }

    public interface LocationsColumns {
        String ID = BaseColumns._ID;
        String TIME = "location_timestamp";
        String TIME_AGE = "location_time_age";
        String LATITUDE = "location_latitude";
        String LONGITUDE = "location_longtitude";
        String ACCURACY = "location_accuracy";
        String ALTITUDE = "location_altitude";
        String BEARING = "location_bearing";
        String SPEED = "location_speed";
        /**
         * @see at.specure.android.database.enums.LocationType
         **/
        String TYPE = "location_type";
        String PROVIDER = "location_provider";
        String REF_ID = "location_reference_id";
    }

    public interface CellLocationsColumns {
        String ID = BaseColumns._ID;
        String TIME = "cell_location_timestamp";
        String TIME_AGE = "cell_location_time_age";
        String LOCATION_ID = "cell_location_id";
        String AREA_CODE = "cell_location_area_code";
        String PRIMARY_SCRAMBLING_CODE = "cell_location_primary_scrambling_code";
        /**
         * @see at.specure.android.database.enums.CellLocationType
         **/
        String TYPE = "cell_location_type";
        String REF_ID = "cell_location_reference_id";
    }


    public static final class ZeroMeasurements implements ZeroMeasurementsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ZERO_MEASUREMENTS).build();

        public static final String FULL_ID = Database.Tables.ZERO_MEASUREMENTS + "." + ID;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zero_measurements";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zero_measurements";

        public static final String DEFAULT_SORT = ID;

        /**
         * Build {@link Uri} for requested {@link #ID}.
         */
        public static Uri buildZeroMeasurementUri(String zeroMeasurementId) {
            return CONTENT_URI.buildUpon().appendPath(zeroMeasurementId).build();
        }

        /**
         * Read {@link #ID} from {@link ZeroMeasurements} {@link Uri}.
         */
        public static String getZeroMeasurementId(Uri uri) {
            return uri.getPathSegments().get(1);
        }


    }

    public static final class Signals implements SignalsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SIGNALS).build();

        public static final String FULL_ID = Database.Tables.SIGNALS + "." + ID;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.signals";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.signals";

        public static final String DEFAULT_SORT = ID;

        /**
         * Build {@link Uri} for requested {@link #ID}.
         */
        public static Uri buildSignalsUri(String zeroMeasurementId) {
            return CONTENT_URI.buildUpon().appendPath(zeroMeasurementId).build();
        }

        /**
         * Read {@link #ID} from {@link Signals} {@link Uri}.
         */
        public static String getSignalsId(Uri uri) {
            return uri.getPathSegments().get(1);
        }


        public static String getReferenceId(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildSignalsForStg(Long refId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_BY_REFERENCE).appendPath(refId.toString()).build();
        }


    }

    public static final class CellLocations implements CellLocationsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CELL_LOCATIONS).build();

        public static final String FULL_ID = Database.Tables.CELL_LOCATIONS + "." + ID;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cell_locations";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cell_locations";

        public static final String DEFAULT_SORT = ID;

        /**
         * Build {@link Uri} for requested {@link #ID}.
         */
        public static Uri buildCellLocationsUri(String cellLocationId) {
            return CONTENT_URI.buildUpon().appendPath(cellLocationId).build();
        }

        /**
         * Read {@link #ID} from {@link CellLocations} {@link Uri}.
         */
        public static String getCellLocationId(Uri uri) {
            return uri.getPathSegments().get(1);
        }


        public static String getReferenceId(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildCellLocationForStg(Long refId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_BY_REFERENCE).appendPath(refId.toString()).build();
        }
    }

    public static final class Locations implements LocationsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();

        public static final String FULL_ID = Database.Tables.LOCATIONS + "." + ID;
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.locations";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.locations";

        public static final String DEFAULT_SORT = ID;

        /**
         * Build {@link Uri} for requested {@link #ID}.
         */
        public static Uri buildLocationsUri(String locationId) {
            return CONTENT_URI.buildUpon().appendPath(locationId).build();
        }

        /**
         * Read {@link #ID} from {@link Locations} {@link Uri}.
         */
        public static String getLocationId(Uri uri) {
            return uri.getPathSegments().get(1);
        }


        public static String getReferenceId(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildLocationsForStg(Long refId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_BY_REFERENCE).appendPath(refId.toString()).build();
        }

    }


}
