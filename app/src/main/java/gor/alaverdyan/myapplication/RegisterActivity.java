package gor.alaverdyan.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        EditText etNickname = findViewById(R.id.etRegNickname);
        EditText etEmail = findViewById(R.id.etRegEmail);
        EditText etPassword = findViewById(R.id.etRegPassword);
        Button btnRegister = findViewById(R.id.btnRegisterNow);

        btnRegister.setOnClickListener(v -> {
            String name = etNickname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "Լրացրեք բոլոր դաշտերը ճիշտ", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        });
                    }
                } else {
                    Toast.makeText(this, "Սխալ՝ " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}