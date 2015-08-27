package technology.mainthread.apps.moment.data.wear;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.common.data.vo.Friend;
import technology.mainthread.apps.moment.data.WearApi;
import timber.log.Timber;

public class FriendWearSender {

    private final GoogleApiClient mGoogleApiClient;

    @Inject
    public FriendWearSender(@WearApi GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void refresh(List<Friend> friends) {
        Timber.d("refresh friends database on wear");
        ArrayList<DataMap> friendsDataMap = new ArrayList<>(friends.size());
        for (Friend friend : friends) {
            friendsDataMap.add(friendToDataMap(friend));
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(Constants.PATH_FRIENDS_REFRESH);
        dataMapRequest.getDataMap().putDataMapArrayList(Constants.KEY_FRIENDS_LIST, friendsDataMap);
        dataMapRequest.getDataMap().putLong(Constants.KEY_FORCE_UPDATE, System.currentTimeMillis());

        send(dataMapRequest);
    }

    private void send(final PutDataMapRequest dataMapRequest) {
        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        if (connectionResult.isSuccess()) {
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, dataMapRequest.asPutDataRequest());

            Timber.d("Uploading friends update to wear");
            DataApi.DataItemResult result = pendingResult.await();

            if (!result.getStatus().isSuccess()) {
                Timber.w("Could not upload to wear");
            }
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private DataMap friendToDataMap(Friend friend) {
        DataMap dataMap = new DataMap();
        dataMap.putLong(Constants.KEY_FRIEND_ID, friend.getRecordId());
        dataMap.putLong(Constants.KEY_FRIEND_USER_ID, friend.getFriendId());
        dataMap.putString(Constants.KEY_FRIEND_DISPLAY_NAME, friend.getDisplayName());
        return dataMap;
    }
}
