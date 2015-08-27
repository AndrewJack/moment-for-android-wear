package technology.mainthread.apps.moment.background.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.background.receiver.GcmBroadcastReceiver;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.UserManager;
import technology.mainthread.apps.moment.data.WearApi;
import technology.mainthread.apps.moment.ui.notification.Notifier;
import technology.mainthread.service.moment.momentApi.MomentApi;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;
import timber.log.Timber;

import static com.google.android.gms.gcm.GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE;
import static technology.mainthread.apps.moment.util.DataMapCreatorUtil.createFriendAddedBackDataMap;
import static technology.mainthread.apps.moment.util.DataMapCreatorUtil.createNewMomentDataMap;

public class GcmListenerService extends IntentService {

    @Inject
    GoogleCloudMessaging gcm;
    @Inject
    Notifier notifier;
    @Inject
    @WearApi
    GoogleApiClient mGoogleApiClient;
    @Inject
    MomentApi.Builder momentApi;
    @Inject
    Picasso picasso;
    @Inject
    UserManager userManager;

    private Intent intent;

    public GcmListenerService() {
        super(GcmListenerService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MomentApp.get(this).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.intent = intent;
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (userManager.isSignedIn() && MESSAGE_TYPE_MESSAGE.equals(messageType)
                && extras != null && !extras.isEmpty()) {

            if (extras.containsKey(Constants.GCM_KEY_MOMENT)) {
                getMomentAndSendToWear(extras.getString(Constants.GCM_KEY_MOMENT));
            } else if (extras.containsKey(Constants.GCM_KEY_FRIEND_ID)
                    && extras.containsKey(Constants.GCM_KEY_FRIEND_NAME)
                    && extras.containsKey(Constants.GCM_KEY_IS_FRIEND)) {
                try {
                    long friendId = Long.parseLong(extras.getString(Constants.GCM_KEY_FRIEND_ID));
                    String name = extras.getString(Constants.GCM_KEY_FRIEND_NAME);
                    boolean isFriend = Boolean.parseBoolean(extras.getString(Constants.GCM_KEY_IS_FRIEND));

                    Timber.d("friend added, friendId: %1$d, name: %2$s, isFriend: %3$s", friendId, name, isFriend);
                    if (isFriend) {
                        showFriendAddedBackNotification(friendId, name);
                    } else {
                        notifier.showFriendAddNotification(friendId, name);
                    }

                    // sync friends
                    userManager.requestSync();
                } catch (Exception e) {
                    Timber.w(e, "cannot show new friend notification");
                }
            }
        } else {
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void getMomentAndSendToWear(String momentId) {
        try {
            MomentResponse moment = momentApi.build().moments().get(Long.parseLong(momentId)).execute();
            Timber.d("moment id: %d", moment.getMomentId());

            // This code is for local development
            //Uri uri = Uri.parse(response.getServingUrl());
            //picasso.load("http://<LOCAL-IP>:8888" + uri.getPath()).get();

            // This code is for dev & prod
            Bitmap drawing = picasso.load(moment.getServingUrl()).get();

            if (drawing != null) {
                showNewMomentNotification(moment, drawing);
            }

        } catch (IOException e) {
            Timber.w(e, "getting url failed");
        } finally {
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void showNewMomentNotification(MomentResponse moment, Bitmap drawing) {
        if (!sendDataItemToWear(createNewMomentDataMap(moment, drawing))) {
            notifier.showMobileOnlyMomentNotification(moment);
        }
    }

    private void showFriendAddedBackNotification(long friendId, String name) {
        if (sendDataItemToWear(createFriendAddedBackDataMap(friendId, name))) {
            notifier.cancelNotification(Notifier.ID_ADD);
        } else {
            notifier.showFriendAddedBackNotification(name);
        }
    }

    private boolean sendDataItemToWear(PutDataMapRequest request) {
        boolean success = false;
        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        if (connectionResult.isSuccess() && hasConnectedNodes()) {
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request.asPutDataRequest());

            DataApi.DataItemResult result = pendingResult.await();

            Timber.d("isSuccess() == %s", result.getStatus().isSuccess());
            success = result.getStatus().isSuccess();
        } else {
            Timber.d("Could not connect to wear");
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        return success;
    }

    private boolean hasConnectedNodes() {
        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
        Timber.d("has %d connected nodes", nodes.size());
        return !nodes.isEmpty();
    }

}
