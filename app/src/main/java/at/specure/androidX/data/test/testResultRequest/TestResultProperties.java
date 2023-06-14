package at.specure.androidX.data.test.testResultRequest;

import com.google.gson.annotations.SerializedName;

/**
 * previously info object in Information Collector
 */
public class TestResultProperties {

        /**
         * WIFI only
         **/
        @SerializedName("wifi_supplicant_state_detail")
        public String wifiSupplicantStateDetail;  // e.g. OBTAINING_IPADDR, todo: ???

        @SerializedName("wifi_supplicant_state")
        public String wifiSupplicantState;  // e.g. COMPLETED, todo: ???

        @SerializedName("wifi_ssid")
        public String wifiSSID; // e.g. Martes-Specure

        @SerializedName("wifi_bssid")
        public String wifiBSSID;   //e.g. b8:ec:a3:f8:3f:08

        @SerializedName("wifi_network_id")
        public String wifiNetworkId; //e.g. "4"

//removed previously also in info object
//    @SerializedName("wifi_linkspeed")
//    String wifiLinkspeed;
//
//    @SerializedName("wifi_rssi")
//    String wifirssi;

        /** END WIFI only **/

        /**
         * MOBILE NET ONLY
         **/

        @SerializedName("telephony_network_operator")
        public String telephonyNetworkOperator; //todo: ??? not filled

        @SerializedName("telephony_network_is_roaming")
        public Boolean telephonyNetworkIsRoaming = false;//todo: ??? not filled

        @SerializedName("telephony_network_country")
        public String telephonyNetworkCountry; //todo: ??? not filled

        @SerializedName("telephony_network_operator_name")
        public String telephonyNetworkOperatorName; //e.g. "O2 - SK"

        @SerializedName("telephony_network_sim_operator_name")
        public String telephonyNetworkSimOperatorName; // todo: ??? not filled

        @SerializedName("telephony_network_sim_operator")
        public String telephonyNetworkSimOperator; // todo: ??? not filled

        @SerializedName("telephony_network_sim_country")
        public String telephonyNetworkSimCountry; //todo: ??? not filled

        @SerializedName("telephony_phone_type")
        public String telephonyPhoneType; //todo: ??? not filled

        @SerializedName("telephony_data_state")
        public String telephonyDataState; //todo: ??? not filled


        /**
         * END MOBILE NET ONLY
         **/


}
