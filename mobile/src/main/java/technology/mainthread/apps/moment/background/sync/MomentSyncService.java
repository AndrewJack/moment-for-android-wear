package technology.mainthread.apps.moment.background.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MomentSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static MomentSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new MomentSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
