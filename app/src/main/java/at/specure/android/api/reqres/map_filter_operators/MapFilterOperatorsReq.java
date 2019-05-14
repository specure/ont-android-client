package at.specure.android.api.reqres.map_filter_operators;

import com.google.gson.annotations.SerializedName;

public class MapFilterOperatorsReq {

    @SerializedName("language")
    String languageCodeISO2;

    @SerializedName("country_code")
    String countryCodeISO2;

    /**
     * Use constants from #MapFilterType class
     */
    @SerializedName("provider_type")
    String providerType;

    public MapFilterOperatorsReq(String languageCodeISO2, String countryCodeISO2, String providerType) {
        this.languageCodeISO2 = languageCodeISO2;
        this.countryCodeISO2 = countryCodeISO2;
        this.providerType = providerType;
    }
}
