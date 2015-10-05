package technology.mainthread.apps.moment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

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

    @Inject
    GoogleApiAvailability googleApiAvailability;

    @Override
    public void onCreate() {
        super.onCreate();
        component = WearComponent.Initializer.init(this);
        component.inject(this);

        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mWearUEH);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        checkApiAvailability();
    }

    private void checkApiAvailability() {
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.showErrorNotification(this, status);
        }
    }

    public static WearComponent get(Context context) {
        return ((MomentWearApp) context.getApplicationContext()).component;
    }
}
