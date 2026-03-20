package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_leaderboard);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_account) {
                    // Այստեղ արդեն կան նաև կարգավորումները
                    startActivity(new Intent(this, AccountActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                // nav_settings բլոկը հեռացված է, որպեսզի Build-ը չկանգնի
                return id == R.id.nav_leaderboard;
            });
        }
    }
}