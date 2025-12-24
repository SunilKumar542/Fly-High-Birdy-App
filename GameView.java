package com.example.interactiveanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    public interface ScoreListener {
        void onScoreUpdated(int score);
    }

    private Paint paint;
    private Paint obstaclePaint;
    private Bitmap birdBitmap;
    private Bitmap backgroundBitmap;
    private int score = 0;
    private ScoreListener scoreListener;
    private Random random;

    // Physics and Game State
    private float birdX, birdY;
    private float velocity = 0;
    private float gravity = 2.0f;
    private float jumpStrength = -30f;
    private boolean isGameStarted = false;
    private boolean isGameOver = false;

    // Obstacles (Pipes)
    private List<Pipe> pipes;
    private int pipeWidth = 200;
    private int pipeGap = 500; // Vertical gap between pipes
    private int pipeDistance = 700; // Horizontal distance between pipe sets
    private int pipeSpeed = 15;

    private class Pipe {
        Rect topRect;
        Rect bottomRect;
        boolean passed = false;

        Pipe(int x, int screenHeight, Random r) {
            // Random height for the gap
            int minPipeHeight = 100;
            int maxGapCenter = screenHeight - minPipeHeight - pipeGap / 2;
            int minGapCenter = minPipeHeight + pipeGap / 2;
            int gapCenter = r.nextInt(maxGapCenter - minGapCenter + 1) + minGapCenter;

            topRect = new Rect(x, 0, x + pipeWidth, gapCenter - pipeGap / 2);
            bottomRect = new Rect(x, gapCenter + pipeGap / 2, x + pipeWidth, screenHeight);
        }

        void move(int speed) {
            topRect.offset(-speed, 0);
            bottomRect.offset(-speed, 0);
        }

        boolean isOffScreen() {
            return topRect.right < 0;
        }

        boolean collides(Rect bird) {
            return Rect.intersects(bird, topRect) || Rect.intersects(bird, bottomRect);
        }
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        obstaclePaint = new Paint();
        obstaclePaint.setColor(Color.GREEN); // Typical pipe color
        obstaclePaint.setStyle(Paint.Style.FILL);
        obstaclePaint.setStrokeWidth(5);

        birdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        if (birdBitmap != null) {
            birdBitmap = Bitmap.createScaledBitmap(birdBitmap, 120, 120, true);
        }
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.forest_background);

        random = new Random();
        pipes = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetGame();
    }

    private void resetGame() {
        birdX = getWidth() / 4f;
        birdY = getHeight() / 2f;
        velocity = 0;
        score = 0;
        isGameOver = false;
        isGameStarted = false;
        pipes.clear();
        if (scoreListener != null) scoreListener.onScoreUpdated(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                resetGame();
            } else {
                isGameStarted = true;
                velocity = jumpStrength;
            }
            invalidate();
        }
        return true;
    }

    private void update() {
        if (!isGameStarted || isGameOver) return;

        // Apply gravity
        velocity += gravity;
        birdY += velocity;

        // Ground/Ceiling check
        if (birdY < 0) birdY = 0; // Don't fly above screen
        if (birdY > getHeight() - (birdBitmap != null ? birdBitmap.getHeight() : 0)) {
            isGameOver = true;
        }

        // Manage Pipes
        if (pipes.isEmpty() || pipes.get(pipes.size() - 1).topRect.left < getWidth() - pipeDistance) {
            pipes.add(new Pipe(getWidth(), getHeight(), random));
        }

        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.move(pipeSpeed);

            if (pipe.isOffScreen()) {
                pipes.remove(i);
            }

            // Collision detection
            Rect birdRect = getBirdRect();
            if (pipe.collides(birdRect)) {
                isGameOver = true;
            }

            // Score update
            if (!pipe.passed && pipe.topRect.right < birdX) {
                score++;
                pipe.passed = true;
                if (scoreListener != null) {
                    scoreListener.onScoreUpdated(score);
                }
            }
        }
    }

    private Rect getBirdRect() {
        int w = birdBitmap != null ? birdBitmap.getWidth() : 50;
        int h = birdBitmap != null ? birdBitmap.getHeight() : 50;
        // Make collision box slightly smaller than image for better feel
        return new Rect((int) birdX + 10, (int) birdY + 10, (int) birdX + w - 10, (int) birdY + h - 10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw Background
        if (backgroundBitmap != null) {
            Rect destRect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(backgroundBitmap, null, destRect, null);
        }

        // Update game logic
        update();

        // Draw Pipes
        for (Pipe pipe : pipes) {
            canvas.drawRect(pipe.topRect, obstaclePaint);
            canvas.drawRect(pipe.bottomRect, obstaclePaint);
            
            // Optional: Draw a border for pipes
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.BLACK);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(5);
            canvas.drawRect(pipe.topRect, borderPaint);
            canvas.drawRect(pipe.bottomRect, borderPaint);
        }

        // Draw Bird
        if (birdBitmap != null) {
            canvas.drawBitmap(birdBitmap, birdX, birdY, paint);
        }

        // Game Over Text
        if (isGameOver) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(100);
            textPaint.setFakeBoldText(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over", getWidth() / 2f, getHeight() / 2f, textPaint);
            textPaint.setTextSize(50);
            canvas.drawText("Tap to Restart", getWidth() / 2f, getHeight() / 2f + 100, textPaint);
        } else if (!isGameStarted) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(60);
            textPaint.setFakeBoldText(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Tap to Flap", getWidth() / 2f, getHeight() / 2f + 200, textPaint);
        }

        // Force redraw if game is active
        if (isGameStarted && !isGameOver) {
            invalidate();
        }
    }
}
