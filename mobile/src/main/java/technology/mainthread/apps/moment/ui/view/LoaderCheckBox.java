package technology.mainthread.apps.moment.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import rx.Subscriber;

/**
 * LoaderCheckBox - shows a progress bar when checked or unchecked
 * until finishProgress is called or the RxSubscriber is used
 */
public class LoaderCheckBox extends FrameLayout implements View.OnClickListener {

    private Listener listener;
    private CheckBox mCheckBox;
    private View mProgress;
    private boolean mLoading;

    public LoaderCheckBox(Context context) {
        super(context);
        initialize();
    }

    public LoaderCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public LoaderCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mCheckBox = new CheckBox(getContext());
        mCheckBox.setOnClickListener(this);
        mCheckBox.setVisibility(VISIBLE);
        mProgress = new ProgressBar(getContext());
        mProgress.setVisibility(GONE);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mCheckBox, params);
        addView(mProgress, params);

    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setChecked(boolean checked) {
        mCheckBox.setChecked(checked);
    }

    public boolean isChecked() {
        return mCheckBox.isChecked();
    }

    public void cancelProgress() {
        if (mLoading) {
            mLoading = false;
            mProgress.setVisibility(GONE);
            mCheckBox.setVisibility(VISIBLE);
        }
    }

    public void finishProgress() {
        if (mLoading) {
            mLoading = false;
            mProgress.setVisibility(GONE);
            mCheckBox.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (!mLoading) {
            mLoading = true;
            mCheckBox.setVisibility(GONE);
            mProgress.setVisibility(VISIBLE);

            if (listener != null) {
                listener.onCheckChanged(this, mCheckBox.isChecked());
            }
        }
    }

    public static class RxSubscriber extends Subscriber<Void> {

        private final LoaderCheckBox view;

        public RxSubscriber(LoaderCheckBox view) {
            this.view = view;
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            view.cancelProgress();
            view.setChecked(!view.isChecked());
        }

        @Override
        public void onNext(Void aVoid) {
            view.finishProgress();
        }
    }

    public interface Listener {
        void onCheckChanged(LoaderCheckBox v, boolean isChecked);
    }
}
