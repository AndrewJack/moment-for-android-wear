package technology.mainthread.apps.moment.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SlidingFrameLayout extends FrameLayout {

    public SlidingFrameLayout(Context context) {
        super(context);
    }

    public SlidingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getXFraction() {
        if (getWidth() == 0) {
            return 0;
        }
        return getX() / getWidth();
    }

    public void setXFraction(float xFraction) {
        final int width = getWidth();
        setX(width > 0 ? xFraction * width : -9999);
    }
}
