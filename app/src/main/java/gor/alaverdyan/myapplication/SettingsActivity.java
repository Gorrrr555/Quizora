package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private Button btnRussian, btnEnglish;
    private View btnLogoutLayout, cardChooseAvatar;
    private ImageView ivUserAvatar;
    private TextView tvNickname, tvEmail, tvScore, tvGamesPlayed, tvLogout, tvUserAvatarEmoji;
    private SharedPreferences settingsPref;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private AvatarManager avatarManager;

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
        tvLogout = findViewById(R.id.tvLogout);
        btnLogoutLayout = findViewById(R.id.btnLogoutLayout);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnRussian = findViewById(R.id.btnRussian);
        btnEnglish = findViewById(R.id.btnEnglish);
        
        cardChooseAvatar = findViewById(R.id.cardChooseAvatar);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvUserAvatarEmoji = findViewById(R.id.tvUserAvatarEmoji);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null && avatarManager != null) {
                avatarManager.handleGalleryImage(uri);
            }
        });

        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            avatarManager = new AvatarManager(this, userRef, pickMedia);
            loadUserData();
            cardChooseAvatar.setOnClickListener(v -> {
                if (avatarManager != null) {
                    avatarManager.showAvatarSelectionDialog();
                }
            });
        } else {
            tvNickname.setText(R.string.guest_user);
            tvEmail.setText(getString(R.string.no_account_linked));
            tvScore.setText("0");
            tvGamesPlayed.setText("0");
            tvLogout.setText(R.string.login_or_register);
            cardChooseAvatar.setAlpha(0.5f);
            cardChooseAvatar.setOnClickListener(v -> Toast.makeText(this, R.string.log_in_to_set_avatar, Toast.LENGTH_SHORT).show());
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

        btnLogoutLayout.setOnClickListener(v -> {
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
                    String emoji = snapshot.child("avatarEmoji").getValue(String.class);
                    String base64Image = snapshot.child("avatarUrl").getValue(String.class);

                    tvNickname.setText(nickname != null ? nickname : "User");
                    tvScore.setText(String.valueOf(totalScore != null ? totalScore : 0));
                    tvGamesPlayed.setText(String.valueOf(gamesCount != null ? gamesCount : 0));
                    
                    if (emoji != null && !emoji.isEmpty()) {
                        tvUserAvatarEmoji.setText(emoji);
                        tvUserAvatarEmoji.setVisibility(View.VISIBLE);
                        ivUserAvatar.setVisibility(View.GONE);
                    } else if (base64Image != null && !base64Image.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            ivUserAvatar.setImageBitmap(decodedByte);
                            tvUserAvatarEmoji.setVisibility(View.GONE);
                            ivUserAvatar.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            ivUserAvatar.setImageResource(R.drawable.avatar_placeholder);
                            tvUserAvatarEmoji.setVisibility(View.GONE);
                            ivUserAvatar.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tvUserAvatarEmoji.setVisibility(View.GONE);
                        ivUserAvatar.setVisibility(View.VISIBLE);
                        ivUserAvatar.setImageResource(R.drawable.avatar_placeholder);
                    }
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
