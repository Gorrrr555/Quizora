package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String selectedCategory = "";
    private String selectedDifficulty = "Easy";
    private LinearLayout difficultySection;
    private Button btnEasy, btnMed, btnHard, btnStart;
    private TextView tvTopNickname, tvSubtitle, tvCoinsCount, tvLeagueName, tvLeagueIcon;
    private MaterialCardView lastSelectedCard = null, cardCoins;
    private BottomNavigationView bottomNav;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTopNickname = findViewById(R.id.tvTopNickname);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvCoinsCount = findViewById(R.id.tvCoinsCount);
        tvLeagueName = findViewById(R.id.tvLeagueName);
        tvLeagueIcon = findViewById(R.id.tvLeagueIcon);
        cardCoins = findViewById(R.id.cardCoins);
        difficultySection = findViewById(R.id.difficultySection);
        btnStart = findViewById(R.id.btnStart);
        btnEasy = findViewById(R.id.btnEasy);
        btnMed = findViewById(R.id.btnMed);
        btnHard = findViewById(R.id.btnHard);
        bottomNav = findViewById(R.id.bottom_navigation);

        MaterialCardView cardMath = findViewById(R.id.cardMath);
        MaterialCardView cardChemistry = findViewById(R.id.cardChemistry);
        MaterialCardView cardHistory = findViewById(R.id.cardHistory);
        MaterialCardView cardSport = findViewById(R.id.cardSport);

        cardMath.setOnClickListener(v -> selectCategory(cardMath, "Math", R.string.math));
        cardChemistry.setOnClickListener(v -> selectCategory(cardChemistry, "Chemistry", R.string.chemistry));
        cardHistory.setOnClickListener(v -> selectCategory(cardHistory, "History", R.string.history));
        cardSport.setOnClickListener(v -> selectCategory(cardSport, "Sport", R.string.sport));

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleDifficulty);
        toggleGroup.check(R.id.btnEasy);
        
        btnEasy.setOnClickListener(v -> selectedDifficulty = "Easy");
        btnMed.setOnClickListener(v -> selectedDifficulty = "Medium");
        btnHard.setOnClickListener(v -> selectedDifficulty = "Hard");

        btnStart.setOnClickListener(v -> {
            if (!selectedCategory.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("category", selectedCategory);
                intent.putExtra("difficulty", selectedDifficulty);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.select_category, Toast.LENGTH_SHORT).show();
            }
        });

        cardCoins.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ShopActivity.class));
        });

        loadUserInfo();
        checkDailyBonus();
        setupBottomNavigation();
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nickname = snapshot.child("nickname").getValue(String.class);
                                Long coins = snapshot.child("quizCoins").getValue(Long.class);
                                String league = snapshot.child("league").getValue(String.class);
                                
                                if (nickname != null) {
                                    tvTopNickname.setText(getString(R.string.hello_user, nickname));
                                }
                                
                                if (coins != null) {
                                    tvCoinsCount.setText(String.valueOf(coins));
                                } else {
                                    tvCoinsCount.setText("0");
                                }

                                if (league != null) {
                                    updateLeagueDisplay(league);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void updateLeagueDisplay(String league) {
        switch (league) {
            case "Bronze":
                tvLeagueName.setText(R.string.league_bronze);
                tvLeagueIcon.setText("🥉");
                break;
            case "Silver":
                tvLeagueName.setText(R.string.league_silver);
                tvLeagueIcon.setText("🥈");
                break;
            case "Gold":
                tvLeagueName.setText(R.string.league_gold);
                tvLeagueIcon.setText("🥇");
                break;
            case "Elite":
                tvLeagueName.setText(R.string.league_elite);
                tvLeagueIcon.setText("💎");
                break;
            default:
                tvLeagueName.setText(R.string.league_bronze);
                tvLeagueIcon.setText("🥉");
        }
    }

    private void checkDailyBonus() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        userRef.child("lastLoginDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastLogin = snapshot.getValue(String.class);
                if (lastLogin == null || !lastLogin.equals(today)) {
                    userRef.child("quizCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot coinSnapshot) {
                            Long currentCoins = coinSnapshot.getValue(Long.class);
                            if (currentCoins == null) currentCoins = 0L;
                            userRef.child("quizCoins").setValue(currentCoins + 20);
                            userRef.child("lastLoginDate").setValue(today);
                            Toast.makeText(MainActivity.this, "Daily login bonus: +20 Coins! 🪙", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void selectCategory(MaterialCardView card, String category, int stringResId) {
        if (lastSelectedCard != null) {
            lastSelectedCard.setStrokeColor(ContextCompat.getColor(this, R.color.cardStroke));
            lastSelectedCard.setStrokeWidth(2);
            lastSelectedCard.setCardElevation(2);
        }

        selectedCategory = category;
        card.setStrokeColor(ContextCompat.getColor(this, R.color.primaryBlue));
        card.setStrokeWidth(8);
        card.setCardElevation(12);
        lastSelectedCard = card;

        difficultySection.setVisibility(View.VISIBLE);
        tvSubtitle.setText(getString(stringResId));

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleDifficulty);
        toggleGroup.check(R.id.btnEasy);
        selectedDifficulty = "Easy";

        checkUnlocksForCategory(category);
    }

    private void checkUnlocksForCategory(String category) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("progress").child(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isMediumUnlocked = snapshot.hasChild("medium_unlocked");
                        boolean isHardUnlocked = snapshot.hasChild("hard_unlocked");

                        btnMed.setEnabled(isMediumUnlocked);
                        btnMed.setAlpha(isMediumUnlocked ? 1.0f : 0.4f);

                        btnHard.setEnabled(isHardUnlocked);
                        btnHard.setAlpha(isHardUnlocked ? 1.0f : 0.4f);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_leaderboard) {
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_home;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        if (!selectedCategory.isEmpty()) {
            checkUnlocksForCategory(selectedCategory);
        }
    }
}
