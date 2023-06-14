package at.specure.android.configs;

import android.content.Context;
import androidx.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.specure.opennettest.R;

import java.io.IOException;
import java.io.InputStream;


@SuppressWarnings("WeakerAccess")
public class TermsAndConditionsConfig {

    public static final int TERMS_SOURCE_HYBRID = 0;
    public static final int TERMS_SOURCE_LOCAL = 1;
    public static final int TERMS_SOURCE_REMOTE = 2;

    public static final int PP_SOURCE_HYBRID = 0;
    public static final int PP_SOURCE_LOCAL = 1;
    public static final int PP_SOURCE_REMOTE = 2;


    public static void showTaC(WebView webview, final Context context) {
        int tac_source = getTermSourceType(context);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        switch (tac_source) {
            case TERMS_SOURCE_LOCAL:
                webview.loadData(getTACLocalString(context), "text/html; charset=utf-8", "utf-8");
                break;
            case TERMS_SOURCE_REMOTE:
                webview.loadUrl(context.getString(R.string.url_terms));
                break;
            case TERMS_SOURCE_HYBRID:
                if (CheckInternetAvailability.isInternetConnected(context)) {
                    webview.loadUrl(context.getString(R.string.url_terms));
                } else {
                    webview.loadData(getTACLocalString(context), "text/html; charset=utf-8", "utf-8");
                }
                break;
        }
    }

    public static void showPP(WebView webview, final Context context) {
        int tac_source = getPrivacyPolicySourceType(context);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        switch (tac_source) {
            case PP_SOURCE_LOCAL:
                webview.loadData(getPPLocalString(context), "text/html; charset=utf-8", "utf-8");
                break;
            case PP_SOURCE_REMOTE:
                webview.loadUrl(context.getString(R.string.url_pp));
                break;
            case PP_SOURCE_HYBRID:
                if (CheckInternetAvailability.isInternetConnected(context)) {
                    webview.loadUrl(context.getString(R.string.url_pp));
                } else {
                    webview.loadData(getPPLocalString(context), "text/html; charset=utf-8", "utf-8");
                }
                break;
        }
    }

    @NonNull
    private static String getTACLocalString(Context context) {
        String prompt = "";
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.tc);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            prompt = new String(buffer);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prompt;
    }

    @NonNull
    private static String getPPLocalString(Context context) {
        String prompt = "";
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.pp);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            prompt = new String(buffer);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prompt;
    }

    private static int getTermSourceType(Context context) {
        return context.getResources().getInteger(R.integer.terms_and_conditions_source);
    }

    private static int getPrivacyPolicySourceType(Context context) {
        return context.getResources().getInteger(R.integer.privacy_policy_source);
    }

    public static boolean shouldShowPrivacyPolicy(Context context) {
        return context.getResources().getBoolean(R.bool.show_privacy_policy_on_start);
    }

    public static boolean showPrivacyPolicyInAboutPage(Context context) {
        return context.getResources().getBoolean(R.bool.show_privacy_policy_in_information_screen);
    }
}
