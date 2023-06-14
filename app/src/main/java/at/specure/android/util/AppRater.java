package at.specure.android.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;

/**
 * Created by michal.cadrik on 1/25/2018.
 */

public class AppRater {
//    private final static int DAYS_UNTIL_PROMPT = 7;//Min number of days
//    private final static int LAUNCHES_UNTIL_PROMPT = 1;//Min number of tests

    public static void testPerformed(MainActivity activity) {

        boolean shouldBeAppReviewed = ConfigHelper.shouldBeAppReviewed(activity);
        int measurementsUntilPrompt = ConfigHelper.measurementCountToDisplayReviewDialog(activity);
        int daysUntilPrompt = ConfigHelper.daysCountToDisplayReviewDialog(activity);

        if (shouldBeAppReviewed) {
            SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
            if (prefs.getBoolean("dontshowagain", false)) {
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();

            // Increment launch counter
            long launch_count = prefs.getLong("test_count", 0) + 1;
            editor.putLong("test_count", launch_count);

            // Get date of first launch
            Long date_firstLaunch = prefs.getLong("date_firstTest", 0);
        /*if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstTest", date_firstLaunch);
        }*/

            // Wait at least n days before opening
            if (launch_count >= measurementsUntilPrompt) {
                if (System.currentTimeMillis() - date_firstLaunch >= (daysUntilPrompt * 24 * 60 * 60 * 1000)) {
                    showRateDialog(activity, editor);
                }
            }

            editor.apply();
        }
    }

    private static void showRateDialog(final MainActivity activity, final SharedPreferences.Editor editor) {

        String appTitle = activity.getString(R.string.app_name);

        /*new AppRatingDialog.Builder()
                .setPositiveButtonText(activity.getString(R.string.review_positive_button))
                .setNegativeButtonText(activity.getString(R.string.review_negative_button))
                .setNeutralButtonText(activity.getString(R.string.review_neutral_button))
//                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(5)
//                .setTitle("Rate this application")
                .setDescription(activity.getString(R.string.review_description, appTitle))
//                .setDefaultComment("This app is pretty cool !")
//                .setStarColor(R.color.starColor)
//                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
//                .setTitleTextColor(R.color.titleTextColor)
//                .setDescriptionTextColor(R.color.contentTextColor)
//                .setHint("Please write your comment here ...")
//                .setHintTextColor(R.color.hintTextColor)
//                .setCommentTextColor(R.color.commentTextColor)
//                .setCommentBackgroundColor(R.color.colorPrimaryDark)
//                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setDefaultComment(null)
                .create(activity)
                .show();
*/

        final Dialog dialog = new Dialog(activity);
        dialog.setTitle("Rate " + appTitle);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams layoutParams = ll.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        ll.setLayoutParams(layoutParams);

        TextView tv = new TextView(activity);
        tv.setGravity(Gravity.CENTER);
        tv.setText(activity.getString(R.string.review_description, appTitle));
        ViewGroup.LayoutParams layoutParams1 = tv.getLayoutParams();
        if (layoutParams1 == null) {
            layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setLayoutParams(layoutParams1);
        tv.setPadding(16, 16, 16, 16);
        ll.addView(tv);



        Button b1 = new Button(activity);
        b1.setText(activity.getString(R.string.review_positive_button));
        b1.setBackgroundResource(R.drawable.button_selector);
        b1.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPositiveButtonClicked(activity, 0, "");
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(activity);
        b2.setBackgroundResource(R.drawable.button_selector);
        b2.setText(activity.getString(R.string.review_neutral_button));
        b2.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onNeutralButtonClicked(activity);
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(activity);
        b3.setText(activity.getString(R.string.review_negative_button));
        b3.setBackgroundResource(R.drawable.button_selector);
        b3.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onNegativeButtonClicked(activity);
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }

    public static void onPositiveButtonClicked(Activity activity, int starsCount, String reviewText) {
        String appPackageName = activity.getApplicationContext().getPackageName();
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException ignored) {
        }

        editor.putBoolean("dontshowagain", true);
        editor.apply();

    }

    public static void onNegativeButtonClicked(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("dontshowagain", true);
        editor.apply();
    }

    public static void onNeutralButtonClicked(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("date_firstTest", System.currentTimeMillis());
        editor.putLong("test_count", 0);
        editor.apply();
    }
}
