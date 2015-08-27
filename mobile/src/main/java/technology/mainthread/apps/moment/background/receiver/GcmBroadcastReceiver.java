package technology.mainthread.apps.moment.background.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import technology.mainthread.apps.moment.background.service.GcmListenerService;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName componentName = new ComponentName(context.getPackageName(),
                GcmListenerService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(componentName)));

        setResultCode(Activity.RESULT_OK);
    }

}
