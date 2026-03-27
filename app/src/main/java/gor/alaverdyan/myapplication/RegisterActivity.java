package gor.alaverdyan.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegNickname, etRegEmail, etRegPassword;
    private Button btnRegisterNow;
    private ProgressBar progressBar;
    private TextView loginTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        etRegNickname = findViewById(R.id.etRegNickname);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegisterNow = findViewById(R.id.btnRegisterNow);
        progressBar = findViewById(R.id.progressBar);
        loginTextView = findViewById(R.id.loginTextView);

        btnRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String nickname = etRegNickname.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        // --- Input Validation ---
        if (nickname.isEmpty()) {
            etRegNickname.setError("Nickname is required!");
            etRegNickname.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etRegEmail.setError("Email is required!");
            etRegEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.setError("Please enter a valid email!");
            etRegEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegPassword.setError("Password is required!");
            etRegPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegPassword.setError("Password should be at least 6 characters long!");
            etRegPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {


                            Map<String, String> userMap = new HashMap<>();
                            userMap.put("nickname", nickname);
                            userMap.put("email", email);

                            mDatabase.child(firebaseUser.getUid()).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            sendVerificationEmail(firebaseUser);
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Failed to save user data: " + dbTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            firebaseUser.delete();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });

                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful. Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_LONG).show();


                        startActivity(new Intent(RegisterActivity.this, EmailVerificationActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to send verification email: " + emailTask.getException().getMessage(),
                                Toast.LENGTH_LONG).show();

                        user.delete();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}