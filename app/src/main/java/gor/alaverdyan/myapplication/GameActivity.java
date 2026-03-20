package gor.alaverdyan.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity {

    private RelativeLayout loadingLayout;
    private TextView tvQuestion, tvScore, tvTimer, tvQuestionCount;
    private LinearLayout optionsContainer;

    private String category, difficulty;
    private int score = 0;
    private int questionIndex = 1;
    private int correctAnswersCount = 0;
    private CountDownTimer countDownTimer;
    private final String API_KEY = BuildConfig.OPENROUTER_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadingLayout = findViewById(R.id.loadingLayout);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        optionsContainer = findViewById(R.id.optionsContainer);

        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");

        loadQuestion();
    }

    private void loadQuestion() {
        if (questionIndex > 10) {
            handleGameOver();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();

        loadingLayout.setVisibility(View.VISIBLE);
        optionsContainer.removeAllViews();
        tvQuestionCount.setText(questionIndex + "/10");
        tvScore.setText("Pts: " + score);

        OkHttpClient client = new OkHttpClient();
        String prompt = "One " + difficulty + " quiz question about " + category +
                " in English. Format: Question|Opt1|Opt2|Opt3|Opt4|Correct(1-4). Seed: " + System.currentTimeMillis();

        try {
            JSONObject json = new JSONObject();
            json.put("model", "google/gemini-2.0-flash-001");
            json.put("temperature", 1.0);
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            json.put("messages", messages);

            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> tvQuestion.setText("Network Error"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String res = response.body().string();
                            String content = new JSONObject(res).getJSONArray("choices")
                                    .getJSONObject(0).getJSONObject("message").getString("content");
                            runOnUiThread(() -> {
                                parseAndShow(content);
                                startTimer();
                            });
                        } catch (Exception e) { runOnUiThread(() -> loadQuestion()); }
                    }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startTimer() {
        long timeLimit;
        if (difficulty.equalsIgnoreCase("easy")) timeLimit = 10000;
        else if (difficulty.equalsIgnoreCase("medium")) timeLimit = 30000;
        else timeLimit = 60000;

        countDownTimer = new CountDownTimer(timeLimit, 1000) {
            @Override
            public void onTick(long millis) {
                tvTimer.setText((millis / 1000) + "s");
                if (millis < 4000) tvTimer.setTextColor(Color.RED);
                else tvTimer.setTextColor(Color.parseColor("#2196F3"));
            }

            @Override
            public void onFinish() {
                Toast.makeText(GameActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                questionIndex++;
                loadQuestion();
            }
        }.start();
    }

    private void parseAndShow(String text) {
        loadingLayout.setVisibility(View.GONE);
        String[] p = text.trim().split("\\|");
        if (p.length >= 6) {
            tvQuestion.setText(p[0]);
            int correctIdx = Integer.parseInt(p[5].replaceAll("[^0-9]", ""));

            for (int i = 1; i <= 4; i++) {
                final int current = i;
                Button btn = new Button(this);
                btn.setText(p[i]);
                btn.setAllCaps(false);
                btn.setBackgroundResource(R.drawable.edit_text_bg);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 15, 0, 15);
                btn.setLayoutParams(params);

                btn.setOnClickListener(v -> {
                    if (current == correctIdx) {
                        score += 10;
                        correctAnswersCount++;
                        Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Wrong! ❌", Toast.LENGTH_SHORT).show();
                    }
                    questionIndex++;
                    loadQuestion();
                });
                optionsContainer.addView(btn);
            }
        } else { loadQuestion(); }
    }

    private void handleGameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("leaderboard").child(uid).setValue(score);

        if (correctAnswersCount == 10) {
            String unlockKey = "";
            if (difficulty.equalsIgnoreCase("easy")) unlockKey = "medium_unlocked";
            else if (difficulty.equalsIgnoreCase("medium")) unlockKey = "hard_unlocked";

            if (!unlockKey.isEmpty()) {
                db.child("users").child(uid).child("progress").child(unlockKey).setValue(true);
                Toast.makeText(this, "Mastered! Next Level Unlocked!", Toast.LENGTH_LONG).show();
            }
        }

        tvQuestion.setText("Finished! Score: " + score);
        optionsContainer.removeAllViews();
        Button btnExit = new Button(this);
        btnExit.setText("Back to Menu");
        btnExit.setOnClickListener(v -> finish());
        optionsContainer.addView(btnExit);
    }
}