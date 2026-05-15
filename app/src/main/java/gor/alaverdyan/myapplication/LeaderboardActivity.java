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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
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
    private MaterialCardView cardMyRank;
    private TextView tvMyRank, tvMyNickname, tvMyScore;
    
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private String currentUid;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        currentUid = FirebaseAuth.getInstance().getUid();

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        podiumContainer = findViewById(R.id.podiumContainer);
        podium1 = findViewById(R.id.podium1);
        podium2 = findViewById(R.id.podium2);
        podium3 = findViewById(R.id.podium3);

        cardMyRank = findViewById(R.id.cardMyRank);
        tvMyRank = findViewById(R.id.tvMyRank);
        tvMyNickname = findViewById(R.id.tvMyNickname);
        tvMyScore = findViewById(R.id.tvMyScore);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>(), this, this::showUserProfile);
        rvLeaderboard.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.keepSynced(true);

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
                            if (user != null && user.nickname != null && !user.nickname.trim().isEmpty() 
                                    && user.totalScore != null && user.totalScore > 0) {
                                user.uid = data.getKey();
                                tempList.add(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + data.getKey(), e);
                        }
                    }
                }
                
                Collections.sort(tempList, (u1, u2) -> Long.compare(u2.totalScore, u1.totalScore));
                
                if (tempList.size() > 10) {
                    allUsers = new ArrayList<>(tempList.subList(0, 10));
                } else {
                    allUsers = tempList;
                }

                updateUI();
                updateMyRank();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    private void showUserProfile(LeaderboardUser user) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_user_profile, (ViewGroup) findViewById(android.R.id.content), false);

        TextView tvNickname = view.findViewById(R.id.tvProfileNickname);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvScore = view.findViewById(R.id.tvProfileScore);
        TextView tvGames = view.findViewById(R.id.tvProfileGames);
        View btnClose = view.findViewById(R.id.btnCloseProfile);

        tvNickname.setText(user.nickname);
        tvEmail.setText(user.email != null ? user.email : "HIDDEN");
        tvScore.setText(String.valueOf(user.totalScore != null ? user.totalScore : 0));
        tvGames.setText(String.valueOf(user.gamesPlayed != null ? user.gamesPlayed : 0));

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
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
            podium1.setOnClickListener(v -> showUserProfile(allUsers.get(0)));
        } else {
            podium1.setVisibility(View.INVISIBLE);
        }

        if (allUsers.size() >= 2) {
            fillPodium(podium2, allUsers.get(1), 2);
            podium2.setVisibility(View.VISIBLE);
            podium2.setOnClickListener(v -> showUserProfile(allUsers.get(1)));
        } else {
            podium2.setVisibility(View.INVISIBLE);
        }

        if (allUsers.size() >= 3) {
            fillPodium(podium3, allUsers.get(2), 3);
            podium3.setVisibility(View.VISIBLE);
            podium3.setOnClickListener(v -> showUserProfile(allUsers.get(2)));
        } else {
            podium3.setVisibility(View.INVISIBLE);
        }

        adapter.updateData(allUsers);
    }

    private void updateMyRank() {
        if (currentUid == null || allUsers.isEmpty()) {
            cardMyRank.setVisibility(View.GONE);
            return;
        }

        int myRank = -1;
        LeaderboardUser currentUser = null;
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).uid != null && allUsers.get(i).uid.equals(currentUid)) {
                myRank = i + 1;
                currentUser = allUsers.get(i);
                break;
            }
        }

        if (myRank != -1 && currentUser != null) {
            cardMyRank.setVisibility(View.VISIBLE);
            tvMyRank.setText(String.valueOf(myRank));
            tvMyNickname.setText(currentUser.nickname != null ? currentUser.nickname : getString(R.string.you));
            tvMyScore.setText(getString(R.string.points_format, currentUser.totalScore));
            
            int myRankColor = ContextCompat.getColor(this, myRank <= 3 ? R.color.primaryBlue : R.color.primaryBlueVariant);
            cardMyRank.setCardBackgroundColor(ColorStateList.valueOf(myRankColor));
            
            LeaderboardUser finalCurrentUser = currentUser;
            cardMyRank.setOnClickListener(v -> showUserProfile(finalCurrentUser));
        } else {
            cardMyRank.setVisibility(View.GONE);
        }
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
            tvScore.setText(getString(R.string.points_format, p));
        }
        if (tvRank != null) tvRank.setText("#" + rank);

        if (cardPodiumStep != null) {
            int color;
            int heightDp;
            switch (rank) {
                case 1:
                    color = Color.parseColor("#FFD700"); 
                    heightDp = 150;
                    break;
                case 2:
                    color = Color.parseColor("#E2E8F0"); 
                    heightDp = 120;
                    break;
                case 3:
                    color = Color.parseColor("#FDBA74"); 
                    heightDp = 95;
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
            
            if (tvScore != null) {
                tvScore.setTextColor(Color.WHITE);
                tvScore.setShadowLayer(4, 0, 2, Color.BLACK);
            }
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
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(LeaderboardActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
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
