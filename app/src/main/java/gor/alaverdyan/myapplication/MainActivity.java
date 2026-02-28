package gor.alaverdyan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private String selectedCategory = "";
    private String selectedDifficulty = "Easy";
    private LinearLayout difficultySection;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        difficultySection = findViewById(R.id.difficultySection);

        setupCard(findViewById(R.id.cardMath), "Math");
        setupCard(findViewById(R.id.cardChemistry), "Chemistry");
        setupCard(findViewById(R.id.cardHistory), "History");
        setupCard(findViewById(R.id.cardSport), "Sport");

        findViewById(R.id.btnEasy).setOnClickListener(v -> selectedDifficulty = "Easy");
        findViewById(R.id.btnMed).setOnClickListener(v -> selectedDifficulty = "Medium");
        findViewById(R.id.btnHard).setOnClickListener(v -> selectedDifficulty = "Hard");

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("CAT", selectedCategory);
            intent.putExtra("DIFF", selectedDifficulty);
            startActivity(intent);
        });
    }

    private void setupCard(MaterialCardView card, String cat) {
        card.setOnClickListener(v -> {
            selectedCategory = cat;
            statusText.setText("Selected: " + cat);
            difficultySection.setVisibility(View.VISIBLE);
            resetCards();
            card.setStrokeWidth(5);
            card.setStrokeColor(getResources().getColor(android.R.color.holo_purple));
        });
    }

    private void resetCards() {
        int[] ids = {R.id.cardMath, R.id.cardChemistry, R.id.cardHistory, R.id.cardSport};
        for (int id : ids) {
            MaterialCardView c = findViewById(id);
            if (c != null) c.setStrokeWidth(0);
        }
    }
}