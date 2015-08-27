package technology.mainthread.apps.moment.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WearableListView;
import android.view.View;

import com.trello.rxlifecycle.components.RxActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import technology.mainthread.apps.moment.MomentWearApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.background.WearToMobileSender;
import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.common.data.vo.Friend;
import technology.mainthread.apps.moment.common.rx.RxSchedulerHelper;
import technology.mainthread.apps.moment.data.db.AsyncWearFriends;
import technology.mainthread.apps.moment.data.prefs.WearMomentPreferences;
import technology.mainthread.apps.moment.ui.adapter.WearFriendsAdapter;
import technology.mainthread.apps.moment.ui.notification.Notifier;

public class SenderActivity extends RxActivity implements WearableListView.ClickListener, DelayedConfirmationView.DelayedConfirmationListener {

    private static final String PARAM_RECIPIENT = "paramRecipient";
    private static final String PARAM_DRAWING = "paramDrawing";

    @Inject
    WearToMobileSender wearSender;
    @Inject
    @AsyncWearFriends
    FriendsTable friendsTable;
    @Inject
    WearMomentPreferences preferences;
    @Inject
    Notifier notifier;

    @Bind(R.id.list_friends)
    WearableListView mFriendsListView;
    @Bind(R.id.container_sending)
    View mSendingContainer;
    @Bind(R.id.delayed_confirm)
    DelayedConfirmationView mDelayedView;

    private long selectedUserId;
    private Subscription sendDrawingSubscriber = Subscriptions.empty();

    public static Intent getRecipientSelectorIntent(Context context, long recipient, Bitmap drawing) {
        Intent intent = new Intent(context, SenderActivity.class);
        intent.putExtra(PARAM_RECIPIENT, recipient);
        intent.putExtra(PARAM_DRAWING, drawing);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentWearApp.get(this).inject(this);

        setContentView(R.layout.activity_sender);
        ButterKnife.bind(this);

        mDelayedView.setListener(this);

        long recipient = getIntent().getLongExtra(PARAM_RECIPIENT, 0);
        if (recipient != 0) {
            startSendingTimer(recipient);
        } else {
            List<Friend> friends = friendsTable.getAll();
            if (friends.isEmpty()) {
                findViewById(R.id.txt_no_friends).setVisibility(View.VISIBLE);
                mFriendsListView.setVisibility(View.GONE);
            } else {
                mFriendsListView.setAdapter(new WearFriendsAdapter(friends));
                mFriendsListView.setClickListener(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        sendDrawingSubscriber.unsubscribe();
        super.onDestroy();
    }

    private void startSendingTimer(long recipient) {
        selectedUserId = recipient;

        mFriendsListView.setVisibility(View.GONE);
        mSendingContainer.setVisibility(View.VISIBLE);
        mDelayedView.setTotalTimeMs(2000);
        mDelayedView.start();
    }

    @Override
    public void onClick(WearableListView.ViewHolder holder) {
        startSendingTimer((long) holder.itemView.getTag());
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onTimerFinished(View view) {
        notifier.cancelNotification(Notifier.ID_ADD);
        Bitmap drawing = getIntent().getParcelableExtra(PARAM_DRAWING);
        Observable<Void> sendDrawingObservable = wearSender.sendDrawing(selectedUserId, drawing)
                .compose(this.<Void>bindToLifecycle())
                .compose(RxSchedulerHelper.<Void>applySchedulers());
        sendDrawingSubscriber = sendDrawingObservable.subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Intent intent = new Intent(SenderActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.sending_moment_failed));
                startActivity(intent);
            }

            @Override
            public void onNext(Void aVoid) {
                preferences.incrementDrawingCount();
                Intent intent = new Intent(SenderActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onTimerSelected(View view) {
        mDelayedView.setListener(null);
        finish();
    }
}
