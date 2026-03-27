package gor.alaverdyan.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView verificationMessage;
    private Button btnCheckVerification, btnResendVerification, btnBackToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        verificationMessage = findViewById(R.id.verificationMessage);
        btnCheckVerification = findViewById(R.id.btnCheckVerification);
        btnResendVerification = findViewById(R.id.btnResendVerification);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        if (currentUser != null) {
            String email = currentUser.getEmail();
            verificationMessage.setText("A verification email has been sent to " + email + ". Please check your inbox and spam folder to verify your account.");
        } else {
            verificationMessage.setText("It seems you are not logged in. Please go back to login and try again.");
            btnResendVerification.setEnabled(false);
        }

        btnCheckVerification.setOnClickListener(v -> checkEmailVerificationStatus());
        btnResendVerification.setOnClickListener(v -> resendVerificationEmail());
        btnBackToLogin.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEmailVerificationStatus();
    }

    private void checkEmailVerificationStatus() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            currentUser.reload().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(EmailVerificationActivity.this, "Email verified! Redirecting...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EmailVerificationActivity.this, "Email not yet verified. Please verify your email.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EmailVerificationActivity.this, "Failed to refresh verification status: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {

            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void resendVerificationEmail() {
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this, "Verification email sent. Check your inbox and spam.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Failed to send verification email: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(EmailVerificationActivity.this, "No user logged in. Please log in or register.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
            finish();
        }
    }
}