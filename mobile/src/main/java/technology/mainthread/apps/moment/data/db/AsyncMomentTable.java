package technology.mainthread.apps.moment.data.db;

import android.os.AsyncTask;

import java.util.List;

import technology.mainthread.apps.moment.common.data.vo.Moment;

public class AsyncMomentTable implements MomentTable {

    private final MomentTable syncMomentTable;

    public AsyncMomentTable(MomentTable syncMomentTable) {
        this.syncMomentTable = syncMomentTable;
    }

    @Override
    public void add(final Moment moment) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncMomentTable.add(moment);
                return null;
            }
        }.execute();
    }

    @Override
    public Moment get(long id) {
        return syncMomentTable.get(id);
    }

    @Override
    public Moment getNextInLine() {
        return syncMomentTable.getNextInLine();
    }

    @Override
    public List<Moment> getAll() {
        return syncMomentTable.getAll();
    }

    @Override
    public void update(final Moment moment) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncMomentTable.update(moment);
                return null;
            }
        }.execute();
    }

    @Override
    public void delete(final int id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncMomentTable.delete(id);
                return null;
            }
        }.execute();
    }

    @Override
    public void deleteAll() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                syncMomentTable.deleteAll();
                return null;
            }
        }.execute();
    }

    @Override
    public boolean hasMoments() {
        return syncMomentTable.hasMoments();
    }
}
