package gor.alaverdyan.myapplication;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.database.FirebaseDatabase;

public class QuizoraApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            SharedPreferences settingsPref = getSharedPreferences("Settings", MODE_PRIVATE);
            boolean isDarkMode = settingsPref.getBoolean("DarkMode", false);
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } catch (Exception ignored) {}
    }
}
