package technology.mainthread.apps.moment.data.wear;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.WearApi;
import timber.log.Timber;

public class WearableFinder {

    private final GoogleApiClient mGoogleApiClient;

    @Inject
    public WearableFinder(@WearApi GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    public Observable<List<Node>> getWearableNodes() {
        return Observable.create(new Observable.OnSubscribe<List<Node>>() {
            @Override
            public void call(final Subscriber<? super List<Node>> subscriber) {
                ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                if (connectionResult.isSuccess()) {
                    List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
                    Timber.d("has %d connected nodes", nodes.size());
                    subscriber.onNext(nodes);
                } else {
                    String message = "Cannot connect to wear api, connectionResult == %s";
                    Timber.w(message, connectionResult);
                    subscriber.onError(new Exception(String.format(message, connectionResult)));
                }

                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }

                subscriber.onCompleted();
            }

        }).subscribeOn(Schedulers.io());
    }

}
