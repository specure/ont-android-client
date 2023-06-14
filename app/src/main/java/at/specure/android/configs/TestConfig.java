package at.specure.android.configs;

import android.content.Context;

import timber.log.Timber;

/**
 * Class to handle preserved settings for test
 */
public class TestConfig {

    private static boolean showResults = false;
    private static String lastTestUuid = null;

    /**
     * This method is handy when user enter app while test was executed on the background and we are not sure if results were already shown to the user.
     * This method checks for loop mode running also, when we do not want to display result after the test
     *
     * @param appContext - applicationContext
     */
    public static boolean shouldShowResults(Context appContext) {
        if (appContext != null) {
            if (LoopModeConfig.isLoopMode(appContext)) {
                return false;
            } else {
                return showResults;
            }
        }
        return false;
    }

    /**
     * You must call this method when test ends with true and with false when results are loaded from the backend
     *
     * @param shouldShowResults true if results should be shown, false if results are already loaded and displayed in the screen
     */
    public static void setShouldShowResults(boolean shouldShowResults) {
        showResults = shouldShowResults;
        Timber.d("Should show results set to %b", shouldShowResults);
    }

    /**
     * For displaying result purposes, when user left the app and test uuid is discarded in the test service after the test
     * Step to reproduce: wait until test is done and then left the app and return back and then click on show results - before it was displaying old
     * results or not finish the loading of the results at all @see{getCurrentlyPerformingTestUUID} too
     * @param testUuid
     */
    public static void setCurrentlyPerformingTestUUID(String testUuid) {
        lastTestUuid = testUuid;
    }

    /**
     * @see {setCurrentlyPerformingTestUUID} for more info
     * @return last obtained test uuid on the test start
     */
    public static String getCurrentlyPerformingTestUUID() {
        return lastTestUuid;
    }
}