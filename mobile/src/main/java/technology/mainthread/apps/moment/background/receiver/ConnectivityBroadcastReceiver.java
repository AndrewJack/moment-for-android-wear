package technology.mainthread.apps.moment.background.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import javax.inject.Inject;

import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.background.ConnectivityHelper;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static technology.mainthread.apps.moment.background.service.MomentSenderService.getMomentSenderServiceStartIntent;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    @Inject
    ConnectivityHelper connectivityHelper;

    public static void enableNetworkChangeReceiver(Context context, boolean enabled) {
        ComponentName receiver = new ComponentName(context, ConnectivityBroadcastReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiver,
                enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MomentApp.get(context).inject(this);

        if (connectivityHelper.isConnected()) {
            enableNetworkChangeReceiver(context, false); // disable receiver
            context.startService(getMomentSenderServiceStartIntent(context));
        }
    }

}
