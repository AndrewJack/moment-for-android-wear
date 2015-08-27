package technology.mainthread.apps.moment.common.data.db;

import android.os.AsyncTask;

import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Friend;

public class AsyncFriendsTable implements FriendsTable {

    private final FriendsTable syncFriendsTable;

    public AsyncFriendsTable(FriendsTable syncFriendsTable) {
        this.syncFriendsTable = syncFriendsTable;
    }

    @Override
    public void add(final Friend friend) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncFriendsTable.add(friend);
                return null;
            }
        }.execute();
    }

    @Override
    public Friend get(long index) {
        return syncFriendsTable.get(index);
    }

    @Override
    public List<Friend> getAll() {
        return syncFriendsTable.getAll();
    }

    @Override
    public void addOrUpdate(final Friend friend) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncFriendsTable.addOrUpdate(friend);
                return null;
            }
        }.execute();
    }

    @Override
    public void update(final Friend friend) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncFriendsTable.update(friend);
                return null;
            }
        }.execute();
    }

    @Override
    public boolean delete(final long index) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncFriendsTable.delete(index);
                return null;
            }
        }.execute();
        return true;
    }

    @Override
    public boolean deleteAll() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncFriendsTable.deleteAll();
                return null;
            }
        }.execute();
        return true;
    }

    @Override
    public boolean hasFriends() {
        return syncFriendsTable.hasFriends();
    }
}
