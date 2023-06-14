package at.specure.android.configs;

import android.content.Context;


/**
 * Set is handled by android system itself
 */
public class ThreadsConfig {

    public static final int THREAD_CONFIG__THREAD_NUMBER_SERVER_DEFAULT = 0;

    public static int getThreadNumber(final Context context) {
        Integer threadNumber = THREAD_CONFIG__THREAD_NUMBER_SERVER_DEFAULT;
        if (context != null) {
            threadNumber = PreferenceConfig.getPreferenceSharedPreferences(context).getInt("thread_number_override_value", THREAD_CONFIG__THREAD_NUMBER_SERVER_DEFAULT);
        }
        return threadNumber;
    }
}
