package technology.mainthread.apps.moment.ui.activity;

import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import technology.mainthread.apps.moment.R;
import technology.mainthread.apps.moment.common.rx.RxSchedulerHelper;

public abstract class BaseActivity extends RxAppCompatActivity implements ToolbarProvider {

    private final Observable.Transformer applySchedulersTransformer = RxSchedulerHelper.applySchedulers();

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    public final <T> Observable.Transformer<T, T> applySchedulers() {
        return applySchedulersTransformer;
    }

}
