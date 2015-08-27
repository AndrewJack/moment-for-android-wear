package technology.mainthread.apps.moment.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.common.data.db.FriendsTable;
import technology.mainthread.apps.moment.common.data.vo.Friend;
import technology.mainthread.apps.moment.data.bus.DatabaseChangeEvent;
import technology.mainthread.apps.moment.data.bus.RxBus;
import technology.mainthread.apps.moment.data.db.AsyncFriends;
import technology.mainthread.apps.moment.data.rx.api.RxFriendApi;
import technology.mainthread.apps.moment.data.rx.api.RxSyncFriends;
import technology.mainthread.apps.moment.ui.adapter.FriendsAdapter;

public class FriendsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, FriendsAdapter.FriendDeleteClickListener {

    @Inject
    @AsyncFriends
    FriendsTable friendsTable;
    @Inject
    RxSyncFriends friendsSync;
    @Inject
    RxFriendApi rxFriendApi;
    @Inject
    Picasso picasso;
    @Inject
    RxBus bus;

    @Bind(R.id.recycler_friends)
    RecyclerView mFriendsRecyclerView;
    @Bind(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Fragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, rootView);

        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        updateAdapter();

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.primary_dark,
                R.color.primary,
                R.color.accent);

        registerRxBus();

        return rootView;
    }

    private void registerRxBus() {
        Observable<Object> busObservable = bus.toObservable()
                .compose(bindToLifecycle())
                .compose(applySchedulers());
        compositeSubscription.add(busObservable.subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof DatabaseChangeEvent) {
                    updateAdapter();
                }
            }
        }));
    }

    @Override
    public void onDestroyView() {
        compositeSubscription.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        syncFriends();
    }

    private void updateAdapter() {
        mFriendsRecyclerView.setAdapter(new FriendsAdapter(picasso, friendsTable.getAll(), this));
    }

    private void syncFriends() {
        Observable<Void> syncObservable = friendsSync.syncFriends()
                .compose(this.<Void>bindToLifecycle())
                .compose(this.<Void>applySchedulers());
        compositeSubscription.add(syncObservable.subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onNext(Void aVoid) {
                mRefreshLayout.setRefreshing(false);
                updateAdapter();
            }
        }));
    }

    @Override
    public void onFriendDeleteClicked(Friend friend) {
        showDeleteUserDialog(friend);
    }

    private void showDeleteUserDialog(final Friend friend) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_delete_friend)
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        removeFriend(friend);
                    }
                })
                .show();
    }

    private void removeFriend(Friend friend) {
        ((FriendsAdapter) mFriendsRecyclerView.getAdapter()).removeItem(friend);
        sendDeleteFriendRequest(friend);
    }

    private void sendDeleteFriendRequest(Friend friend) {
        Observable<Void> removeObservable = rxFriendApi.remove(friend.getFriendId())
                .compose(this.<Void>bindToLifecycle())
                .compose(this.<Void>applySchedulers());
        compositeSubscription.add(removeObservable.subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                syncFriends();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Void aVoid) {

            }
        }));
    }
}
