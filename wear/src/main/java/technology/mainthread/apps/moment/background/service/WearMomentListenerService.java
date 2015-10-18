package technology.mainthread.apps.moment.background.service;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentWearApp;
import technology.mainthread.apps.moment.common.Constants;
import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.data.db.AsyncWearFriends;
import technology.mainthread.apps.moment.data.prefs.WearMomentPreferences;
import technology.mainthread.apps.moment.ui.notification.Notifier;
import timber.log.Timber;

import static technology.mainthread.apps.moment.data.ConvertUtil.toFriend;

public class WearMomentListenerService extends WearableListenerService {

    @Inject
    Notifier notifier;
    @Inject
    @AsyncWearFriends
    FriendsTable friendsTable;
    @Inject
    WearMomentPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        MomentWearApp.get(this).inject(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            String path = dataEvent.getDataItem().getUri().getPath();
            Timber.d("Path == %s", path);

            switch (path) {
                case Constants.PATH_MOMENT_NOTIFICATION:
                    handleNewMoment(dataEvent);
                    break;
                case Constants.PATH_FRIEND_ADDED_BACK:
                    handleFriendAddedNotification(dataEvent);
                    break;
                case Constants.PATH_FRIENDS_REFRESH:
                    handleRefreshFriends(dataEvent);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleNewMoment(DataEvent dataEvent) {
        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        long momentId = dataMapItem.getDataMap().getLong(Constants.KEY_MOMENT_ID);
        long senderId = dataMapItem.getDataMap().getLong(Constants.KEY_SENDER_ID);
        String senderName = dataMapItem.getDataMap().getString(Constants.KEY_SENDER_NAME);
        Asset asset = dataMapItem.getDataMap().getAsset(Constants.KEY_DRAWING);

        WearEventsIntentService.showMomentNotification(this, momentId, senderId, senderName, asset);
    }

    /**
     * Display friend added back notification with send drawing action.
     */
    private void handleFriendAddedNotification(DataEvent dataEvent) {
        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        long senderId = dataMapItem.getDataMap().getLong(Constants.KEY_SENDER_ID);
        String senderName = dataMapItem.getDataMap().getString(Constants.KEY_SENDER_NAME);

        notifier.showFriendAddedBack(senderId, senderName);
    }

    private void handleRefreshFriends(DataEvent dataEvent) {
        friendsTable.deleteAll();

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        List<DataMap> friendsDataMap = dataMapItem.getDataMap().getDataMapArrayList(Constants.KEY_FRIENDS_LIST);

        if (friendsDataMap != null) {
            for (DataMap dataMap : friendsDataMap) {
                friendsTable.add(toFriend(dataMap));
            }
        }

        preferences.friendsRefreshedSuccessfully();
    }

}
