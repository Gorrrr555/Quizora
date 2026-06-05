package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private TextView tvTopNickname, tvSubtitle, tvCoinsCount, tvStreakCount, tvAvatarEmoji;
    private ImageView ivAvatarSmall;
    private MaterialCardView lastSelectedCard = null;
    private BottomNavigationView bottomNav;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private AvatarManager avatarManager;

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
        tvStreakCount = findViewById(R.id.tvStreakCount);
        difficultySection = findViewById(R.id.difficultySection);
        btnStart = findViewById(R.id.btnStart);
        btnEasy = findViewById(R.id.btnEasy);
        btnMed = findViewById(R.id.btnMed);
        btnHard = findViewById(R.id.btnHard);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        ivAvatarSmall = findViewById(R.id.ivAvatarSmall);
        tvAvatarEmoji = findViewById(R.id.tvAvatarEmoji);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null && avatarManager != null) {
                avatarManager.handleGalleryImage(uri);
            }
        });

        MaterialCardView cardMath = findViewById(R.id.cardMath);
        MaterialCardView cardChemistry = findViewById(R.id.cardChemistry);
        MaterialCardView cardHistory = findViewById(R.id.cardHistory);
        MaterialCardView cardSport = findViewById(R.id.cardSport);

        cardMath.setOnClickListener(v -> selectCategory(cardMath, "Math", R.string.math));
        cardChemistry.setOnClickListener(v -> selectCategory(cardChemistry, "Chemistry", R.string.chemistry));
        cardHistory.setOnClickListener(v -> selectCategory(cardHistory, "History", R.string.history));
        cardSport.setOnClickListener(v -> selectCategory(cardSport, "Sport", R.string.sport));

        findViewById(R.id.btnInfoMath).setOnClickListener(v -> showCategoryInfo(
                getString(R.string.math),
                getString(R.string.math_subtitle),
                getString(R.string.math_info),
                "∑",
                ContextCompat.getColor(this, R.color.math_bg),
                ContextCompat.getColor(this, R.color.math_icon)
        ));
        
        findViewById(R.id.btnInfoChemistry).setOnClickListener(v -> showCategoryInfo(
                getString(R.string.chemistry),
                getString(R.string.chemistry_subtitle),
                getString(R.string.chemistry_info),
                "🧪",
                ContextCompat.getColor(this, R.color.chem_bg),
                ContextCompat.getColor(this, R.color.chem_icon)
        ));
        
        findViewById(R.id.btnInfoHistory).setOnClickListener(v -> showCategoryInfo(
                getString(R.string.history),
                getString(R.string.history_subtitle),
                getString(R.string.history_info),
                "📜",
                ContextCompat.getColor(this, R.color.hist_bg),
                ContextCompat.getColor(this, R.color.hist_icon)
        ));
        
        findViewById(R.id.btnInfoSport).setOnClickListener(v -> showCategoryInfo(
                getString(R.string.sport),
                getString(R.string.sport_subtitle),
                getString(R.string.sport_info),
                "🏆",
                ContextCompat.getColor(this, R.color.sport_bg),
                ContextCompat.getColor(this, R.color.sport_icon)
        ));

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

        loadUserInfo();

        View avatarContainer = findViewById(R.id.avatarContainer);
        if (avatarContainer != null) {
            avatarContainer.setOnClickListener(v -> {
                if (avatarManager != null) {
                    avatarManager.showAvatarSelectionDialog();
                } else {
                    Toast.makeText(this, R.string.log_in_to_set_avatar, Toast.LENGTH_SHORT).show();
                }
            });
        }

        checkDailyBonus();
        setupBottomNavigation();
    }

    private void showCategoryInfo(String title, String subtitle, String desc, String emoji, int bgColor, int iconColor) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_category_info, null);
        
        TextView tvTitle = view.findViewById(R.id.tvCategoryTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvCategorySubtitle);
        TextView tvDesc = view.findViewById(R.id.tvCategoryDescription);
        TextView tvEmoji = view.findViewById(R.id.tvCategoryEmoji);
        MaterialCardView cardIcon = view.findViewById(R.id.cardCategoryIcon);
        MaterialButton btnClose = view.findViewById(R.id.btnCloseInfo);

        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        tvSubtitle.setTextColor(iconColor);
        tvDesc.setText(desc);
        tvEmoji.setText(emoji);
        tvEmoji.setTextColor(iconColor);
        cardIcon.setCardBackgroundColor(ColorStateList.valueOf(bgColor));
        btnClose.setBackgroundTintList(ColorStateList.valueOf(iconColor));

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            avatarManager = new AvatarManager(this, userRef, pickMedia);
            userRef.keepSynced(true);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nickname = snapshot.child("nickname").getValue(String.class);
                        Long coins = snapshot.child("quizCoins").getValue(Long.class);
                        Long streak = snapshot.child("streak").getValue(Long.class);
                        String emoji = snapshot.child("avatarEmoji").getValue(String.class);
                        String base64Image = snapshot.child("avatarUrl").getValue(String.class);
                        
                        if (nickname != null) {
                            tvTopNickname.setText(getString(R.string.hello_user, nickname));
                        } else {
                            tvTopNickname.setText(getString(R.string.hello_user, "Explorer"));
                        }
                        
                        if (coins != null) {
                            tvCoinsCount.setText(String.valueOf(coins));
                        } else {
                            tvCoinsCount.setText("0");
                        }

                        if (streak != null) {
                            tvStreakCount.setText(String.valueOf(streak));
                        } else {
                            tvStreakCount.setText("0");
                        }

                        if (emoji != null && !emoji.isEmpty()) {
                            if (tvAvatarEmoji != null) {
                                tvAvatarEmoji.setText(emoji);
                                tvAvatarEmoji.setVisibility(View.VISIBLE);
                            }
                            if (ivAvatarSmall != null) ivAvatarSmall.setVisibility(View.GONE);
                        } else if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                if (ivAvatarSmall != null) {
                                    ivAvatarSmall.setImageBitmap(decodedByte);
                                    ivAvatarSmall.setVisibility(View.VISIBLE);
                                }
                                if (tvAvatarEmoji != null) tvAvatarEmoji.setVisibility(View.GONE);
                            } catch (Exception e) {
                                if (ivAvatarSmall != null) {
                                    ivAvatarSmall.setImageResource(R.drawable.avatar_placeholder);
                                    ivAvatarSmall.setVisibility(View.VISIBLE);
                                }
                                if (tvAvatarEmoji != null) tvAvatarEmoji.setVisibility(View.GONE);
                            }
                        } else {
                            if (tvAvatarEmoji != null) tvAvatarEmoji.setVisibility(View.GONE);
                            if (ivAvatarSmall != null) {
                                ivAvatarSmall.setVisibility(View.VISIBLE);
                                ivAvatarSmall.setImageResource(R.drawable.avatar_placeholder);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            tvTopNickname.setText(getString(R.string.hello_user, getString(R.string.guest_user)));
            tvCoinsCount.setText("0");
            tvStreakCount.setText("0");
            if (tvAvatarEmoji != null) tvAvatarEmoji.setVisibility(View.GONE);
            if (ivAvatarSmall != null) {
                ivAvatarSmall.setVisibility(View.VISIBLE);
                ivAvatarSmall.setImageResource(R.drawable.avatar_placeholder);
            }
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
                            
                            userRef.child("streak").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot streakSnapshot) {
                                    Long currentStreak = streakSnapshot.getValue(Long.class);
                                    if (currentStreak == null) currentStreak = 0L;
                                    userRef.child("streak").setValue(currentStreak + 1);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });

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
            lastSelectedCard.setStrokeWidth(dpToPx(1));
            lastSelectedCard.setCardElevation(dpToPx(2));
        }

        selectedCategory = category;
        card.setStrokeColor(ContextCompat.getColor(this, R.color.primaryBlue));
        card.setStrokeWidth(dpToPx(3));
        card.setCardElevation(dpToPx(8));
        lastSelectedCard = card;

        difficultySection.setVisibility(View.VISIBLE);
        tvSubtitle.setText(getString(stringResId));

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleDifficulty);
        toggleGroup.check(R.id.btnEasy);
        selectedDifficulty = "Easy";

        checkUnlocksForCategory(category);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void checkUnlocksForCategory(String category) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            btnMed.setEnabled(false);
            btnMed.setAlpha(0.4f);
            btnHard.setEnabled(false);
            btnHard.setAlpha(0.4f);
            return;
        }

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
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
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
