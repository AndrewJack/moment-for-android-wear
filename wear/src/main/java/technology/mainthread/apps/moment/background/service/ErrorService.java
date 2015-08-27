package technology.mainthread.apps.moment.background.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentWearApp;
import technology.mainthread.apps.moment.common.Constants;
import timber.log.Timber;

public class ErrorService extends IntentService {

    @Inject
    GoogleApiClient mGoogleAppClient;

    public ErrorService() {
        super(ErrorService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MomentWearApp.get(this).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleAppClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        List<String> nodes = getNodes(mGoogleAppClient);
        if (nodes.isEmpty()) {
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(intent.getSerializableExtra("exception"));

            byte[] exceptionData = bos.toByteArray();
            DataMap dataMap = new DataMap();

            dataMap.putString("board", Build.BOARD);
            dataMap.putString("fingerprint", Build.FINGERPRINT);
            dataMap.putString("model", Build.MODEL);
            dataMap.putString("manufacturer", Build.MANUFACTURER);
            dataMap.putString("product", Build.PRODUCT);
            dataMap.putByteArray(Constants.KEY_WEAR_EXCEPTION, exceptionData);

            Wearable.MessageApi.sendMessage(mGoogleAppClient, nodes.get(0), Constants.PATH_WEAR_ERROR, dataMap.toByteArray());

        } catch (Exception e) {
            Timber.w(e, "Failed sending error to phone");
        } finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException ignore) {
            }
            try {
                bos.close();
            } catch (IOException ignore) {
            }
        }
    }

    private List<String> getNodes(GoogleApiClient mGoogleApiClient) {
        ArrayList<String> results = new ArrayList<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

}