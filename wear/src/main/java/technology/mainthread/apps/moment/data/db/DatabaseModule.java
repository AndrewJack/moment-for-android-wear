package technology.mainthread.apps.moment.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

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

    private final SQLiteOpenHelper helper;

    public DatabaseModule(Context context) {
        helper = new FriendDbHelper(context, TABLE_FRIENDS);
    }

    @Provides
    @Singleton
    @SyncWearFriends
    FriendsTable providesSyncFriendsTable() {
        return new SyncFriendTable(helper, TABLE_FRIENDS);
    }

    @Provides
    @Singleton
    @AsyncWearFriends
    FriendsTable providesAsyncFriendsTable(@SyncWearFriends FriendsTable syncFriendsTable) {
        return new AsyncFriendsTable(syncFriendsTable);
    }

}
