package at.specure.androidX.logging.timber;

import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import timber.log.Timber;

public class LoggingTree extends Timber.DebugTree {

    @Override
    protected void log(final int priority, final String tag, final String message, final Throwable throwable) {
        switch (priority) {
            case Log.ERROR:
                HyperLog.e(tag, message, throwable);
                break;
            case Log.DEBUG:
                HyperLog.d(tag, message, throwable);
                break;
            case Log.WARN:
                HyperLog.w(tag, message, throwable);
                break;
            case Log.INFO:
                HyperLog.i(tag, message, throwable);
                break;
            case Log.ASSERT:
                HyperLog.a(message);
                break;
            case Log.VERBOSE:
                HyperLog.v(tag, message, throwable);
                break;
            default:
                HyperLog.e(tag, message, throwable);
        }
    }
}