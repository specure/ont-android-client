package at.specure.androidX.data.badges;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import at.specure.android.configs.LocaleConfig;

public class BadgesRequest {

    @SerializedName("language")
    String language;

    public BadgesRequest(Context context) {
        try {
            this.language = LocaleConfig.getLocaleForServerRequest(context);
        } catch (Exception ignored) {
            this.language = "en";
        }
    }
}
