package gor.alaverdyan.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private String selectedCategory = "";
    private String selectedDifficulty = "Easy";
    private LinearLayout difficultySection;
    private Button btnEasy, btnMed, btnHard, btnStart;
    private MaterialCardView lastSelectedCard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultySection = findViewById(R.id.difficultySection);
        btnStart = findViewById(R.id.btnStart);
        btnEasy = findViewById(R.id.btnEasy);
        btnMed = findViewById(R.id.btnMed);
        btnHard = findViewById(R.id.btnHard);

        MaterialCardView cardMath = findViewById(R.id.cardMath);
        MaterialCardView cardChemistry = findViewById(R.id.cardChemistry);
        MaterialCardView cardHistory = findViewById(R.id.cardHistory);
        MaterialCardView cardSport = findViewById(R.id.cardSport);

        cardMath.setOnClickListener(v -> selectCategory(cardMath, "Math"));
        cardChemistry.setOnClickListener(v -> selectCategory(cardChemistry, "Chemistry"));
        cardHistory.setOnClickListener(v -> selectCategory(cardHistory, "History"));
        cardSport.setOnClickListener(v -> selectCategory(cardSport, "Sport"));

        btnEasy.setOnClickListener(v -> updateDifficulty("Easy", btnEasy));
        btnMed.setOnClickListener(v -> updateDifficulty("Medium", btnMed));
        btnHard.setOnClickListener(v -> updateDifficulty("Hard", btnHard));

        btnStart.setOnClickListener(v -> {
            if (!selectedCategory.isEmpty()) {
                // Note: Changed QuizActivity to GameActivity to match your logic file
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("category", selectedCategory);
                intent.putExtra("difficulty", selectedDifficulty);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a category first", Toast.LENGTH_SHORT).show();
            }
        });

        checkUnlocks();
        setupBottomNavigation();
    }

    private void selectCategory(MaterialCardView card, String category) {

        if (lastSelectedCard != null) {
            lastSelectedCard.setStrokeColor(Color.parseColor("#DDDDDD"));
            lastSelectedCard.setStrokeWidth(2);
        }


        selectedCategory = category;
        card.setStrokeColor(Color.parseColor("#2196F3"));
        card.setStrokeWidth(6);
        lastSelectedCard = card;

        difficultySection.setVisibility(View.VISIBLE);
    }

    private void updateDifficulty(String diff, Button clickedButton) {
        selectedDifficulty = diff;


        btnEasy.setBackgroundColor(Color.LTGRAY);
        btnMed.setBackgroundColor(Color.LTGRAY);
        btnHard.setBackgroundColor(Color.LTGRAY);

        clickedButton.setBackgroundColor(Color.parseColor("#2196F3"));
        clickedButton.setTextColor(Color.WHITE);
    }

    private void checkUnlocks() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("progress")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot data = task.getResult();

                        boolean isMediumUnlocked = data.hasChild("medium_unlocked");
                        boolean isHardUnlocked = data.hasChild("hard_unlocked");

                        btnMed.setEnabled(isMediumUnlocked);
                        btnMed.setAlpha(isMediumUnlocked ? 1.0f : 0.4f);

                        btnHard.setEnabled(isHardUnlocked);
                        btnHard.setAlpha(isHardUnlocked ? 1.0f : 0.4f);
                    } else {

                        btnMed.setEnabled(false);
                        btnHard.setEnabled(false);
                        btnMed.setAlpha(0.4f);
                        btnHard.setAlpha(0.4f);
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_leaderboard) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return id == R.id.nav_home;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUnlocks();
    }
}