package technology.mainthread.apps.moment.background.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentWearApp;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.ui.notification.Notifier;
import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling wear events.
 */
public class WearEventsIntentService extends IntentService {

    private static final String ACTION_NEW_MOMENT = "ACTION_NEW_MOMENT";

    @Inject
    Notifier notifier;
    @Inject
    GoogleApiClient googleApiClient;

    public static void showMomentNotification(Context context, long momentId, long senderId,
                                              String senderName, Asset asset) {
        Intent intent = new Intent(context, WearEventsIntentService.class);
        intent.setAction(ACTION_NEW_MOMENT);
        intent.putExtra(Constants.KEY_MOMENT_ID, momentId);
        intent.putExtra(Constants.KEY_SENDER_ID, senderId);
        intent.putExtra(Constants.KEY_SENDER_NAME, senderName);
        intent.putExtra(Constants.KEY_DRAWING, asset);
        context.startService(intent);
    }

    public WearEventsIntentService() {
        super(WearEventsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MomentWearApp.get(this).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NEW_MOMENT.equals(action)) {
                handleShowMomentNotification(intent);
            }
        }
    }

    /**
     * Display the new moment notification with drawing and reply button.
     */
    private void handleShowMomentNotification(final Intent intent) {
        long momentId = intent.getLongExtra(Constants.KEY_MOMENT_ID, 0L);
        long senderId = intent.getLongExtra(Constants.KEY_SENDER_ID, 0L);
        String senderName = intent.getStringExtra(Constants.KEY_SENDER_NAME);
        Asset asset = intent.getParcelableExtra(Constants.KEY_DRAWING);

        if (asset == null) {
            return;
        }

        ConnectionResult result = googleApiClient
                .blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        if (result.isSuccess()) {

            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset)
                    .await().getInputStream();

            googleApiClient.disconnect();

            if (assetInputStream != null) {
                Bitmap drawing = BitmapFactory.decodeStream(assetInputStream);
                notifier.showNewMoment(momentId, senderId, senderName, drawing);
            } else {
                Timber.w("Requested an unknown Asset.");
            }
        }
    }

}
