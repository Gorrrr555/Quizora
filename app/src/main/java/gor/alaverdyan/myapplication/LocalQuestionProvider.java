package gor.alaverdyan.myapplication;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalQuestionProvider {

    private static JSONObject questionsJson;

    private static void loadJson(Context context) {
        if (questionsJson != null) return;
        try {
            InputStream is = context.getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            questionsJson = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Question> getQuestions(Context context, String category, String difficulty, String lang, int count) {
        loadJson(context);
        List<Question> result = new ArrayList<>();
        try {
            if (questionsJson == null) return result;
            
            String language = lang.equalsIgnoreCase("ru") ? "ru" : "en";
            if (!questionsJson.has(language)) language = "en";
            
            JSONObject langObj = questionsJson.getJSONObject(language);
            if (!langObj.has(category)) return result;
            
            JSONObject catObj = langObj.getJSONObject(category);
            if (!catObj.has(difficulty)) return result;
            
            JSONArray qArray = catObj.getJSONArray(difficulty);
            
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < qArray.length(); i++) indices.add(i);
            Collections.shuffle(indices);
            
            int limit = Math.min(count, qArray.length());
            for (int i = 0; i < limit; i++) {
                JSONObject qObj = qArray.getJSONObject(indices.get(i));
                JSONArray optionsArray = qObj.getJSONArray("o");
                String[] options = new String[4];
                for (int j = 0; j < 4; j++) {
                    options[j] = optionsArray.getString(j);
                }
                result.add(new Question(
                    qObj.getString("q"),
                    options,
                    qObj.getInt("a"),
                    qObj.getString("e")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
