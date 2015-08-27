package technology.mainthread.apps.moment.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.Node;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.wear.WearableFinder;
import technology.mainthread.apps.moment.ui.activity.SignInStateUpdater;

public class WearablesFragment extends BaseFragment {

    @Inject
    WearableFinder wearableFinder;

    @Bind(R.id.loader_container)
    View mLoaderContainer;
    @Bind(R.id.content)
    View mMainContent;
    @Bind(R.id.img_wearable_status)
    ImageView mWearableStatusImageView;
    @Bind(R.id.txt_result)
    TextView mResultText;
    @Bind(R.id.txt_result_detail)
    TextView mResultDetailText;

    private Subscription wearNodesSubscription = Subscriptions.empty();
    private final Subscriber<List<Node>> wearNodesSubscriber = new Subscriber<List<Node>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            setResult(Collections.<Node>emptyList());
        }

        @Override
        public void onNext(List<Node> nodes) {
            setResult(nodes);
        }
    };

    private SignInStateUpdater mSignInStateUpdater;

    public static Fragment newInstance() {
        return new WearablesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSignInStateUpdater = (SignInStateUpdater) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wearables, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        wearNodesSubscription.unsubscribe();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Observable<List<Node>> wearNodesObservable = wearableFinder.getWearableNodes()
                .compose(this.<List<Node>>bindToLifecycle())
                .compose(this.<List<Node>>applySchedulers());
        wearNodesSubscription = wearNodesObservable.subscribe(wearNodesSubscriber);
    }

    private void setResult(List<Node> nodes) {
        if (nodes.isEmpty()) {
            mWearableStatusImageView.setImageResource(R.drawable.ic_watch_not_connected_120dp);
            mResultText.setText(R.string.result_wearables_not_found);
            mResultDetailText.setText(R.string.result_wearables_not_found_detail);
        } else {
            mWearableStatusImageView.setImageResource(R.drawable.ic_watch_120dp);
            mResultText.setText(R.string.result_wearables_found);
            mResultDetailText.setText(R.string.result_wearables_found_detail);
        }
        mLoaderContainer.setVisibility(View.GONE);
        mMainContent.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_continue)
    void onContinueClick() {
        mSignInStateUpdater.updateSignInState();
    }
}
