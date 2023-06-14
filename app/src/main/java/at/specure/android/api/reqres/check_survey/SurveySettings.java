package at.specure.android.api.reqres.check_survey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by michal.cadrik on 2/1/2018.
 */

public class SurveySettings {

    @SerializedName("survey_url")
    public String surveyUrl;

    @SerializedName("is_active_service")
    public Boolean isActive;

    @SerializedName("date_started")
    public Long timestampStarted;
}
