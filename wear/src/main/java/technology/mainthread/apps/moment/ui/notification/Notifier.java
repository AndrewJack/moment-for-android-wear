package technology.mainthread.apps.moment.ui.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import javax.inject.Inject;

import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.ui.activity.DrawActivity;

public class Notifier {

    public static final int ID_ADD = 20;

    private static final String GROUP_KEY_MOMENT = "key_moment";

    private final NotificationManagerCompat notificationManager;
    private final Context context;
    private final Resources resources;

    @Inject
    public Notifier(NotificationManagerCompat notificationManager, Context context, Resources resources) {
        this.notificationManager = notificationManager;
        this.context = context;
        this.resources = resources;
    }

    public void showNewMoment(long momentId, long sender, String senderName, Bitmap drawing) {
        Bitmap bgWhiteDrawing = Bitmap.createBitmap(drawing.getWidth(), drawing.getHeight(), drawing.getConfig());
        Canvas canvas = new Canvas(bgWhiteDrawing);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(drawing, 0, 0, null);

        PendingIntent drawPendingIntent = PendingIntent.getActivity(context, 0,
                DrawActivity.getDrawIntent(context, sender), 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(resources.getString(R.string.notification_new_moment_title))
                .setContentText(resources.getString(R.string.notification_new_moment_text, senderName))
                .setGroup(GROUP_KEY_MOMENT)
                .addAction(R.drawable.ic_reply_48dp, resources.getString(R.string.notification_action_reply), drawPendingIntent);

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bgWhiteDrawing);
        notificationBuilder.setStyle(style);

        notificationManager.notify((int) momentId, notificationBuilder.build());
    }

    public void showFriendAddedBack(long senderId, String senderName) {
        PendingIntent drawPendingIntent = PendingIntent.getActivity(context, 0,
                DrawActivity.getDrawIntent(context, senderId), 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(resources.getString(R.string.notification_added_back, senderName))
                .addAction(R.drawable.ic_create_48dp, resources.getString(R.string.notification_action_message), drawPendingIntent);

        notificationManager.notify(ID_ADD, notificationBuilder.build());
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }
}
