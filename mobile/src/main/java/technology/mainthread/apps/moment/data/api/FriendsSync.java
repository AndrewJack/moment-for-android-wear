package technology.mainthread.apps.moment.data.api;

import java.io.IOException;

import javax.inject.Inject;

import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.common.data.vo.Friend;
import technology.mainthread.apps.moment.data.db.SyncFriends;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;
import technology.mainthread.apps.moment.data.wear.FriendWearSender;
import technology.mainthread.apps.moment.util.CredentialUtil;
import technology.mainthread.service.moment.friendApi.FriendApi;
import technology.mainthread.service.moment.friendApi.model.CollectionResponseFriendResponse;
import technology.mainthread.service.moment.friendApi.model.FriendResponse;
import timber.log.Timber;

public class FriendsSync {

    private final FriendApi.Builder friendApiBuilder;
    private final MomentPreferences preferences;
    private final FriendsTable friendsTable;
    private final FriendWearSender friendSender;

    private FriendApi friendApi;

    @Inject
    public FriendsSync(FriendApi.Builder friendApiBuilder,
                       MomentPreferences preferences,
                       @SyncFriends FriendsTable friendsTable,
                       FriendWearSender friendSender) {
        this.friendApiBuilder = friendApiBuilder;
        this.preferences = preferences;
        this.friendsTable = friendsTable;
        this.friendSender = friendSender;
    }

    private boolean checkCredential() {
        String accountName = preferences.getAccountName();
        boolean result = false;
        if (accountName != null) {
            friendApi = (FriendApi) CredentialUtil.updateCredential(friendApiBuilder, accountName);
            result = true;
        }
        return result;
    }

    public void syncFriends() throws IOException {
        if (checkCredential()) {
            Timber.d("Syncing friends");
            CollectionResponseFriendResponse friends = friendApi.friends().all().execute();
            if (syncTable(friendsTable, friends)) {
                friendSender.refresh(friendsTable.getAll());
            }
        } else {
            Timber.e("Cancelling sync, because account name is null");
        }
    }

    private boolean syncTable(FriendsTable table, CollectionResponseFriendResponse requests) {
        boolean changed = false;
        if (requests != null) {
            table.deleteAll();
            if (requests.getItems() != null) {
                for (FriendResponse friend : requests.getItems()) {
                    table.add(Friend.builder()
                            .friendId(friend.getFriendId())
                            .displayName(friend.getDisplayName())
                            .profileImageUrl(friend.getProfileImageUrl())
                            .build());
                }
            }
            changed = true;
        }
        return changed;
    }

}
