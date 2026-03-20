package gor.alaverdyan.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

public class QuizActivity extends AppCompatActivity {

    private RelativeLayout loadingLayout;
    private TextView tvQuestion;
    private LinearLayout optionsContainer;
    private String category, difficulty;

    // Բանալին վերցվում է BuildConfig-ից
    private final String API_KEY = BuildConfig.OPENROUTER_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        loadingLayout = findViewById(R.id.loadingLayout);
        tvQuestion = findViewById(R.id.tvQuestion);
        optionsContainer = findViewById(R.id.optionsContainer);

        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");

        loadQuestionFromAI();
    }

    private void loadQuestionFromAI() {
        if (API_KEY.isEmpty()) {
            tvQuestion.setText("API Key missing in local.properties");
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);
        optionsContainer.removeAllViews();
        tvQuestion.setText("AI is generating question...");

        OkHttpClient client = new OkHttpClient();

        String promptText = "Generate 1 multiple choice quiz question about " + category +
                " for " + difficulty + " level. " +
                "Format: Question|Option1|Option2|Option3|Option4|CorrectIndex(1-4). " +
                "Example: What is 2+2?|3|4|5|6|2. Return ONLY the formatted string.";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "google/gemini-2.0-flash-001"); // Լավագույն մոդելը OpenRouter-ում

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", promptText);
            messages.put(message);

            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("HTTP-Referer", "http://localhost") // Պարտադիր է OpenRouter-ի համար
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String aiResult = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            runOnUiThread(() -> parseAndShow(aiResult));
                        } catch (Exception e) {
                            showError("JSON Parsing Error");
                        }
                    } else {
                        showError("API Error: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            showError("Request Building Error");
        }
    }

    private void parseAndShow(String text) {
        loadingLayout.setVisibility(View.GONE);
        try {
            // Հեռացնում ենք հնարավոր կոդային նշանները
            String cleanedText = text.replace("```", "").replace("json", "").trim();
            String[] parts = cleanedText.split("\\|");

            if (parts.length >= 6) {
                tvQuestion.setText(parts[0].trim());
                int correctIdx = Integer.parseInt(parts[5].replaceAll("[^0-9]", "").trim());

                for (int i = 1; i <= 4; i++) {
                    addOptionButton(parts[i].trim(), i == correctIdx);
                }
            } else {
                loadQuestionFromAI(); // Re-try
            }
        } catch (Exception e) {
            Log.e("OpenRouterError", "Parse failed: " + e.getMessage());
            loadQuestionFromAI();
        }
    }

    private void addOptionButton(String text, boolean isCorrect) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            if (isCorrect) {
                Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong! ❌", Toast.LENGTH_SHORT).show();
            }
            loadQuestionFromAI();
        });
        optionsContainer.addView(btn);
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            loadingLayout.setVisibility(View.GONE);
            tvQuestion.setText(message);
            Log.e("OpenRouterError", message);
        });
    }
}