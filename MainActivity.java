package com.example.interactiveanimation;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameView gameView = findViewById(R.id.game_view);
        TextView scoreBoard = findViewById(R.id.score_board);

        gameView.setScoreListener(score -> {
            runOnUiThread(() -> scoreBoard.setText("Score: " + score));
        });
    }
}
