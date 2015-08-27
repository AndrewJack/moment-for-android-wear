package technology.mainthread.apps.moment.ui.fragment;

import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.RefWatcher;
import com.trello.rxlifecycle.components.RxFragment;

import javax.inject.Inject;

import rx.Observable;
import technology.mainthread.apps.moment.MomentApp;
import technology.mainthread.apps.moment.common.rx.RxSchedulerHelper;

public abstract class BaseFragment extends RxFragment {

    private final Observable.Transformer applySchedulersTransformer = RxSchedulerHelper.applySchedulers();

    @Inject
    Tracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentApp.get(getActivity()).inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // send a fragment screen view
        tracker.setScreenName(this.getClass().getSimpleName());
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MomentApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    public final <T> Observable.Transformer<T, T> applySchedulers() {
        return applySchedulersTransformer;
    }
}
