package technology.mainthread.apps.moment.background.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.background.ConnectivityHelper;
import technology.mainthread.apps.moment.common.data.vo.Moment;
import technology.mainthread.apps.moment.common.rx.RxSchedulerHelper;
import technology.mainthread.apps.moment.data.db.MomentTable;
import technology.mainthread.apps.moment.data.db.SyncMoment;
import technology.mainthread.apps.moment.data.rx.api.RxMomentApi;
import timber.log.Timber;

import static technology.mainthread.apps.moment.background.receiver.ConnectivityBroadcastReceiver.enableNetworkChangeReceiver;
import static technology.mainthread.apps.moment.common.data.vo.MomentType.DRAWING;

// TODO: convert to sync intent service
public class MomentSenderService extends Service {

    public enum StartCommand {
        START, SEND
    }

    private static final String PARAM_START_COMMAND = "param_start_command";
    private static final String PARAM_RECIPIENT = "param_recipient";
    private static final String PARAM_DRAWING = "param_drawing";

    @Inject
    RxMomentApi rxMomentApi;
    @Inject
    @SyncMoment
    MomentTable momentTable;
    @Inject
    ConnectivityHelper connectivityHelper;

    private int currentMomentId;
    private Subscription momentSubscription = Subscriptions.empty();

    public static Intent getMomentSenderServiceStartIntent(Context context) {
        Intent intent = new Intent(context, MomentSenderService.class);
        intent.putExtra(PARAM_START_COMMAND, StartCommand.START);
        return intent;
    }

    public static Intent getMomentSenderServiceSendIntent(Context context, long[] recipients, Bitmap drawing) {
        Intent intent = new Intent(context, MomentSenderService.class);
        intent.putExtra(PARAM_START_COMMAND, StartCommand.SEND);
        intent.putExtra(PARAM_RECIPIENT, recipients);
        intent.putExtra(PARAM_DRAWING, drawing);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        MomentApp.get(this).inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            StartCommand command = (StartCommand) intent.getSerializableExtra(PARAM_START_COMMAND);
            Timber.d("onStartCommand with command: %s", command);
            switch (command) {
                case START:
                    sendNextInLine();
                    break;
                case SEND:
                    long[] recipient = intent.getLongArrayExtra(PARAM_RECIPIENT);
                    Bitmap drawing = intent.getParcelableExtra(PARAM_DRAWING);
                    queueMoment(convertToList(recipient), drawing);
                    break;
                default:
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        momentSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Do not allow binding
        return null;
    }

    private void queueMoment(List<Long> recipients, Bitmap drawing) {
        if (!recipients.isEmpty() && drawing != null) {
            String fileName = String.valueOf(System.currentTimeMillis());
            try {
                FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                drawing.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                outputStream.close();
            } catch (Exception e) {
                Timber.e(e, "Cannot save drawing");
            }

            momentTable.add(Moment.builder()
                    .recipients(recipients)
                    .fileName(fileName)
                    .momentType(DRAWING)
                    .build());
            sendNextInLine();
        }
    }

    private void sendNextInLine() {
        Timber.d("sendNextInLine");
        if (currentMomentId != 0) {
            // cancel if already sending
            return;
        }

        Moment moment = momentTable.getNextInLine();
        if (moment != null) {
            currentMomentId = moment.getId();
            sendMoment(moment);
        } else {
            stopSelf();
        }
    }

    private void sendMoment(Moment moment) {
        Timber.d("sending moment");
        Bitmap drawing = null;
        try {
            FileInputStream inputStream = openFileInput(moment.getFileName());
            drawing = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Timber.e(e, "Cannot load drawing");
        }
        if (drawing != null) {
            momentSubscription = rxMomentApi.send(moment.getRecipients(), drawing)
                    .compose(RxSchedulerHelper.<Void>applySchedulers())
                    .subscribe(new Observer<Void>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!connectivityHelper.isConnected()) {
                                enableNetworkChangeReceiver(MomentSenderService.this, true);
                            }
                            stopSelf();
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            removeMoment();
                            sendNextInLine();
                        }
                    });
        } else {
            momentTable.delete(moment.getId());
            sendNextInLine();
        }
    }

    private void removeMoment() {
        Timber.d("removeMoment - currentMomentId: %d", currentMomentId);
        if (currentMomentId != 0) {
            Moment moment = momentTable.get(currentMomentId);
            deleteFile(moment.getFileName());
            momentTable.delete(currentMomentId);
            currentMomentId = 0;
        }
    }

    private List<Long> convertToList(long[] longArray) {
        List<Long> list = new ArrayList<>();
        for (long value : longArray) {
            list.add(value);
        }
        return list;
    }

}
