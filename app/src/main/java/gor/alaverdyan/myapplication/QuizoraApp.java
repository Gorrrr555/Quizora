package gor.alaverdyan.myapplication;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class QuizoraApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
        }
    }
}
