package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkMode;
    private Button btnRussian, btnEnglish, btnLogout;
    private TextView tvNickname, tvEmail, tvScore, tvGamesPlayed;
    private SharedPreferences settingsPref;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        settingsPref = getSharedPreferences("Settings", MODE_PRIVATE);

        tvNickname = findViewById(R.id.tvUserNickname);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvScore = findViewById(R.id.tvTotalScore);
        tvGamesPlayed = findViewById(R.id.tvGamesPlayed);
        btnLogout = findViewById(R.id.btnLogout);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnRussian = findViewById(R.id.btnRussian);
        btnEnglish = findViewById(R.id.btnEnglish);

        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserData();
        }

        boolean isDarkMode = settingsPref.getBoolean("DarkMode", false);
        switchDarkMode.setChecked(isDarkMode);
        
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                settingsPref.edit().putBoolean("DarkMode", isChecked).apply();
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        btnRussian.setOnClickListener(v -> updateLanguage("ru"));
        btnEnglish.setOnClickListener(v -> updateLanguage("en"));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    Long totalScore = snapshot.child("totalScore").getValue(Long.class);
                    Long gamesCount = snapshot.child("gamesPlayed").getValue(Long.class);

                    tvNickname.setText(nickname != null ? nickname : "User");
                    tvScore.setText(String.valueOf(totalScore != null ? totalScore : 0));
                    tvGamesPlayed.setText(String.valueOf(gamesCount != null ? gamesCount : 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLanguage(String lang) {
        LocaleHelper.setLocale(this, lang);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_leaderboard) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_settings;
        });
    }
}
