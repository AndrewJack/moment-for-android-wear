package technology.mainthread.apps.moment.data.rx;

import android.graphics.Bitmap;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.WearApi;
import technology.mainthread.service.moment.momentApi.MomentApi;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;
import timber.log.Timber;

import static technology.mainthread.apps.moment.util.DataMapCreatorUtil.createNewMomentDataMap;

public class RxWearNotifier {

    private final Picasso picasso;
    private final MomentApi momentApi;
    private final GoogleApiClient googleApiClient;

    @Inject
    public RxWearNotifier(Picasso picasso,
                          MomentApi.Builder momentApiBuilder,
                          @WearApi GoogleApiClient googleApiClient) {
        this.picasso = picasso;
        this.momentApi = momentApiBuilder.build();
        this.googleApiClient = googleApiClient;
    }

    public Observable<Boolean> sendNotificationToWear(final long momentId) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(getMomentAndSendToWear(momentId));
                } catch (IOException e) {
                    Timber.w(e, "Sending notification to wear failed");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }

        });
    }

    private boolean getMomentAndSendToWear(final long momentId) throws IOException {
        MomentResponse moment = momentApi.moments().get(momentId).execute();
        Timber.d("moment id: %d", moment.getMomentId());

        // This code is for local development
        //Uri uri = Uri.parse(response.getServingUrl());
        //picasso.load("http://<LOCAL-IP>:8888" + uri.getPath()).get();

        // This code is for dev & prod
        Bitmap drawing = picasso.load(moment.getServingUrl()).get();

        boolean success = false;
        if (drawing != null) {
            success = showNewMomentNotification(moment, drawing);
        }

        return success;
    }

    private boolean showNewMomentNotification(MomentResponse moment, Bitmap drawing) {
        ConnectionResult connectionResult = googleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        boolean success = false;
        if (connectionResult.isSuccess() && hasConnectedNodes()) {
            PutDataMapRequest request = createNewMomentDataMap(moment, drawing);

            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(googleApiClient, request.asPutDataRequest());

            DataApi.DataItemResult result = pendingResult.await();

            Timber.d("isSuccess() == %s", result.getStatus().isSuccess());
            success = result.getStatus().isSuccess();
        } else {
            Timber.d("Could not connect to wear");
        }

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        return success;
    }

    private boolean hasConnectedNodes() {
        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
        Timber.d("has %d connected nodes", nodes.size());
        return !nodes.isEmpty();
    }
}
