package at.specure.android.configs;

import android.content.Context;
import android.content.SharedPreferences;

import com.specure.opennettest.R;

public class StartConfig {

    public static final String TUTORIAL_DISPLAYED = "tutorial_displayed";

    public static final int TUTORIAL_BRIEF = 0;
    public static final int TUTORIAL_ADVANCED = 1;

    public static boolean shouldDisplayStartTutorial(Context context) {
        boolean should = false;
        return should;
    }

    public static int getTutorialType(Context context) {
        return 0;
    }

    public static void setTutorialDisplayed(Context context) {
        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(TUTORIAL_DISPLAYED, true);
        edit.apply();
    }

}
