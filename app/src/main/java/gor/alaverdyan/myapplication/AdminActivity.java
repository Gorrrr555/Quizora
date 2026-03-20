package gor.alaverdyan.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private DatabaseReference db;
    private GeminiService geminiService;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseDatabase.getInstance().getReference("questions");
        geminiService = new GeminiService();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Հարցերը ստեղծվում են...");
        progressDialog.setCancelable(false);

        Button btnFill = findViewById(R.id.btnFill);
        btnFill.setOnClickListener(v -> {
            progressDialog.show();
            fillDatabase("General Knowledge");
        });
    }

    private void fillDatabase(String category) {
        geminiService.generateQuestions(category, "Medium", new GeminiService.AIResponseCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                for (Question q : questions) db.push().setValue(q);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminActivity.this, "Հարցերը ավելացվեցին!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminActivity.this, "Սխալ: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}