package technology.mainthread.apps.moment.data.api;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.GooglePlusApi;
import timber.log.Timber;

public class GooglePlusSignInRequester {

    private final GoogleApiClient googleApiClient;

    @Inject
    public GooglePlusSignInRequester(@GooglePlusApi GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public void logOut() {
        if (connect().isSuccess()) {
            Timber.d("clearing default account");
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
        }
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public void revoke() {
        if (connect().isSuccess()) {
            Timber.d("revoking access");
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
        }
    }

    private ConnectionResult connect() {
        Timber.d("Connecting");
        return googleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
    }
}
