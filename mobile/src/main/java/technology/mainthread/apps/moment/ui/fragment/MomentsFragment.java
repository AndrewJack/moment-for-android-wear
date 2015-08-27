package technology.mainthread.apps.moment.ui.fragment;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.subscriptions.CompositeSubscription;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.data.rx.api.RxMomentApi;
import technology.mainthread.apps.moment.ui.adapter.MomentAdapter;
import technology.mainthread.service.moment.momentApi.model.MomentResponse;
import timber.log.Timber;

import static technology.mainthread.apps.moment.ui.activity.MomentActivity.getMomentActivityIntent;

public class MomentsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, MomentAdapter.OnSelectedListener {

    private static final String PARAM_MOMENT_VIEW = "param_moment_view";

    public enum MomentView {
        RECEIVED,
        SENT
    }

    @Inject
    RxMomentApi momentApi;
    @Inject
    Picasso picasso;

    @Bind(R.id.recycler_moments)
    RecyclerView mRecyclerMoments;
    @Bind(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Fragment newInstance(MomentView momentView) {
        MomentsFragment fragment = new MomentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MOMENT_VIEW, momentView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_moment, container, false);
        ButterKnife.bind(this, rootView);

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.primary_dark,
                R.color.primary,
                R.color.accent);

        mRecyclerMoments.setLayoutManager(new GridLayoutManager(getActivity(), getSpanCount()));
        loadMoments();

        return rootView;
    }

    private int getSpanCount() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x / 600;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        compositeSubscription.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        loadMoments();
    }

    private void loadMoments() {
        MomentView momentView = (MomentView) getArguments().getSerializable(PARAM_MOMENT_VIEW);
        switch (momentView) {
            case RECEIVED:
                getMoments(momentApi.allReceived());
                break;
            case SENT:
                getMoments(momentApi.allSent());
                break;
        }
    }

    private void getMoments(Observable<List<MomentResponse>> observable) {
        Observable<List<MomentResponse>> momentsObservable = observable
                .compose(this.<List<MomentResponse>>bindToLifecycle())
                .compose(this.<List<MomentResponse>>applySchedulers());
        compositeSubscription.add(momentsObservable.subscribe(new Observer<List<MomentResponse>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                mRefreshLayout.setRefreshing(false);
                Timber.w(e, "Error fetching moments");
                Toast.makeText(getActivity(), "Can't get moments", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(List<MomentResponse> moments) {
                mRefreshLayout.setRefreshing(false);
                if (moments != null) {
                    updateAdapter(moments);
                }
            }
        }));
    }

    private void updateAdapter(List<MomentResponse> moments) {
        mRecyclerMoments.setAdapter(new MomentAdapter(picasso, moments, this));
    }

    @Override
    public void onMomentSelected(long momentId) {
        startActivity(getMomentActivityIntent(getActivity(), momentId));
    }

}