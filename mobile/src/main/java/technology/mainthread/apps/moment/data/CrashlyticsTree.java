package technology.mainthread.apps.moment.data;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

public class CrashlyticsTree extends Timber.Tree {

    @Override
    public void e(String message, Object... args) {
        Crashlytics.log(String.format(message, args));
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        Crashlytics.log(String.format(message, args));
        Crashlytics.logException(t);
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
    }

}
