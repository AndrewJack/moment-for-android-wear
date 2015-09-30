package technology.mainthread.apps.moment.background;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.prefs.WearMomentPreferences;
import timber.log.Timber;

public class WearToMobileSender {

    private final GoogleApiClient mGoogleApiClient;
    private final WearMomentPreferences preferences;

    @Inject
    public WearToMobileSender(GoogleApiClient mGoogleApiClient, WearMomentPreferences preferences) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.preferences = preferences;
    }

    public rx.Observable<Void> sendDrawing(final Long recipient, final byte[] drawing) {
        return rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                if (drawing != null) {
                    Timber.d("Making asset");
                    Asset asset = Asset.createFromBytes(drawing);

                    PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PATH_NEW_MOMENT);
                    dataMap.getDataMap().putLong(Constants.KEY_RECIPIENT, recipient);
                    dataMap.getDataMap().putAsset(Constants.KEY_DRAWING, asset);

                    ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(
                            Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                    if (connectionResult.isSuccess()) { // TODO: && hasConnectedNodes() ??
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                .putDataItem(mGoogleApiClient, dataMap.asPutDataRequest());

                        DataApi.DataItemResult result = pendingResult.await();

                        Timber.d("result: %s", result.getStatus().isSuccess());
                        if (result.getStatus().isSuccess()) {
                            subscriber.onNext(null);
                        } else {
                            subscriber.onError(null);
                        }
                    } else {
                        Timber.e("Cannot connect to wear api or mobile node");
                        subscriber.onError(null);
                    }

                    if (mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }
                } else {
                    Timber.w("Drawing is null");
                    subscriber.onError(null);
                }
                subscriber.onCompleted();
            }

        }).subscribeOn(Schedulers.io());
    }

    public rx.Observable<Void> requestFriendsSync() { // TODO: should this use the message api?
        return rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Timber.d("requestFriendsSync");

                if (preferences.shouldRequestRefresh()) {
                    PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.PATH_FRIENDS_REFRESH_REQUEST);
                    dataMap.getDataMap().putLong(Constants.KEY_FORCE_UPDATE, System.currentTimeMillis());

                    ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(
                            Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

                    if (connectionResult.isSuccess()) {
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                .putDataItem(mGoogleApiClient, dataMap.asPutDataRequest());

                        DataApi.DataItemResult result = pendingResult.await();

                        Timber.d("result: %s", result.getStatus().isSuccess());
                        if (result.getStatus().isSuccess()) {
                            subscriber.onNext(null);
                        } else {
                            subscriber.onError(null);
                        }
                    } else {
                        Timber.e("Cannot connect to wear api or mobile node");
                        subscriber.onError(null);
                    }

                    if (mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }
                }
                subscriber.onCompleted();
            }

        }).subscribeOn(Schedulers.io());
    }
}
