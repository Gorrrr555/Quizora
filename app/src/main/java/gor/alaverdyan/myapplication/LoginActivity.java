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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            currentUser.reload().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Already logged in and verified.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Your email is not verified. Redirecting to verification.",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, EmailVerificationActivity.class));
                        finish();
                    }
                } else {

                    Toast.makeText(LoginActivity.this,
                            "Failed to refresh user data, please log in again: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            });
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(reloadTask -> {
                                progressBar.setVisibility(View.GONE);
                                if (reloadTask.isSuccessful()) {
                                    if (user.isEmailVerified()) {
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Your email is not verified. Redirecting to verification.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(LoginActivity.this, EmailVerificationActivity.class));
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "Failed to refresh user data: " + reloadTask.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}