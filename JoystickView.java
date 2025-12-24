package com.example.interactiveanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class JoystickView extends View {

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }

    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoystickListener joystickListener;
    private Paint paint;
    private float hatX;
    private float hatY;

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        baseRadius = Math.min(getWidth(), getHeight()) / 4f;
        hatRadius = Math.min(getWidth(), getHeight()) / 8f;
        hatX = centerX;
        hatY = centerY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the base
        paint.setColor(0xFF888888); // Gray
        canvas.drawCircle(centerX, centerY, baseRadius, paint);

        // Draw the hat
        paint.setColor(0xFF444444); // Dark Gray
        canvas.drawCircle(hatX, hatY, hatRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (joystickListener == null) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float displacementX = x - centerX;
                float displacementY = y - centerY;
                float distance = (float) Math.sqrt(displacementX * displacementX + displacementY * displacementY);

                if (distance < baseRadius) {
                    hatX = x;
                    hatY = y;
                    joystickListener.onJoystickMoved(displacementX / baseRadius, displacementY / baseRadius);
                } else {
                    float ratio = baseRadius / distance;
                    hatX = centerX + displacementX * ratio;
                    hatY = centerY + displacementY * ratio;
                    joystickListener.onJoystickMoved(displacementX * ratio / baseRadius, displacementY * ratio / baseRadius);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                hatX = centerX;
                hatY = centerY;
                joystickListener.onJoystickMoved(0, 0);
                invalidate();
                break;
        }
        return true;
    }

    public void setJoystickListener(JoystickListener joystickListener) {
        this.joystickListener = joystickListener;
    }
}
