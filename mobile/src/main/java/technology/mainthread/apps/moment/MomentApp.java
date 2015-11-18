package technology.mainthread.apps.moment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import technology.mainthread.apps.moment.data.CrashlyticsTree;
import javax.inject.Inject;

import technology.mainthread.apps.moment.data.StethoUtil;
import timber.log.Timber;

public class MomentApp extends Application {

    public enum TrackerName {
        APP_TRACKER
    }

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    private MomentComponent component;
    private RefWatcher refWatcher;

    @Inject
    GoogleApiAvailability googleApiAvailability;

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
        component = MomentComponent.Initializer.init(this);
        component.inject(this);

        setupAnalyticsTools();
        StethoUtil.setupStetho(this); // enabled on debug variants

        checkApiAvailability();
    }

    private void setupAnalyticsTools() {
        boolean isProd = !BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("prod");

        // setup fabric
        Crashlytics crashlytics = new Crashlytics.Builder().disabled(!isProd).build();
        final Fabric fabric = new Fabric.Builder(this)
                .kits(crashlytics)
                .build();
        Fabric.with(fabric);

        // set up timber
        Timber.plant(isProd ? new CrashlyticsTree() : new Timber.DebugTree());

        // set up analytics
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        analytics.setAppOptOut(!userPrefs.getBoolean(getString(R.string.key_analytics), true));
        analytics.getLogger().setLogLevel(Logger.LogLevel.ERROR);
        analytics.setDryRun(!isProd);

        userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.key_analytics))) {
                    GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(!sharedPreferences.getBoolean(key, true));
                }
            }
        });
    }

    private void checkApiAvailability() {
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.showErrorNotification(this, status);
        }
    }

    public synchronized Tracker getTracker() {
        if (!mTrackers.containsKey(TrackerName.APP_TRACKER)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // setup app tracker
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            t.enableAdvertisingIdCollection(true);

            mTrackers.put(TrackerName.APP_TRACKER, t);
        }
        return mTrackers.get(TrackerName.APP_TRACKER);
    }

    public static MomentComponent get(Context context) {
        return ((MomentApp) context.getApplicationContext()).component;
    }

    public static RefWatcher getRefWatcher(Context context) {
        MomentApp application = (MomentApp) context.getApplicationContext();
        return application.refWatcher;
    }

}
