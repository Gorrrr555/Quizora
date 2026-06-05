package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin, btnGuest, btnTestUser;
    private ProgressBar progressBar;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;

    private static final String TEST_EMAIL = "innovationcampus26@gmail.com";
    private static final String TEST_PASSWORD = "Samsung2026"; 

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGuest = findViewById(R.id.btnGuest);
        btnTestUser = findViewById(R.id.btnTestUser);
        progressBar = findViewById(R.id.progressBar);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGuest.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
        btnTestUser.setOnClickListener(v -> loginWithTestUser());
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
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
                    if (currentUser.isEmailVerified() || (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase(TEST_EMAIL))) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(LoginActivity.this, EmailVerificationActivity.class));
                        finish();
                    }
                } else {
                    mAuth.signOut();
                }
            });
        }
    }

    private void loginWithTestUser() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            mAuth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                                    .addOnCompleteListener(regTask -> {
                                        if (regTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("nickname", "Samsung");
                                                userMap.put("email", TEST_EMAIL);
                                                userMap.put("totalScore", 0L);
                                                userMap.put("gamesPlayed", 0L);
                                                userMap.put("quizCoins", 100L);
                                                userMap.put("streak", 0L);
                                                userMap.put("lastLoginDate", "");

                                                db.setValue(userMap).addOnCompleteListener(dbTask -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                });
                                            }
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, "Access Error: " + regTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Access Error: The Test User password in the app code is incorrect.", Toast.LENGTH_LONG).show();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "System Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required!");
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
                                    if (user.isEmailVerified() || (user.getEmail() != null && user.getEmail().equalsIgnoreCase(TEST_EMAIL))) {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, EmailVerificationActivity.class));
                                        finish();
                                    }
                                } else {
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
