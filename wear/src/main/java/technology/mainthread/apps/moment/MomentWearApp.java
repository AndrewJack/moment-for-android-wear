package technology.mainthread.apps.moment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import technology.mainthread.apps.moment.background.service.ErrorService;
import timber.log.Timber;

public class MomentWearApp extends Application {

    // Android Wear's default UncaughtExceptionHandler
    private Thread.UncaughtExceptionHandler mDefaultUEH;

    private Thread.UncaughtExceptionHandler mWearUEH = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(final Thread thread, final Throwable ex) {

            // Pass the exception to a Service which will send the data upstream to your Smartphone/Tablet
            Intent errorIntent = new Intent(MomentWearApp.this, ErrorService.class);
            errorIntent.putExtra("exception", ex);
            startService(errorIntent);

            // Let the default UncaughtExceptionHandler take it from here
            mDefaultUEH.uncaughtException(thread, ex);
        }
    };

    private WearComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = WearComponent.Initializer.init(this);

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mWearUEH);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static WearComponent get(Context context) {
        return ((MomentWearApp) context.getApplicationContext()).component;
    }
}
