package gor.alaverdyan.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Եթե այս տողից հետո է crash լինում, ուրեմն խնդիրը XML-ի մեջ է
        setContentView(R.layout.activity_settings);
    }
}