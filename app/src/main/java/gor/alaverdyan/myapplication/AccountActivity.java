package gor.alaverdyan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        TextView tvNickname = findViewById(R.id.tvUserNickname);
        TextView tvEmail = findViewById(R.id.tvUserEmail);
        TextView tvScore = findViewById(R.id.tvTotalScore);
        Button btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvNickname.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Nickname");
            tvEmail.setText(user.getEmail());

            // Fetch Score from Database
            FirebaseDatabase.getInstance().getReference("leaderboard")
                    .child(user.getUid()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            tvScore.setText("Total Score: " + task.getResult().getValue().toString());
                        }
                    });
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}