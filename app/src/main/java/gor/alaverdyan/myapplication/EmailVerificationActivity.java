package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView verificationMessage;
    private Button btnCheckVerification, btnResendVerification, btnBackToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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
        }

        btnCheckVerification.setOnClickListener(v -> checkEmailVerificationStatus());
        btnResendVerification.setOnClickListener(v -> resendVerificationEmail());
        btnBackToLogin.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void checkEmailVerificationStatus() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            currentUser.reload().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(EmailVerificationActivity.this, "Email not yet verified.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void resendVerificationEmail() {
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerificationActivity.this, "Verification email sent.", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
