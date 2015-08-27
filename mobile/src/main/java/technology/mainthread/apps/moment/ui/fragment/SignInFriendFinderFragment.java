package technology.mainthread.apps.moment.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.rx.api.RxFriendApi;
import technology.mainthread.apps.moment.data.rx.api.RxSyncFriends;
import technology.mainthread.apps.moment.ui.adapter.FriendDiscoveryAdapter;
import technology.mainthread.apps.moment.ui.view.LoaderCheckBox;
import technology.mainthread.service.moment.friendApi.model.FriendResponse;

public class SignInFriendFinderFragment extends BaseFragment implements FriendDiscoveryAdapter.FriendAddListener {

    @Inject
    RxFriendApi rxFriendApi;
    @Inject
    RxSyncFriends rxSyncFriends;

    @Bind(R.id.recycler_found_friends)
    RecyclerView mFoundFriendsRecycler;
    @Bind(R.id.txt_info)
    TextView mInfoText;
    @Bind(R.id.btn_invite)
    Button mInviteButton;
    @Bind(R.id.container_info)
    View mInfoContainer;
    @Bind(R.id.progress)
    View mProgress;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Fragment newInstance() {
        return new SignInFriendFinderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in_friend_finder, container, false);
        ButterKnife.bind(this, rootView);

        mInfoText.setText(R.string.searching_friends);

        mFoundFriendsRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        searchForFriends();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        compositeSubscription.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @OnClick(R.id.btn_continue)
    void onContinueClicked() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                .replace(R.id.container, WearablesFragment.newInstance())
                .commit();
    }

    private void searchForFriends() {
        Observable<List<FriendResponse>> searchObservable = rxFriendApi.search()
                .compose(this.<List<FriendResponse>>bindToLifecycle())
                .compose(this.<List<FriendResponse>>applySchedulers());
        compositeSubscription.add(searchObservable.subscribe(new Subscriber<List<FriendResponse>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                noFriendsFound();
            }

            @Override
            public void onNext(List<FriendResponse> response) {
                friendsFound(response);
            }
        }));
    }

    private void friendsFound(List<FriendResponse> response) {
        mProgress.setVisibility(View.GONE);
        if (response != null && !response.isEmpty()) {
            mFoundFriendsRecycler.setVisibility(View.VISIBLE);
            mInfoContainer.setVisibility(View.GONE);
            mFoundFriendsRecycler.setAdapter(new FriendDiscoveryAdapter(response, this, android.R.color.white));
        } else {
            noFriendsFound();
        }
    }

    private void noFriendsFound() {
        mFoundFriendsRecycler.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
        mInfoContainer.setVisibility(View.VISIBLE);
        mInfoText.setVisibility(View.VISIBLE);
        mInfoText.setText(R.string.no_friends_found);
        mInviteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAddClicked(final LoaderCheckBox view, long friendId, boolean isChecked) {
        if (isChecked) {
            // add
            Observable<Void> addObservable = rxFriendApi.add(friendId)
                    .compose(this.<Void>bindToLifecycle())
                    .compose(this.<Void>applySchedulers());
            compositeSubscription.add(addObservable.subscribe(new LoaderCheckBox.RxSubscriber(view) {
                @Override
                public void onNext(Void aVoid) {
                    super.onNext(aVoid);
                    Observable<Void> syncPending = rxSyncFriends.syncFriends()
                            .compose(SignInFriendFinderFragment.this.<Void>bindToLifecycle())
                            .compose(SignInFriendFinderFragment.this.<Void>applySchedulers());
                    compositeSubscription.add(syncPending.subscribe());
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
                    Observable<Void> syncPending = rxSyncFriends.syncFriends()
                            .compose(SignInFriendFinderFragment.this.<Void>bindToLifecycle())
                            .compose(SignInFriendFinderFragment.this.<Void>applySchedulers());
                    compositeSubscription.add(syncPending.subscribe());
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
