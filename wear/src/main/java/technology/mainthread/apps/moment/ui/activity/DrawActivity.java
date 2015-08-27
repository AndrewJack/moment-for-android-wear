package technology.mainthread.apps.moment.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.trello.rxlifecycle.components.RxActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import technology.mainthread.apps.moment.MomentWearApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.background.WearToMobileSender;
import technology.mainthread.apps.moment.data.detector.ShakeDetector;
import technology.mainthread.apps.moment.data.prefs.WearMomentPreferences;
import technology.mainthread.apps.moment.ui.view.DrawingView;
import timber.log.Timber;

import static technology.mainthread.apps.moment.common.rx.RxSchedulerHelper.applySchedulers;

public class DrawActivity extends RxActivity implements DrawingView.DrawingListener, ShakeDetector.Listener {

    private static final String PARAM_RECIPIENT = "PARAM_RECIPIENT";

    private static final long INITIAL_DELAY = 1000;

    private final Handler drawHandler = new Handler();
    private final Runnable finishedDrawingRunnable = new Runnable() {
        @Override
        public void run() {
            shownHelpToast = true;
            showToast(R.string.help_double_tap);
        }
    };

    @Inject
    WearMomentPreferences preferences;
    @Inject
    SensorManager sensorManager;
    @Inject
    WearToMobileSender wearToMobileSender;

    @Bind(R.id.stub)
    WatchViewStub stub;
    @Bind(R.id.dismiss_overlay)
    DismissOverlayView mDismissOverlayView;
    private DrawingView mDrawingView;

    private boolean shownHelpToast;
    private ShakeDetector shakeDetector;
    private Subscription requestSyncSubscription = Subscriptions.empty();

    public static Intent getDrawIntent(Context context, Long recipient) {
        Intent intent = new Intent(context, DrawActivity.class);
        intent.putExtra(PARAM_RECIPIENT, recipient);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentWearApp.get(this).inject(this);
        setContentView(R.layout.activity_draw);
        ButterKnife.bind(this);

        // re-enable drawing view when overlay view is cancelled
        mDismissOverlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mDrawingView.setEnabled(true);
                }
                return false;
            }
        });

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mDrawingView = (DrawingView) findViewById(R.id.view_drawing);
                mDrawingView.setDrawingListener(DrawActivity.this);

                if (preferences.shouldShowHelp()) {
                    showToast(R.string.help_start_drawing);
                }
            }
        });

        requestSyncSubscription = wearToMobileSender
                .requestFriendsSync()
                .compose(bindToLifecycle())
                .compose(applySchedulers())
                .subscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDrawingView != null) {
            mDrawingView.setEnabled(true);
        }

        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(sensorManager);
    }

    @Override
    protected void onPause() {
        shakeDetector.stop();

        if (mDrawingView != null) {
            mDrawingView.setEnabled(false);
        }
        removeFinishedDrawingCallback();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        requestSyncSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onActionDown() {
        Timber.d("onActionDown");
        removeFinishedDrawingCallback();
    }

    @Override
    public void onActionUp() {
        Timber.d("onActionUp");
        if (preferences.shouldShowHelp() && !shownHelpToast) {
            drawHandler.postDelayed(finishedDrawingRunnable, INITIAL_DELAY);
        }
    }

    @Override
    public void onLongPress() {
        Timber.d("onLongPress");
        mDrawingView.setEnabled(false);
        mDismissOverlayView.show();
    }

    @Override
    public void onDoubleTap() {
        Timber.d("onDoubleTap");
        finishedDrawing(mDrawingView.getDrawing());
    }

    private void showToast(@StringRes int message) {
        Toast.makeText(DrawActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void removeFinishedDrawingCallback() {
        drawHandler.removeCallbacks(finishedDrawingRunnable);
    }

    private void finishedDrawing(Bitmap drawing) {
        Timber.d("Opening sender activity, drawing is %d bytes", drawing.getByteCount());
        startActivity(SenderActivity.getRecipientSelectorIntent(this, getIntent().getLongExtra(PARAM_RECIPIENT, 0), drawing));
    }

    @Override
    public void hearShake() {
        Timber.d("hearShake");
        Toast.makeText(DrawActivity.this, R.string.drawing_cleared, Toast.LENGTH_SHORT).show();
        if (mDrawingView != null) {
            mDrawingView.reset();
        }
    }
}
