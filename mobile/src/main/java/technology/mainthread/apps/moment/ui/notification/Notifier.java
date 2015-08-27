package technology.mainthread.apps.moment.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import javax.inject.Inject;

import technology.mainthread.apps.moment.R;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;

import static technology.mainthread.apps.moment.background.service.UpdateFriendsIntentService.getUpdateFriendsServiceIntent;
import static technology.mainthread.apps.moment.ui.activity.FriendDiscoveryActivity.getFriendDiscoveryIntent;
import static technology.mainthread.apps.moment.ui.activity.MainActivity.getMainActivityIntent;
import static technology.mainthread.apps.moment.ui.activity.MomentActivity.getMomentActivityIntent;

public class Notifier {

    public static final int ID_MOMENT = 10;
    public static final int ID_ADD = 20;

    private final Context context;
    private final Resources resources;
    private final NotificationManagerCompat notificationManager;

    @Inject
    public Notifier(Context context, Resources resources, NotificationManagerCompat notificationManager) {
        this.context = context;
        this.resources = resources;
        this.notificationManager = notificationManager;
    }

    public void showMobileOnlyMomentNotification(MomentResponse moment) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                getMomentActivityIntent(context, moment.getMomentId()), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = getBaseNotification()
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(resources.getString(R.string.notification_new_moment_title))
                .setContentText(resources.getString(R.string.notification_new_moment_text, moment.getSenderName()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setLocalOnly(true);

        notificationManager.notify(ID_MOMENT, notificationBuilder.build());
    }

    public void showFriendAddNotification(long friendId, String name) {
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0,
                getFriendDiscoveryIntent(context), 0);
        PendingIntent addPendingIntent = PendingIntent.getService(context, 0,
                getUpdateFriendsServiceIntent(context, friendId), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = getBaseNotification()
                .setContentTitle(resources.getString(R.string.notification_added, name))
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_stat_person_add, resources.getString(R.string.notification_action_add), addPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(ID_ADD, notification.build());
    }

    public void showFriendAddedBackNotification(String name) {
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0,
                getMainActivityIntent(context), 0);

        NotificationCompat.Builder notification = getBaseNotification()
                .setContentTitle(resources.getString(R.string.notification_added_back, name))
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(ID_ADD, notification.build());
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }

    private NotificationCompat.Builder getBaseNotification() {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_moment_logo);
    }
}
