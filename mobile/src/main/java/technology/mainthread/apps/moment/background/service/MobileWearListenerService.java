package technology.mainthread.apps.moment.background.service;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.inject.Inject;

import okio.BufferedSource;
import okio.Okio;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.WearApi;
import technology.mainthread.apps.moment.data.api.FriendsSync;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static technology.mainthread.apps.moment.common.Constants.CONNECTION_TIME_OUT_MS;

public class MobileWearListenerService extends WearableListenerService {

    @Inject
    @WearApi
    GoogleApiClient mGoogleApiClient;
    @Inject
    FriendsSync friendsSync;

    @Override
    public void onCreate() {
        super.onCreate();
        MomentApp.get(this).inject(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Constants.PATH_WEAR_ERROR)) {
            logWearException(messageEvent);
        }
    }

    private void logWearException(MessageEvent messageEvent) {
        Timber.d("Uncaught wear exception");
        if (!mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, MILLISECONDS).isSuccess()) {
            return;
        }

        DataMap map = DataMap.fromByteArray(messageEvent.getData());

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(map.getByteArray(Constants.KEY_WEAR_EXCEPTION));
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            Throwable ex = (Throwable) ois.readObject();

//            Crashlytics.setBool("wear_exception", true);
//            Crashlytics.setString("board", map.getString("board"));
//            Crashlytics.setString("fingerprint", map.getString("fingerprint"));
//            Crashlytics.setString("model", map.getString("model"));
//            Crashlytics.setString("manufacturer", map.getString("manufacturer"));
//            Crashlytics.setString("product", map.getString("product"));
//            Crashlytics.logException(ex);

        } catch (IOException | ClassNotFoundException e) {
            Timber.e(e, "track exception failed");
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                Timber.d("path == %s", path);

                if (path.equals(Constants.PATH_NEW_MOMENT)) {
                    sendMoment(event);
                } else if (path.equals(Constants.PATH_FRIENDS_REFRESH_REQUEST)) {
                    try {
                        friendsSync.syncFriends();
                    } catch (IOException e) {
                        Timber.e(e, "sync friends failed");
                    }
                }
            }
        }
    }

    private void sendMoment(DataEvent event) {
        Timber.d("sending moment");

        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
        long[] recipient = {dataMapItem.getDataMap().getLong(Constants.KEY_RECIPIENT)};

        Asset asset = dataMapItem.getDataMap().getAsset(Constants.KEY_DRAWING);
        byte[] assetBytes = loadBytesFromAsset(asset);

        if (recipient.length > 0 && assetBytes != null && assetBytes.length > 0) {
            Timber.d("start sender service");
            startService(SenderService.getSenderServiceSendIntent(this, recipient, assetBytes));
        }
    }

    private byte[] loadBytesFromAsset(Asset asset) {
        if (asset == null) {
            Timber.e("Asset is null");
            return null;
        }

        byte[] output = null;
        if (mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, MILLISECONDS).isSuccess()) {

            InputStream assetInputStream = Wearable.DataApi
                    .getFdForAsset(mGoogleApiClient, asset).await().getInputStream();

            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }

            BufferedSource buffer = Okio.buffer(Okio.source(assetInputStream));
            try {
                output = buffer.readByteArray();
            } catch (IOException e) {
                Timber.e(e, "converting to bytes error");
            }
        }
        return output;
    }
}
