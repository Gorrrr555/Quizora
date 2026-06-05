package gor.alaverdyan.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.os.Looper;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_splash);
        } catch (Exception e) {
            android.util.Log.e("SplashActivity", "Layout inflation failed", e);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finish();
            } catch (Exception e) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }, 1500);
    }
}
