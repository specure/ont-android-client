package at.specure.androidX.logging.timber;

import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;

import timber.log.Timber;

public class LoggingTree extends Timber.DebugTree {

    @Override
    protected void log(final int priority, final String tag, final String message, final Throwable throwable) {
        super.log(priority, tag, message, throwable);
        switch (priority) {
            case Log.ERROR:
                Log.e(tag, message, throwable);
                HyperLog.e(tag, message, throwable);
                break;
            case Log.DEBUG:
                Log.d(tag, message, throwable);
                HyperLog.d(tag, message, throwable);
                break;
            case Log.WARN:
                Log.w(tag, message, throwable);
                HyperLog.w(tag, message, throwable);
                break;
            case Log.INFO:
                Log.i(tag, message, throwable);
                HyperLog.i(tag, message, throwable);
                break;
            case Log.ASSERT:
                HyperLog.a(message);
                break;
            case Log.VERBOSE:
                Log.v(tag, message, throwable);
                HyperLog.v(tag, message, throwable);
                break;
            default:
                Log.e(tag, message, throwable);
                HyperLog.e(tag, message, throwable);
        } {

        }
    }
}