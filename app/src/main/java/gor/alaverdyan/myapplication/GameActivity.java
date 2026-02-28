package gor.alaverdyan.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private List<Question> questions;
    private int currentIdx = 0;
    private ImageView imgFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        imgFeedback = findViewById(R.id.imgFeedback);
        questions = QuestionBank.getQuestions(getIntent().getStringExtra("CAT"), getIntent().getStringExtra("DIFF"));

        displayQuestion();
    }

    private void displayQuestion() {
        if (currentIdx >= questions.size()) {
            finish();
            return;
        }

        Question q = questions.get(currentIdx);
        ((TextView)findViewById(R.id.txtQuestion)).setText(q.text);

        Button[] btns = {findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2), findViewById(R.id.btnOpt3)};
        for (int i = 0; i < 3; i++) {
            btns[i].setText(q.options[i]);
            btns[i].setEnabled(true);
            final int choice = i;
            btns[i].setOnClickListener(v -> checkAnswer(choice, q.correct, btns));
        }
    }

    private void checkAnswer(int selected, int correct, Button[] btns) {
        for (Button b : btns) b.setEnabled(false);

        if (selected == correct) {
            imgFeedback.setImageResource(android.R.drawable.presence_online);
            imgFeedback.setVisibility(View.VISIBLE);
        } else {
            imgFeedback.setImageResource(android.R.drawable.presence_busy);
            imgFeedback.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(() -> {
            imgFeedback.setVisibility(View.GONE);
            currentIdx++;
            displayQuestion();
        }, 1000);
    }
}