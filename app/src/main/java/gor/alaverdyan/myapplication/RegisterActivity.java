package gor.alaverdyan.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegNickname, etRegEmail, etRegPassword, etRegConfirmPassword;
    private TextInputLayout tilRegNickname, tilRegEmail, tilRegPassword, tilRegConfirmPassword;
    private Button btnRegisterNow;
    private ProgressBar progressBar;
    private TextView loginTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        tilRegNickname = findViewById(R.id.tilRegNickname);
        tilRegEmail = findViewById(R.id.tilRegEmail);
        tilRegPassword = findViewById(R.id.tilRegPassword);
        tilRegConfirmPassword = findViewById(R.id.tilRegConfirmPassword);

        etRegNickname = findViewById(R.id.etRegNickname);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegisterNow = findViewById(R.id.btnRegisterNow);
        progressBar = findViewById(R.id.progressBar);
        loginTextView = findViewById(R.id.loginTextView);

        btnRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNicknameAndRegister();
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

    private void checkNicknameAndRegister() {
        String nickname = etRegNickname.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        tilRegNickname.setError(null);
        tilRegEmail.setError(null);
        tilRegPassword.setError(null);
        tilRegConfirmPassword.setError(null);

        if (nickname.isEmpty()) {
            tilRegNickname.setError("Nickname is required!");
            etRegNickname.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            tilRegEmail.setError("Email is required!");
            etRegEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegEmail.setError("Please enter a valid email!");
            etRegEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilRegPassword.setError("Password is required!");
            etRegPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            tilRegPassword.setError("Password should be at least 6 characters long!");
            etRegPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilRegConfirmPassword.setError("Passwords do not match!");
            etRegConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mDatabase.orderByChild("nickname").equalTo(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    tilRegNickname.setError("This nickname is already taken!");
                    etRegNickname.requestFocus();
                } else {
                    registerUser(nickname, email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String nickname, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("nickname", nickname);
                            userMap.put("email", email);
                            userMap.put("totalScore", 0L);
                            userMap.put("gamesPlayed", 0L);
                            userMap.put("quizCoins", 100L);

                            mDatabase.child(firebaseUser.getUid()).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            sendVerificationEmail(firebaseUser);
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            firebaseUser.delete();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful. Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, EmailVerificationActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        user.delete();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}
