package technology.mainthread.apps.moment;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.analytics.Tracker;
import com.google.api.client.http.HttpTransport;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import technology.mainthread.apps.moment.data.StethoUtil;
import technology.mainthread.apps.moment.data.bus.RxBus;
import technology.mainthread.apps.moment.data.okhttp.OkHttpTransport;

@Module
public class MomentAppModule {

    private static final String PREFERENCES_NAME = "moment";
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 10 MB

    private final MomentApp application;

    public MomentAppModule(MomentApp application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    Resources provideApplicationResources() {
        return application.getResources();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    AccountManager provideAccountManager() {
        return (AccountManager) application.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Provides
    @Singleton
    NotificationManagerCompat provideNotificationManager() {
        return NotificationManagerCompat.from(application);
    }

    @Provides
    @Singleton
    ConnectivityManager provideConnectivityManager() {
        return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    OkHttpClient okHttpClient() {
        final OkHttpClient client = new OkHttpClient();

        // set disk cache
        client.setCache(new Cache(application.getCacheDir(), DISK_CACHE_SIZE));

        // stetho interceptor - only on debug
        StethoUtil.addStethoInterceptor(client);

        return client;
    }

    @Provides
    @Singleton
    HttpTransport httpTransport(OkHttpClient okHttpClient) {
        return new OkHttpTransport.Builder().setOkHttpClient(okHttpClient).build();
    }

    @Provides
    @Singleton
    Picasso picasso(OkHttpClient okHttpClient) {
        return new Picasso.Builder(application).downloader(new OkHttpDownloader(okHttpClient)).build();
    }

    @Provides
    @Singleton
    RxBus bus() {
        return new RxBus();
    }

    @Provides
    Tracker provideTracker() {
        return application.getTracker();
    }
}
