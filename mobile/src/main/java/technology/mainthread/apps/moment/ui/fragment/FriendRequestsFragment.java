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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.rx.api.RxFriendApi;
import technology.mainthread.apps.moment.data.rx.api.RxSyncFriends;
import technology.mainthread.apps.moment.ui.adapter.FriendRequestsAdapter;
import technology.mainthread.apps.moment.ui.view.LoaderCheckBox;
import technology.mainthread.service.moment.friendApi.model.FriendResponse;

public class FriendRequestsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, FriendRequestsAdapter.RequestClickListener {

    @Inject
    RxSyncFriends friendsSync;
    @Inject
    RxFriendApi rxFriendApi;

    @Bind(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.recycler_requests)
    RecyclerView mRequestsRecyclerView;

    // no friends
    @Bind(R.id.container_info)
    View mInfoContainer;
    @Bind(R.id.progress)
    View mProgress;
    @Bind(R.id.txt_info)
    TextView mInfoText;
    @Bind(R.id.btn_invite)
    View mInviteButton;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Fragment newInstance() {
        return new FriendRequestsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_request, container, false);
        ButterKnife.bind(this, rootView);

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.primary_dark, R.color.primary, R.color.accent);

        mRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        getFriendRequests();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        compositeSubscription.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        getFriendRequests();
    }

    private void getFriendRequests() {
        Observable<List<FriendResponse>> requestsObservable = rxFriendApi.requests()
                .compose(this.<List<FriendResponse>>bindToLifecycle())
                .compose(this.<List<FriendResponse>>applySchedulers());
        compositeSubscription.add(requestsObservable.subscribe(new Subscriber<List<FriendResponse>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.setRefreshing(false);
                showNoFriendsFound();
            }

            @Override
            public void onNext(List<FriendResponse> responses) {
                mRefreshLayout.setRefreshing(false);
                if (!responses.isEmpty()) {
                    onFriendsFound(responses);
                } else {
                    showNoFriendsFound();
                }
            }
        }));
    }

    private void onFriendsFound(List<FriendResponse> responses) {
        mInfoContainer.setVisibility(View.GONE);
        mRequestsRecyclerView.setAdapter(new FriendRequestsAdapter(responses, this));
    }

    private void showNoFriendsFound() {
        mProgress.setVisibility(View.GONE);
        mInfoText.setText(R.string.zero_friend_requests);
        mInviteButton.setVisibility(View.VISIBLE);
        mRequestsRecyclerView.setAdapter(new FriendRequestsAdapter(new ArrayList<FriendResponse>(), this));
    }

    private void syncFriends() {
        Observable<Void> syncFriendsObservable = friendsSync.syncFriends()
                .compose(this.<Void>bindToLifecycle())
                .compose(this.<Void>applySchedulers());
        compositeSubscription.add(syncFriendsObservable.subscribe());
    }

    @Override
    public void onAddClicked(LoaderCheckBox view, long friendId, boolean isChecked) {
        if (isChecked) {
            // add
            Observable<Void> addObservable = rxFriendApi.add(friendId)
                    .compose(this.<Void>bindToLifecycle())
                    .compose(this.<Void>applySchedulers());
            compositeSubscription.add(addObservable.subscribe(new LoaderCheckBox.RxSubscriber(view) {
                @Override
                public void onNext(Void aVoid) {
                    super.onNext(aVoid);
                    syncFriends();
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    Toast.makeText(getActivity(), getString(R.string.toast_friend_add_error), Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            // remove
            Observable<Void> removeObservable = rxFriendApi.remove(friendId)
                    .compose(this.<Void>bindToLifecycle())
                    .compose(this.<Void>applySchedulers());
            compositeSubscription.add(removeObservable.subscribe(new LoaderCheckBox.RxSubscriber(view) {
                @Override
                public void onNext(Void aVoid) {
                    super.onNext(aVoid);
                    syncFriends();
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    Toast.makeText(getActivity(), getString(R.string.toast_friend_remove_error), Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }
}
