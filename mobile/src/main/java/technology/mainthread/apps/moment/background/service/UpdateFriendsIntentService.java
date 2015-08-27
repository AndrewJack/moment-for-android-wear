package technology.mainthread.apps.moment.background.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.data.api.FriendsSync;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.ui.notification.Notifier;
import technology.mainthread.apps.moment.util.CredentialUtil;
import technology.mainthread.service.moment.friendApi.FriendApi;
import timber.log.Timber;

public class UpdateFriendsIntentService extends IntentService {

    private static final String ACTION_ADD_FRIEND = "technology.mainthread.apps.moment.background.service.action.addFriend";
    private static final String EXTRA_FRIEND_ID = "technology.mainthread.apps.moment.background.service.extra.friendId";

    @Inject
    FriendApi.Builder friendApiBuilder;
    @Inject
    Tracker tracker;
    @Inject
    MomentPreferences preferences;
    @Inject
    FriendsSync friendsSync;
    @Inject
    Notifier notifier;

    private FriendApi friendApi;

    public static Intent getUpdateFriendsServiceIntent(Context context, long friendId) {
        Intent intent = new Intent(context, UpdateFriendsIntentService.class);
        intent.setAction(ACTION_ADD_FRIEND);
        intent.putExtra(EXTRA_FRIEND_ID, friendId);
        return intent;
    }

    public UpdateFriendsIntentService() {
        super(UpdateFriendsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MomentApp.get(this).inject(this);
        friendApi = friendApiBuilder.build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ADD_FRIEND.equals(action)) {
                if (preferences.getUserId() != 0) {
                    addFriend(intent.getLongExtra(EXTRA_FRIEND_ID, 0));
                }
            }
        }
    }

    // When this class gets created the credential account name may not have been set
    private void checkCredential() {
        friendApi = (FriendApi) CredentialUtil.updateCredential(friendApiBuilder, preferences.getAccountName());
    }

    private void addFriend(long friendId) {
        checkCredential();
        trackFriendAction("add");
        try {
            Timber.d("Adding friend %d", friendId);
            friendApi.friends().add(friendId).execute();
            notifier.cancelNotification(Notifier.ID_ADD);
        } catch (Exception e) {
            Timber.w(e, "Adding friends %d failed", friendId);
        } finally {
            try {
                friendsSync.syncFriends();
            } catch (Exception e) {
                Timber.w(e, "Syncing friends failed");
            }
        }
    }

    private void trackFriendAction(String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("friend")
                .setAction(action)
                .build());
    }
}
