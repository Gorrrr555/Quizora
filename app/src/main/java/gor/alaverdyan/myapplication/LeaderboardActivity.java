package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LeaderboardActivity";
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> allUsers = new ArrayList<>();
    private BottomNavigationView bottomNav;

    private View podium1, podium2, podium3, podiumContainer;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        podiumContainer = findViewById(R.id.podiumContainer);
        podium1 = findViewById(R.id.podium1);
        podium2 = findViewById(R.id.podium2);
        podium3 = findViewById(R.id.podium3);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>(), this);
        rvLeaderboard.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupBottomNavigation();
        startDataListener();
    }

    private void startDataListener() {
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LeaderboardUser> tempList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        try {
                            LeaderboardUser user = data.getValue(LeaderboardUser.class);
                            if (user != null) {
                                user.uid = data.getKey();
                                if (user.totalScore == null) user.totalScore = 0L;
                                tempList.add(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + data.getKey(), e);
                        }
                    }
                }
                
                Collections.sort(tempList, (u1, u2) -> Long.compare(u2.totalScore, u1.totalScore));
                
                allUsers = tempList;
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    private void updateUI() {
        if (allUsers.isEmpty()) {
            podiumContainer.setVisibility(View.GONE);
            adapter.updateData(new ArrayList<>());
            return;
        }

        podiumContainer.setVisibility(View.VISIBLE);

        if (allUsers.size() >= 1) {
            fillPodium(podium1, allUsers.get(0), 1);
            podium1.setVisibility(View.VISIBLE);
        } else {
            podium1.setVisibility(View.INVISIBLE);
        }

        if (allUsers.size() >= 2) {
            fillPodium(podium2, allUsers.get(1), 2);
            podium2.setVisibility(View.VISIBLE);
        } else {
            podium2.setVisibility(View.INVISIBLE);
        }

        if (allUsers.size() >= 3) {
            fillPodium(podium3, allUsers.get(2), 3);
            podium3.setVisibility(View.VISIBLE);
        } else {
            podium3.setVisibility(View.INVISIBLE);
        }

        List<LeaderboardUser> recyclerList = new ArrayList<>();
        if (allUsers.size() > 3) {
            recyclerList.addAll(allUsers.subList(3, allUsers.size()));
        }
        adapter.updateData(recyclerList);
    }

    private void fillPodium(View view, LeaderboardUser user, int rank) {
        TextView tvName = view.findViewById(R.id.tvPlayerName);
        TextView tvScore = view.findViewById(R.id.tvPlayerScore);
        TextView tvRank = view.findViewById(R.id.tvRankBadge);
        View cardPodiumStep = view.findViewById(R.id.cardPodiumStep);
        View viewStepTopColor = view.findViewById(R.id.viewStepTopColor);
        MaterialCardView cardRankBadge = view.findViewById(R.id.cardRankBadge);

        if (tvName != null) tvName.setText(user.nickname != null ? user.nickname : "---");
        if (tvScore != null) {
            long p = user.totalScore != null ? user.totalScore : 0;
            tvScore.setText(p + " pts");
        }
        if (tvRank != null) tvRank.setText("#" + rank);

        if (cardPodiumStep != null) {
            int color;
            int heightDp;
            switch (rank) {
                case 1:
                    color = Color.parseColor("#FFD700"); // Gold
                    heightDp = 140;
                    break;
                case 2:
                    color = Color.parseColor("#C0C0C0"); // Silver
                    heightDp = 110;
                    break;
                case 3:
                    color = Color.parseColor("#CD7F32"); // Bronze
                    heightDp = 90;
                    break;
                default:
                    color = Color.GRAY;
                    heightDp = 70;
                    break;
            }

            ViewGroup.LayoutParams params = cardPodiumStep.getLayoutParams();
            params.height = dpToPx(heightDp);
            cardPodiumStep.setLayoutParams(params);

            if (viewStepTopColor != null) viewStepTopColor.setBackgroundColor(color);
            if (cardRankBadge != null) cardRankBadge.setCardBackgroundColor(ColorStateList.valueOf(color));
            if (tvScore != null) tvScore.setTextColor(color);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_leaderboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(LeaderboardActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(LeaderboardActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_leaderboard;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null && usersRef != null) {
            usersRef.removeEventListener(usersListener);
        }
    }
}
