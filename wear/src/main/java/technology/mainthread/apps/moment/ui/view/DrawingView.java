package technology.mainthread.apps.moment.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private final Path path = new Path();
    private final GestureDetector mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        public void onLongPress(MotionEvent ev) {
            listener.onLongPress();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            listener.onDoubleTap();
            return false;
        }
    });

    private DrawingListener listener = DrawingListener.NO_OP;
    private Paint drawPaint;
    private boolean enabled = true;

    public DrawingView(Context context) {
        super(context);
        onPostConstruct();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onPostConstruct();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onPostConstruct();
    }

    private void onPostConstruct() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
    }

    // Setup paint with color and stroke styles
    private void setupPaint() {
        drawPaint = new Paint();
        int paintColor = Color.BLACK;
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setDrawingListener(DrawingListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, drawPaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mDetector.onTouchEvent(event)) {
            return true;
        }

        if (!enabled) {
            return true;
        }

        float pointX = event.getX();
        float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Starts a new line in the path
                listener.onActionDown();
                path.moveTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                // Draws line between last point and this point
                path.lineTo(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                listener.onActionUp();
                break;
            default:
                return false;
        }

        postInvalidate(); // Indicate view should be redrawn
        return true; // Indicate we've consumed the touch
    }

    public Bitmap getDrawing() {
        Bitmap drawing = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(drawing);
        layout(getLeft(), getTop(), getRight(), getBottom());
        draw(canvas);
        return drawing;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        path.reset();
        postInvalidate();
    }

    public static interface DrawingListener {

        DrawingListener NO_OP = new DrawingListener() {
            @Override
            public void onActionDown() {

            }

            @Override
            public void onActionUp() {

            }

            @Override
            public void onLongPress() {

            }

            @Override
            public void onDoubleTap() {

            }
        };

        void onActionDown();

        void onActionUp();

        void onLongPress();

        void onDoubleTap();
    }
}
