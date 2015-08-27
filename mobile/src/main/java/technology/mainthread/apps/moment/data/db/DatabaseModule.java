package technology.mainthread.apps.moment.data.db;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import technology.mainthread.apps.moment.common.data.db.AsyncFriendsTable;
import technology.mainthread.apps.moment.common.data.db.FriendDbHelper;
import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.common.data.db.SyncFriendTable;

@Module
public class DatabaseModule {

    private static final String TABLE_FRIENDS = "TABLE_FRIENDS";
    private static final String TABLE_MOMENT = "TABLE_MOMENT";

    private final FriendDbHelper friendsHelper;
    private final MomentDbHelper momentHelper;

    public DatabaseModule(Context context) {
        friendsHelper = new FriendDbHelper(
                context,
                TABLE_FRIENDS
        );

        momentHelper = new MomentDbHelper(
                context,
                TABLE_MOMENT);
    }

    @Provides
    @Singleton
    @SyncFriends
    FriendsTable provideSyncFriendsTable() {
        return new SyncFriendTable(friendsHelper, TABLE_FRIENDS);
    }

    @Provides
    @Singleton
    @AsyncFriends
    FriendsTable provideAsyncFriendsTable(@SyncFriends FriendsTable syncFriendsTable) {
        return new AsyncFriendsTable(syncFriendsTable);
    }

    @Provides
    @Singleton
    @SyncMoment
    MomentTable provideSyncMomentTable() {
        return new SyncMomentTable(momentHelper, TABLE_MOMENT);
    }

    @Provides
    @Singleton
    @AsyncMoment
    MomentTable provideAsyncMomentTable(@SyncMoment MomentTable syncMomentTable) {
        return new AsyncMomentTable(syncMomentTable);
    }
}
