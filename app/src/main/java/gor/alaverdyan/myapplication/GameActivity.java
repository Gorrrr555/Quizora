package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity {

    private RelativeLayout loadingLayout, finishLayout;
    private TextView tvQuestion, tvScore, tvTimer, tvQuestionCount, tvSolution;
    private TextView tvLoadingTitle, tvLoadingSubtitle, tvFinalScore, tvSummaryCorrect, tvSummaryPoints, tvEarnedTitle, tvSummaryCoins;
    private LinearProgressIndicator questionProgress;
    private LinearLayout optionsContainer;
    private MaterialCardView cardSolution, cardNewTitle;
    private MaterialButton btnNext, btnViewLeaderboard, btnBackToMenu;
    private MaterialButton btnFiftyFifty, btnTimeFreeze;

    private String category, difficulty;
    private int score = 0;
    private int coinsEarned = 0;
    private int questionIndex = 1;
    private int correctAnswersCount = 0;
    private CountDownTimer countDownTimer;
    private final String API_KEY = BuildConfig.OPENROUTER_API_KEY;

    private List<MaterialButton> optionButtons = new ArrayList<>();
    private int currentCorrectIdx = -1;
    private String currentExplanation = "";

    private List<String> askedQuestions = new ArrayList<>();

    private String[] loadingMessages;
    private Handler loadingHandler = new Handler(Looper.getMainLooper());
    private Runnable loadingRunnable;

    private boolean isFiftyFiftyUsed = false;
    private boolean isTimeFreezeUsed = false;
    private boolean isTimerFrozen = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadingLayout = findViewById(R.id.loadingLayout);
        finishLayout = findViewById(R.id.finishLayout);
        
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvSolution = findViewById(R.id.tvSolution);
        tvLoadingTitle = findViewById(R.id.tvLoadingTitle);
        tvLoadingSubtitle = findViewById(R.id.tvLoadingSubtitle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvSummaryCorrect = findViewById(R.id.tvSummaryCorrect);
        tvSummaryPoints = findViewById(R.id.tvSummaryPoints);
        tvSummaryCoins = findViewById(R.id.tvSummaryCoins);
        tvEarnedTitle = findViewById(R.id.tvEarnedTitle);
        
        questionProgress = findViewById(R.id.questionProgress);
        optionsContainer = findViewById(R.id.optionsContainer);
        cardSolution = findViewById(R.id.cardSolution);
        cardNewTitle = findViewById(R.id.cardNewTitle);
        
        btnNext = findViewById(R.id.btnNext);
        btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        
        btnFiftyFifty = findViewById(R.id.btnFiftyFifty);
        btnTimeFreeze = findViewById(R.id.btnTimeFreeze);

        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");

        loadingMessages = getResources().getStringArray(R.array.loading_messages);

        btnNext.setOnClickListener(v -> {
            questionIndex++;
            loadQuestion();
        });

        btnViewLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(GameActivity.this, LeaderboardActivity.class));
            finish();
        });

        btnBackToMenu.setOnClickListener(v -> finish());

        setupBonuses();
        loadQuestion();
    }

    private void setupBonuses() {
        btnFiftyFifty.setOnClickListener(v -> {
            if (!isFiftyFiftyUsed && optionButtons.size() == 4) {
                useFiftyFifty();
            }
        });

        btnTimeFreeze.setOnClickListener(v -> {
            if (!isTimeFreezeUsed) {
                useTimeFreeze();
            }
        });
    }

    private void useFiftyFifty() {
        isFiftyFiftyUsed = true;
        btnFiftyFifty.setEnabled(false);
        btnFiftyFifty.setAlpha(0.5f);
        
        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i + 1 != currentCorrectIdx) {
                wrongIndices.add(i);
            }
        }
        Collections.shuffle(wrongIndices);
        
        optionButtons.get(wrongIndices.get(0)).setVisibility(View.INVISIBLE);
        optionButtons.get(wrongIndices.get(1)).setVisibility(View.INVISIBLE);
        
        Toast.makeText(this, "50/50 Activated!", Toast.LENGTH_SHORT).show();
    }

    private void useTimeFreeze() {
        isTimeFreezeUsed = true;
        isTimerFrozen = true;
        btnTimeFreeze.setEnabled(false);
        btnTimeFreeze.setAlpha(0.5f);
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvTimer.setTextColor(Color.CYAN);
        tvTimer.setText("FROZEN");
        
        Toast.makeText(this, "Time is Frozen! ❄️", Toast.LENGTH_SHORT).show();
    }

    private void startLoadingAnimation() {
        loadingLayout.setVisibility(View.VISIBLE);
        loadingLayout.setAlpha(0f);
        loadingLayout.animate().alpha(1f).setDuration(400).start();
        
        loadingRunnable = new Runnable() {
            @Override
            public void run() {
                if (loadingLayout.getVisibility() == View.VISIBLE && loadingMessages != null && loadingMessages.length > 0) {
                    String msg = loadingMessages[new Random().nextInt(loadingMessages.length)];
                    tvLoadingSubtitle.setText(msg);
                    loadingHandler.postDelayed(this, 2000);
                }
            }
        };
        loadingHandler.post(loadingRunnable);
    }

    private void stopLoadingAnimation() {
        loadingLayout.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            loadingLayout.setVisibility(View.GONE);
        }).start();
        
        if (loadingRunnable != null) {
            loadingHandler.removeCallbacks(loadingRunnable);
        }
    }

    private void loadQuestion() {
        if (questionIndex > 10) {
            handleGameOver();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();
        isTimerFrozen = false;
        tvTimer.setTextColor(ContextCompat.getColor(this, R.color.primaryBlue));

        startLoadingAnimation();
        optionsContainer.removeAllViews();
        optionButtons.clear();
        cardSolution.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        
        tvQuestionCount.setText(getString(R.string.question_of, questionIndex));
        tvScore.setText(getString(R.string.score) + ": " + score);
        questionProgress.setProgress(questionIndex * 10);

        String currentLang = LocaleHelper.getLanguage(this);
        String targetLanguage = currentLang.equalsIgnoreCase("ru") ? "Russian" : "English";

        OkHttpClient client = new OkHttpClient();
        String exclusion = askedQuestions.isEmpty() ? "" : ". Do NOT ask any of these: " + TextUtils.join(", ", askedQuestions);
        
        String prompt = "Generate exactly one " + difficulty + " quiz question about " + category + " in " + targetLanguage + ". " +
                "You MUST follow this format strictly: Question|Option1|Option2|Option3|Option4|CorrectIndex(1-4)|ShortExplanation. " +
                "Do NOT include markdown, backticks, or any introductory text. Just the raw string. " +
                exclusion + ". Seed: " + System.currentTimeMillis();

        try {
            JSONObject json = new JSONObject();
            json.put("model", "google/gemini-2.0-flash-001");
            json.put("temperature", 0.7); 
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
                    runOnUiThread(() -> {
                        stopLoadingAnimation();
                        tvQuestion.setText("Network Error");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String res = response.body().string();
                            String content = new JSONObject(res).getJSONArray("choices")
                                    .getJSONObject(0).getJSONObject("message").getString("content");
                            
                            content = content.replace("```", "").replace("markdown", "").trim();
                            
                            final String finalContent = content;
                            runOnUiThread(() -> {
                                parseAndShow(finalContent);
                                if (!isTimerFrozen) startTimer();
                            });
                        } catch (Exception e) { 
                            runOnUiThread(() -> loadQuestion()); 
                        }
                    } else {
                        runOnUiThread(() -> stopLoadingAnimation());
                    }
                }
            });
        } catch (Exception e) { 
            e.printStackTrace();
            stopLoadingAnimation();
        }
    }

    private void startTimer() {
        if (isTimerFrozen) return;

        long timeLimit;
        if (difficulty.equalsIgnoreCase("easy")) timeLimit = 15000;
        else if (difficulty.equalsIgnoreCase("medium")) timeLimit = 30000;
        else timeLimit = 45000;

        countDownTimer = new CountDownTimer(timeLimit, 1000) {
            @Override
            public void onTick(long millis) {
                long sec = millis / 1000;
                tvTimer.setText(String.format("00:%02d", sec));
                if (millis < 5000) tvTimer.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.secondaryColor));
                else tvTimer.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.primaryBlue));
            }

            @Override
            public void onFinish() {
                showResults(-1);
            }
        }.start();
    }

    private void parseAndShow(String text) {
        stopLoadingAnimation();
        String[] p = text.trim().split("\\|");
        if (p.length >= 6) {
            String qText = p[0];
            tvQuestion.setText(qText);
            askedQuestions.add(qText);

            try {
                currentCorrectIdx = Integer.parseInt(p[5].trim().replaceAll("[^0-9]", ""));
                currentExplanation = (p.length >= 7) ? p[6] : "No explanation provided.";

                for (int i = 1; i <= 4; i++) {
                    final int current = i;
                    MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle);
                    btn.setText(p[i].trim());
                    btn.setAllCaps(false);
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.surfaceColor));
                    btn.setStrokeColorResource(R.color.cardStroke);
                    btn.setStrokeWidth(2);
                    btn.setCornerRadius(28);
                    btn.setTextColor(ContextCompat.getColor(this, R.color.textDark));
                    btn.setTextSize(16);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 160);
                    params.setMargins(0, 15, 0, 15);
                    btn.setLayoutParams(params);

                    btn.setOnClickListener(v -> showResults(current));
                    optionsContainer.addView(btn);
                    optionButtons.add(btn);
                }
            } catch (Exception e) {
                loadQuestion(); 
            }
        } else { 
            loadQuestion(); 
        }
    }

    private void showResults(int selectedIdx) {
        if (countDownTimer != null) countDownTimer.cancel();
        disableOptions();

        for (int i = 0; i < optionButtons.size(); i++) {
            int humanIdx = i + 1;
            MaterialButton btn = optionButtons.get(i);
            
            if (humanIdx == currentCorrectIdx) {
                btn.setVisibility(View.VISIBLE);
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.successLight));
                btn.setStrokeColorResource(R.color.successText);
                btn.setTextColor(ContextCompat.getColor(this, R.color.successText));
            } else if (humanIdx == selectedIdx) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.errorLight));
                btn.setStrokeColorResource(R.color.errorText);
                btn.setTextColor(ContextCompat.getColor(this, R.color.errorText));
            }
        }

        if (selectedIdx == currentCorrectIdx) {
            score += 10;
            coinsEarned += 2;
            correctAnswersCount++;
            tvScore.setText(getString(R.string.score) + ": " + score);
            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
        } else {
            tvSolution.setText(currentExplanation);
            cardSolution.setVisibility(View.VISIBLE);
            if (selectedIdx != -1) {
                Toast.makeText(this, "Wrong! ❌", Toast.LENGTH_SHORT).show();
            }
        }
        btnNext.setVisibility(View.VISIBLE);
    }

    private void disableOptions() {
        for (MaterialButton b : optionButtons) {
            b.setEnabled(false);
        }
    }

    private void handleGameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        if (correctAnswersCount == 10) {
            coinsEarned += 10;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long currentTotalScore = snapshot.child("totalScore").getValue(Long.class);
                    Long currentGamesCount = snapshot.child("gamesPlayed").getValue(Long.class);
                    Long currentCoins = snapshot.child("quizCoins").getValue(Long.class);
                    Long currentLeaguePoints = snapshot.child("leaguePoints").getValue(Long.class);
                    String league = snapshot.child("league").getValue(String.class);

                    if (currentTotalScore == null) currentTotalScore = 0L;
                    if (currentGamesCount == null) currentGamesCount = 0L;
                    if (currentCoins == null) currentCoins = 0L;
                    if (currentLeaguePoints == null) currentLeaguePoints = 0L;
                    if (league == null) league = "Bronze";

                    long newTotal = currentTotalScore + score;
                    long newLeaguePoints = currentLeaguePoints + score;
                    
                    userRef.child("totalScore").setValue(newTotal);
                    userRef.child("gamesPlayed").setValue(currentGamesCount + 1);
                    userRef.child("quizCoins").setValue(currentCoins + coinsEarned);
                    userRef.child("leaguePoints").setValue(newLeaguePoints);

                    FirebaseDatabase.getInstance().getReference("leaderboard").child(uid).setValue(newTotal);
                    FirebaseDatabase.getInstance().getReference("leagues").child(league).child(uid).setValue(newLeaguePoints);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        String earnedTitle = getUniqueTitle(category, difficulty);
        tvEarnedTitle.setText(earnedTitle);

        if (correctAnswersCount == 10) {
            String unlockKey = "";
            if (difficulty.equalsIgnoreCase("easy")) unlockKey = "medium_unlocked";
            else if (difficulty.equalsIgnoreCase("medium")) unlockKey = "hard_unlocked";

            if (!unlockKey.isEmpty()) {
                userRef.child("progress").child(category).child(unlockKey).setValue(true);
                Toast.makeText(this, "Mastered " + category + "! Next Level Unlocked!", Toast.LENGTH_LONG).show();
            }
        }

        finishLayout.setVisibility(View.VISIBLE);
        finishLayout.setAlpha(0f);
        finishLayout.animate().alpha(1f).setDuration(500).start();
        
        tvFinalScore.setText(getString(R.string.congratulations));
        tvSummaryCorrect.setText(String.valueOf(correctAnswersCount));
        tvSummaryPoints.setText("+" + score);
        tvSummaryCoins.setText("+" + coinsEarned);
    }

    private String getUniqueTitle(String category, String difficulty) {
        int resId = R.string.title_conqueror;
        if (category.equals("Math")) {
            if (difficulty.equals("Easy")) resId = R.string.title_math_easy;
            else if (difficulty.equals("Medium")) resId = R.string.title_math_medium;
            else resId = R.string.title_math_hard;
        } else if (category.equals("Chemistry")) {
            if (difficulty.equals("Easy")) resId = R.string.title_chem_easy;
            else if (difficulty.equals("Medium")) resId = R.string.title_chem_medium;
            else resId = R.string.title_chem_hard;
        } else if (category.equals("History")) {
            if (difficulty.equals("Easy")) resId = R.string.title_hist_easy;
            else if (difficulty.equals("Medium")) resId = R.string.title_hist_medium;
            else resId = R.string.title_hist_hard;
        } else if (category.equals("Sport")) {
            if (difficulty.equals("Easy")) resId = R.string.title_sport_easy;
            else if (difficulty.equals("Medium")) resId = R.string.title_sport_medium;
            else resId = R.string.title_sport_hard;
        }
        return getString(resId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLoadingAnimation();
    }
}
