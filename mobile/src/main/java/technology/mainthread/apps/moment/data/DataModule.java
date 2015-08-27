package technology.mainthread.apps.moment.data;

import android.content.Context;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.wearable.Wearable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

    private final Context context;

    public DataModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    GoogleCloudMessaging provideGoogleCloudMessaging() {
        return GoogleCloudMessaging.getInstance(context);
    }

    @Provides
    @Singleton
    @GooglePlusApi
    GoogleApiClient provideGooglePlusLoginGoogleApiClient() {
        return new GoogleApiClient.Builder(context)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Provides
    @Singleton
    @WearApi
    GoogleApiClient provideWearApiClient() {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    @Provides
    @Singleton
    GoogleApiAvailability provideGoogleApiAvailability() {
        return GoogleApiAvailability.getInstance();
    }

}
