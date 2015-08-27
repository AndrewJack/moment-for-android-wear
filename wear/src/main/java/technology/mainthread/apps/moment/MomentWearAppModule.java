package technology.mainthread.apps.moment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MomentWearAppModule {

    private static final String PREFERENCES_NAME = "moment";

    private final MomentWearApp application;

    public MomentWearAppModule(MomentWearApp application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return application.getResources();
    }

    @Provides
    @Singleton
    NotificationManagerCompat provideNotificationManager() {
        return NotificationManagerCompat.from(application);
    }

    @Provides
    @Singleton
    GoogleApiClient providesWearApiClient() {
        return new GoogleApiClient.Builder(application)
                .addApi(Wearable.API)
                .build();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    SensorManager provideSensorManager() {
        return (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
    }

}
