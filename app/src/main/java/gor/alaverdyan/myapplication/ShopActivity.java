package gor.alaverdyan.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShopActivity extends AppCompatActivity {

    private TextView tvShopCoins;
    private MaterialButton btnBuyHint;
    private ImageButton btnBack;
    private long currentCoins = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        tvShopCoins = findViewById(R.id.tvShopCoins);
        btnBuyHint = findViewById(R.id.btnBuyHint);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadCoins();

        btnBuyHint.setOnClickListener(v -> {
            if (currentCoins >= 50) {
                deductCoins(50, "hint");
            } else {
                Toast.makeText(this, "Not enough QuizCoins! 🪙", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCoins() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid).child("quizCoins")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long coins = snapshot.getValue(Long.class);
                            if (coins != null) {
                                currentCoins = coins;
                                tvShopCoins.setText(String.valueOf(currentCoins));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void deductCoins(int amount, String itemType) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("quizCoins").setValue(currentCoins - amount)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (itemType.equals("hint")) {
                            updateInventory("hints");
                        }
                        Toast.makeText(ShopActivity.this, "Purchase successful! 🎉", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShopActivity.this, "Transaction failed. Try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateInventory(String item) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference inventoryRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("inventory").child(item);
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long count = snapshot.getValue(Long.class);
                if (count == null) count = 0L;
                inventoryRef.setValue(count + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
