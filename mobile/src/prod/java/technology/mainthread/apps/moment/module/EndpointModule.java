package technology.mainthread.apps.moment.module;

import android.content.Context;
import android.content.res.Resources;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;

import dagger.Module;
import dagger.Provides;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.service.moment.configApi.ConfigApi;
import technology.mainthread.service.moment.friendApi.FriendApi;
import technology.mainthread.service.moment.momentApi.MomentApi;
import technology.mainthread.service.moment.userApi.UserApi;

@Module
public class EndpointModule {

    private final Context context;
    private final Resources resources;
    private final String appName;

    public EndpointModule(Context context, Resources resources) {
        this.context = context;
        this.resources = resources;
        this.appName = resources.getString(R.string.app_name);
    }

    @Provides
    ConfigApi provideConfigApi(HttpTransport httpTransport) {
        return new ConfigApi.Builder(httpTransport,
                new AndroidJsonFactory(), null)
                .setApplicationName(appName).build();
    }

    @Provides
    UserApi.Builder provideUserApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        return new UserApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()))
                .setApplicationName(appName);
    }

    @Provides
    FriendApi.Builder provideFriendApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        return new FriendApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()))
                .setApplicationName(appName);
    }

    @Provides
    MomentApi.Builder provideMomentApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        return new MomentApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()))
                .setApplicationName(appName);
    }

    private GoogleAccountCredential credential(String accountName) {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingAudience(context,
                        "server:client_id:" + resources.getString(R.string.web_client_id));
        credential.setSelectedAccountName(accountName);
        return credential;
    }

}
