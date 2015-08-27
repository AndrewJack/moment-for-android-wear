package technology.mainthread.apps.moment.module;

import android.content.Context;
import android.content.res.Resources;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;

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
    private final String rootUrl;

    public EndpointModule(Context context, Resources resources) {
        this.context = context;
        this.resources = resources;
        this.appName = resources.getString(R.string.app_name);
        this.rootUrl = resources.getString(R.string.local_root_url);
    }

    @Provides
    ConfigApi provideConfigApi(HttpTransport httpTransport) {
        ConfigApi.Builder builder = new ConfigApi.Builder(httpTransport,
                new AndroidJsonFactory(), null);
        setupLocalApi(builder);
        return builder.build();
    }

    @Provides
    UserApi.Builder provideUserApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        UserApi.Builder builder = new UserApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()));
        setupLocalApi(builder);
        return builder;
    }

    @Provides
    FriendApi.Builder provideFriendApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        FriendApi.Builder builder = new FriendApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()));
        setupLocalApi(builder);
        return builder;
    }

    @Provides
    MomentApi.Builder provideMomentApiBuilder(HttpTransport httpTransport, MomentPreferences preferences) {
        MomentApi.Builder builder = new MomentApi.Builder(httpTransport,
                new AndroidJsonFactory(), credential(preferences.getAccountName()));
        setupLocalApi(builder);
        return builder;
    }

    private GoogleAccountCredential credential(String accountName) {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingAudience(context,
                        "server:client_id:" + resources.getString(R.string.web_client_id));
        credential.setSelectedAccountName(accountName);
        return credential;
    }

    private void setupLocalApi(AbstractGoogleJsonClient.Builder builder) {
        builder.setApplicationName(appName)
                .setRootUrl(rootUrl)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                        request.setDisableGZipContent(true);
                    }
                });
    }

}
