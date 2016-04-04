package technology.mainthread.apps.moment.background.receiver;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.data.UserManager;
import technology.mainthread.apps.moment.data.prefs.MomentPreferences;

public class AccountChangedReceiver extends BroadcastReceiver {

    @Inject
    MomentPreferences preferences;
    @Inject
    UserManager userManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals(intent.getAction())) {
            MomentApp.get(context).inject(this);

            if (!userManager.isSignedIn() && preferences.getUserId() != 0) {
                userManager.logOut();
            }
        }
    }
}
