package technology.mainthread.apps.moment.background.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.data.WearApi;
import technology.mainthread.apps.moment.data.api.FriendsSync;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static technology.mainthread.apps.moment.common.Constants.CONNECTION_TIME_OUT_MS;

public class MomentWearListenerService extends WearableListenerService {

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

        } catch (Exception e) {
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

        Asset drawingAsset = dataMapItem.getDataMap().getAsset(Constants.KEY_DRAWING);
        Bitmap bitmap = loadBitmapFromAsset(drawingAsset);

        if (recipient.length > 0 && bitmap != null) {
            Timber.d("start sender service");
            startService(MomentSenderService.getMomentSenderServiceSendIntent(this, recipient, bitmap));
        }
    }

    private Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            Timber.e("Asset is null");
            return null;
        }

        Bitmap drawing = null;
        if (mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, MILLISECONDS).isSuccess()) {

            InputStream assetInputStream = Wearable.DataApi
                    .getFdForAsset(mGoogleApiClient, asset).await().getInputStream();

            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }

            if (assetInputStream != null) {
                drawing = BitmapFactory.decodeStream(assetInputStream);
            }
        }
        return drawing;
    }
}
