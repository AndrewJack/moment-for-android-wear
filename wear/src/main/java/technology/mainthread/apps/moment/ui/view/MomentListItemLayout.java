package technology.mainthread.apps.moment.ui.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import technology.mainthread.apps.moment.R;

public class MomentListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;
    private ImageView mCircle;
    private TextView mName;

    public MomentListItemLayout(Context context) {
        this(context, null);
    }

    public MomentListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MomentListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFadedTextAlpha = 40 / 100f;
        mFadedCircleColor = getResources().getColor(R.color.grey);
        mChosenCircleColor = getResources().getColor(R.color.blue);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean b) {
        mCircle.animate().scaleX(1f).scaleY(1f).alpha(1);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
        mName.setAlpha(1f);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        mCircle.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);
    }
}
