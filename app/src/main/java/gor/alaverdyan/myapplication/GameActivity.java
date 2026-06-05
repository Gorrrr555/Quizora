package gor.alaverdyan.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
    private TextView tvLoadingTitle, tvLoadingSubtitle, tvSummaryCorrect, tvSummaryPoints, tvEarnedTitle, tvSummaryCoins;
    private LinearProgressIndicator questionProgress;
    private LinearLayout optionsContainer;
    private MaterialCardView cardSolution, cardNewTitle, cardLoadingBrain;
    private MaterialButton btnNext, btnViewLeaderboard, btnBackToMenu;
    private ImageButton btnBack;

    private String category, difficulty;
    private int score = 0;
    private int coinsEarned = 0;
    private int questionIndex = 1;
    private int correctAnswersCount = 0;
    private CountDownTimer countDownTimer;
    private final String API_KEY = BuildConfig.OPENROUTER_API_KEY;

    private List<View> optionViews = new ArrayList<>();
    private int currentCorrectIdx = -1;
    private String currentExplanation = "";

    private List<String> askedQuestions = new ArrayList<>();

    private String[] loadingMessages;
    private final Handler loadingHandler = new Handler(Looper.getMainLooper());
    private Runnable loadingRunnable;
    private ObjectAnimator brainPulseAnimator;

    private boolean isQuestionLoading = false;

    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initViews();
        setupListeners();

        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");
        loadingMessages = getResources().getStringArray(R.array.loading_messages);

        loadQuestion();
    }

    private void initViews() {
        loadingLayout = findViewById(R.id.loadingLayout);
        finishLayout = findViewById(R.id.finishLayout);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvSolution = findViewById(R.id.tvSolution);
        tvLoadingTitle = findViewById(R.id.tvLoadingTitle);
        tvLoadingSubtitle = findViewById(R.id.tvLoadingSubtitle);
        tvSummaryCorrect = findViewById(R.id.tvSummaryCorrect);
        tvSummaryPoints = findViewById(R.id.tvSummaryPoints);
        tvSummaryCoins = findViewById(R.id.tvSummaryCoins);
        tvEarnedTitle = findViewById(R.id.tvEarnedTitle);
        questionProgress = findViewById(R.id.questionProgress);
        optionsContainer = findViewById(R.id.optionsContainer);
        cardSolution = findViewById(R.id.cardSolution);
        cardNewTitle = findViewById(R.id.cardNewTitle);
        cardLoadingBrain = findViewById(R.id.cardLoadingBrain);
        btnNext = findViewById(R.id.btnNext);
        btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            questionIndex++;
            loadQuestion();
        });

        btnViewLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(GameActivity.this, LeaderboardActivity.class));
            finish();
        });

        btnBackToMenu.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void startLoadingAnimation() {
        if (loadingLayout.getVisibility() == View.VISIBLE) return;

        loadingLayout.setVisibility(View.VISIBLE);
        loadingLayout.setAlpha(0f);
        loadingLayout.animate().alpha(1f).setDuration(500).start();
        
        if (brainPulseAnimator == null) {
            brainPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    cardLoadingBrain,
                    PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.05f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.05f)
            );
            brainPulseAnimator.setDuration(1000);
            brainPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            brainPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            brainPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        if (!brainPulseAnimator.isRunning()) brainPulseAnimator.start();

        if (loadingRunnable == null) {
            loadingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (loadingLayout.getVisibility() == View.VISIBLE && loadingMessages != null && loadingMessages.length > 0) {
                        String msg = loadingMessages[new Random().nextInt(loadingMessages.length)];
                        tvLoadingSubtitle.setText(msg);
                        loadingHandler.postDelayed(this, 3000);
                    }
                }
            };
        }
        loadingHandler.removeCallbacks(loadingRunnable);
        loadingHandler.post(loadingRunnable);
    }

    private void stopLoadingAnimation() {
        if (brainPulseAnimator != null) brainPulseAnimator.cancel();
        if (loadingRunnable != null) loadingHandler.removeCallbacks(loadingRunnable);

        loadingLayout.animate().alpha(0f).setDuration(400).withEndAction(() -> {
            loadingLayout.setVisibility(View.GONE);
        }).start();
    }

    private void loadQuestion() {
        if (questionIndex > 10) {
            handleGameOver();
            return;
        }
        if (isQuestionLoading) return;
        isQuestionLoading = true;

        if (countDownTimer != null) countDownTimer.cancel();
        tvTimer.setTextColor(ContextCompat.getColor(this, R.color.primaryBlue));

        startLoadingAnimation();
        optionsContainer.removeAllViews();
        optionViews.clear();
        cardSolution.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        
        tvQuestionCount.setText(questionIndex + " / 10");
        tvScore.setText(String.valueOf(score));
        questionProgress.setProgress(questionIndex * 10);

        String currentLang = LocaleHelper.getLanguage(this);
        String targetLanguage = currentLang.equalsIgnoreCase("ru") ? "Russian" : "English";
        String exclusion = askedQuestions.isEmpty() ? "" : ". Do NOT ask any of these: " + TextUtils.join(", ", askedQuestions);
        
        String prompt = "Generate exactly one " + difficulty + " quiz question about " + category + " in " + targetLanguage + ". " +
                "You MUST follow this format strictly: Question|Option1|Option2|Option3|Option4|CorrectIndex(1-4)|ShortExplanation. " +
                "Do NOT include markdown, backticks, or any introductory text. Just the raw string. " +
                exclusion + ". Seed: " + System.currentTimeMillis();

        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("model", "google/gemini-2.0-flash-lite-001");
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        isQuestionLoading = false;
                        stopLoadingAnimation();
                        tvQuestion.setText(R.string.error_transmission);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String res = response.isSuccessful() && response.body() != null ? response.body().string() : null;
                    runOnUiThread(() -> {
                        isQuestionLoading = false;
                        if (res != null) {
                            try {
                                String content = new JSONObject(res).getJSONArray("choices")
                                        .getJSONObject(0).getJSONObject("message").getString("content");
                                content = content.replace("```", "").replace("markdown", "").trim();
                                retryCount = 0;
                                parseAndShow(content);
                                startTimer();
                            } catch (Exception e) { 
                                handleRetry();
                            }
                        } else {
                            stopLoadingAnimation();
                            tvQuestion.setText(R.string.error_transmission);
                        }
                    });
                }
            });
        } catch (Exception e) { 
            isQuestionLoading = false;
            stopLoadingAnimation();
        }
    }

    private void handleRetry() {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            new Handler(Looper.getMainLooper()).postDelayed(this::loadQuestion, 1500);
        } else {
            stopLoadingAnimation();
            tvQuestion.setText(R.string.error_transmission);
            retryCount = 0;
        }
    }

    private void startTimer() {
        final long timeLimit = difficulty.equalsIgnoreCase("easy") ? 15000 : (difficulty.equalsIgnoreCase("medium") ? 30000 : 45000);
        countDownTimer = new CountDownTimer(timeLimit, 50) {
            @Override
            public void onTick(long millis) {
                tvTimer.setText(String.valueOf((int) (millis / 1000)));
                if (millis < 5000) {
                    tvTimer.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.secondaryColor));
                    if (millis % 1000 < 50) pulseTimer();
                }
            }
            @Override
            public void onFinish() {
                tvTimer.setText("0");
                showResults(-1);
            }
        }.start();
    }

    private void pulseTimer() {
        ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.timerRing),
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.1f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.1f, 1.0f)).setDuration(300).start();
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
                currentExplanation = (p.length >= 7) ? p[6] : "Analysis suggests this is the optimal choice.";
                char[] letters = {'A', 'B', 'C', 'D'};
                for (int i = 1; i <= 4; i++) {
                    final int current = i;
                    View view = LayoutInflater.from(this).inflate(R.layout.item_option, optionsContainer, false);
                    ((TextView) view.findViewById(R.id.tvOptionLetter)).setText(String.valueOf(letters[i-1]));
                    ((TextView) view.findViewById(R.id.tvOptionText)).setText(p[i].trim());
                    view.setOnClickListener(v -> showResults(current));
                    view.setAlpha(0f);
                    view.setTranslationY(dpToPx(40));
                    optionsContainer.addView(view);
                    optionViews.add(view);
                    view.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(i * 100).setInterpolator(new OvershootInterpolator(1.1f)).start();
                }
            } catch (Exception e) { handleRetry(); }
        } else { handleRetry(); }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void showResults(int selectedIdx) {
        if (countDownTimer != null) countDownTimer.cancel();
        disableOptions();
        for (int i = 0; i < optionViews.size(); i++) {
            int humanIdx = i + 1;
            View view = optionViews.get(i);
            MaterialCardView card = view.findViewById(R.id.optionCard);
            MaterialCardView letterCard = view.findViewById(R.id.letterCard);
            TextView letterText = view.findViewById(R.id.tvOptionLetter);
            TextView optionText = view.findViewById(R.id.tvOptionText);
            ImageView statusIcon = view.findViewById(R.id.ivOptionStatus);
            if (humanIdx == currentCorrectIdx) {
                view.setVisibility(View.VISIBLE);
                view.animate().scaleX(1.02f).scaleY(1.02f).setDuration(400).setInterpolator(new OvershootInterpolator()).start();
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.successLight));
                card.setStrokeColor(ContextCompat.getColor(this, R.color.successText));
                card.setStrokeWidth(dpToPx(2));
                letterCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.successText));
                letterText.setTextColor(Color.WHITE);
                optionText.setTextColor(ContextCompat.getColor(this, R.color.successText));
                statusIcon.setImageResource(R.drawable.ic_check_circle); 
                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.successText));
                statusIcon.setVisibility(View.VISIBLE);
            } else if (humanIdx == selectedIdx) {
                shakeView(view);
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.errorLight));
                card.setStrokeColor(ContextCompat.getColor(this, R.color.errorText));
                card.setStrokeWidth(dpToPx(2));
                letterCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.errorText));
                letterText.setTextColor(Color.WHITE);
                optionText.setTextColor(ContextCompat.getColor(this, R.color.errorText));
                statusIcon.setImageResource(R.drawable.ic_error); 
                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.errorText));
                statusIcon.setVisibility(View.VISIBLE);
            } else {
                view.animate().alpha(0.4f).scaleX(0.95f).scaleY(0.95f).setDuration(400).start();
            }
        }
        if (selectedIdx == currentCorrectIdx) {
            score += 10; coinsEarned += 2; correctAnswersCount++;
            tvScore.setText(String.valueOf(score));
        } else {
            tvSolution.setText(currentExplanation);
            cardSolution.setVisibility(View.VISIBLE);
            cardSolution.setAlpha(0f);
            cardSolution.setTranslationY(dpToPx(20));
            cardSolution.animate().alpha(1f).translationY(0).setDuration(500).start();
        }
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setAlpha(0f);
        btnNext.setTranslationY(dpToPx(30));
        btnNext.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(300).setInterpolator(new OvershootInterpolator()).start();
    }

    private void shakeView(View view) {
        ObjectAnimator.ofFloat(view, "translationX", 0, 20, -20, 20, -20, 10, -10, 0).setDuration(500).start();
    }

    private void disableOptions() {
        for (View v : optionViews) v.setEnabled(false);
    }

    private void handleGameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        if (correctAnswersCount == 10) coinsEarned += 10;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long newTotal = (snapshot.child("totalScore").getValue(Long.class) != null ? snapshot.child("totalScore").getValue(Long.class) : 0L) + score;
                    userRef.child("totalScore").setValue(newTotal);
                    userRef.child("gamesPlayed").setValue((snapshot.child("gamesPlayed").getValue(Long.class) != null ? snapshot.child("gamesPlayed").getValue(Long.class) : 0L) + 1);
                    userRef.child("quizCoins").setValue((snapshot.child("quizCoins").getValue(Long.class) != null ? snapshot.child("quizCoins").getValue(Long.class) : 0L) + coinsEarned);
                    FirebaseDatabase.getInstance().getReference("leaderboard").child(uid).setValue(newTotal);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        tvEarnedTitle.setText(getUniqueTitle(category, difficulty));
        finishLayout.setVisibility(View.VISIBLE);
        finishLayout.setAlpha(0f);
        finishLayout.animate().alpha(1f).setDuration(800).start();
        tvSummaryCorrect.setText(correctAnswersCount + "/10");
        tvSummaryPoints.setText("+" + score);
        tvSummaryCoins.setText("+" + coinsEarned);
    }

    private String getUniqueTitle(String category, String difficulty) {
        int resId = R.string.title_conqueror;
        if (category.equals("Math")) resId = difficulty.equals("Easy") ? R.string.title_math_easy : (difficulty.equals("Medium") ? R.string.title_math_medium : R.string.title_math_hard);
        else if (category.equals("Chemistry")) resId = difficulty.equals("Easy") ? R.string.title_chem_easy : (difficulty.equals("Medium") ? R.string.title_chem_medium : R.string.title_chem_hard);
        else if (category.equals("History")) resId = difficulty.equals("Easy") ? R.string.title_hist_easy : (difficulty.equals("Medium") ? R.string.title_hist_medium : R.string.title_hist_hard);
        else if (category.equals("Sport")) resId = difficulty.equals("Easy") ? R.string.title_sport_easy : (difficulty.equals("Medium") ? R.string.title_sport_medium : R.string.title_sport_hard);
        return getString(resId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLoadingAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        loadingHandler.removeCallbacksAndMessages(null);
    }
}
