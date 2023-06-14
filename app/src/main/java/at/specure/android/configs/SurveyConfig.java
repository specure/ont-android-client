package at.specure.android.configs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.specure.opennettest.R;

import at.specure.android.api.calls.CheckSurveyTask;
import at.specure.android.api.reqres.check_survey.CheckSurveyRsp;
import timber.log.Timber;

/**
 * Class to handle preserved settings for surveys
 * Created by michal.cadrik on 2/1/2018.
 */

public class SurveyConfig {

    private static final String SURVEY_ANSWERED_TIMESTAMP = "survey_answered_timestamp";
    private static final String SURVEY_STARTED_TIMESTAMP = "survey_started_timestamp";
    private static final String SURVEY_URL = "survey_url";


    private static void surveyAnsweredOrIgnored(Context context) {
        if (context != null) {
            SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SURVEY_ANSWERED_TIMESTAMP, System.currentTimeMillis());
            editor.apply();
        }

    }

    public static void saveCurrentSurveySettings(Context context, String url, long timestampStart) {
        if (context != null) {
            SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(SURVEY_STARTED_TIMESTAMP, timestampStart);
            editor.putString(SURVEY_URL, url);
            editor.apply();
        }
    }

    private static String getSurveyUrl(Context context) {
        if (context != null) {
            String uuid = ConfigHelper.getUUID(context);
            SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
            String surveyUrl = sharedPreferences.getString(SURVEY_URL, null);
            if ((surveyUrl != null) && !(surveyUrl.isEmpty()) && (uuid != null) && !(uuid.isEmpty())) {
//                Uri parse = Uri.parse(surveyUrl);
//                Uri.Builder builder = new Uri.Builder();
//                Uri surveyUri = builder.path(surveyUrl).appendQueryParameter("client_uuid", uuid).build();
//                Timber.e("SurveyURI:", surveyUri + "");
                surveyUrl += "?client_uuid=" + uuid;
                return surveyUrl;
            }
        }
        return null;
    }

    public static boolean isSurveyActive(Context context) {
        if (context != null) {
            return getSurveyUrl(context) != null;
        } else {
            return false;
        }

    }

    public static void openSurveyPage(Activity activity) {
        if (activity != null) {
            String surveyUrl = getSurveyUrl(activity);
            if (surveyUrl != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(surveyUrl);
                Timber.e("SurveyURI: %s", uri);
                i.setData(uri);
                activity.startActivity(i);
            }
        }
    }

    public static void showSurveyDialog(final Activity activity) {

        if (getSurveyUrl(activity) != null)
            if (!isSurveyAnswered(activity)) {

                CheckSurveyTask checkSurveyTask = new CheckSurveyTask(activity);
                checkSurveyTask.setOnCompleteListener(new CheckSurveyTask.CheckSurveyEndTaskListener() {
                    @Override
                    public void onSurveyCheckEnded(CheckSurveyRsp checkSurveyRsp) {
                        if (checkSurveyRsp != null) {
                            if (checkSurveyRsp.filledUp == null || checkSurveyRsp.filledUp) {
                                surveyAnsweredOrIgnored(activity);
                            } else {
                                AlertDialog alert = new AlertDialog.Builder(activity).
                                        setPositiveButton(R.string.review_positive_button, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
//                                                Uri surveyUrl = getSurveyUrl(activity);
                                                openSurveyPage(activity);
                                                surveyAnsweredOrIgnored(activity);
                                            }
                                        }).
                                        setNegativeButton(R.string.review_negative_button, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                surveyAnsweredOrIgnored(activity);
                                            }
                                        }).
                                        setNeutralButton(R.string.review_neutral_button, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).
                                        setMessage(R.string.survey_take_part_message).
                                        setCancelable(false).
                                        create();

                                alert.show();
                            }
                        }
                    }
                });
                checkSurveyTask.execute(ConfigHelper.getUUID(activity));


            }
    }

    private static boolean isSurveyAnswered(Context context) {
        if (context != null) {
            SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
            long timestampOfNewSurvey = sharedPreferences.getLong(SURVEY_STARTED_TIMESTAMP, 0);
            long answerTimestamp = sharedPreferences.getLong(SURVEY_ANSWERED_TIMESTAMP, 0);
            return timestampOfNewSurvey <= answerTimestamp;
        }
        return true;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean isSurveyEnabledInApp(Context context) {
        if (context != null) {
            boolean surveyEnabled = context.getResources().getBoolean(R.bool.survey_enabled);
            return surveyEnabled;
        } else {
            return false;
        }
    }
}
