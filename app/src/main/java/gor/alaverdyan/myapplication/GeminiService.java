package gor.alaverdyan.myapplication;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiService {
    private final OkHttpClient client;
    private final String API_KEY = BuildConfig.OPENROUTER_API_KEY;

    public GeminiService() {
        this.client = new OkHttpClient();
    }

    public interface AIResponseCallback {
        void onSuccess(List<Question> questions);
        void onError(String errorMessage);
    }

    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    public void generateQuestions(String category, String difficulty, AIResponseCallback callback) {
        String prompt = "Give me 1 Armenian quiz question about " + category + " for " + difficulty + " difficulty. " +
                "Response MUST be ONLY a JSON object: {\"q\":\"Հարց\", \"o1\":\"v1\", \"o2\":\"v2\", \"o3\":\"v3\", \"o4\":\"v4\", \"a\":1}. " +
                "The 'a' field is the index of correct option (1-4).";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "google/gemini-2.0-flash-001");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
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
                    .addHeader("HTTP-Referer", "http://localhost")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String aiResult = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            String cleanedJson = aiResult.replaceAll("(?s)```.*?```", "").trim();
                            if (cleanedJson.contains("{")) {
                                cleanedJson = cleanedJson.substring(cleanedJson.indexOf("{"), cleanedJson.lastIndexOf("}") + 1);
                            }

                            JSONObject obj = new JSONObject(cleanedJson);
                            List<Question> questions = new ArrayList<>();
                            questions.add(new Question(
                                    obj.getString("q"),
                                    obj.getString("o1"),
                                    obj.getString("o2"),
                                    obj.getString("o3"),
                                    obj.getString("o4"),
                                    obj.getInt("a")
                            ));
                            callback.onSuccess(questions);

                        } catch (Exception e) {
                            Log.e("GeminiService", "Parse error: " + e.getMessage());
                            callback.onError("JSON Parsing Error");
                        }
                    } else {
                        callback.onError("API Error: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request error: " + e.getMessage());
        }
    }

    public void askAi(String userPrompt, ChatCallback callback) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "google/gemini-2.0-flash-001");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", userPrompt);
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
                    .addHeader("HTTP-Referer", "http://localhost")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String aiResult = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            callback.onSuccess(aiResult);
                        } catch (Exception e) {
                            callback.onError("Parsing Error");
                        }
                    } else {
                        callback.onError("API Error");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Error");
        }
    }
}
