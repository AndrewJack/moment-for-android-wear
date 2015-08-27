package technology.mainthread.apps.moment.background.sync;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.data.api.FriendsSync;
import timber.log.Timber;


public class MomentSyncAdapter extends AbstractThreadedSyncAdapter {

    @Inject
    FriendsSync friendsSync;

    public MomentSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        postConstruct();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MomentSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        postConstruct();
    }

    private void postConstruct() {
        MomentApp.get(getContext()).inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Timber.d("Performing friends sync");

        try {
            friendsSync.syncFriends();
            Timber.d("syncing friends complete");
        } catch (IOException e) {
            Timber.w(e, "sync friends failed");
        }
    }


}
