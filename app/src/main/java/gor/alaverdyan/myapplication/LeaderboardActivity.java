package gor.alaverdyan.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
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
    private List<LeaderboardUser> filteredList = new ArrayList<>();
    private TabLayout leagueTabs;
    private BottomNavigationView bottomNav;

    private View podium1, podium2, podium3, podiumContainer;
    private String currentSelectedLeague = "Bronze";
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
        leagueTabs = findViewById(R.id.leagueTabs);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        podiumContainer = findViewById(R.id.podiumContainer);
        podium1 = findViewById(R.id.podium1);
        podium2 = findViewById(R.id.podium2);
        podium3 = findViewById(R.id.podium3);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>(), this);
        rvLeaderboard.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupTabs();
        setupBottomNavigation();
        startDataListener();
    }

    private void setupTabs() {
        leagueTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentSelectedLeague = "Bronze"; break;
                    case 1: currentSelectedLeague = "Silver"; break;
                    case 2: currentSelectedLeague = "Gold"; break;
                    case 3: currentSelectedLeague = "Elite"; break;
                }
                Log.d(TAG, "Tab selected: " + currentSelectedLeague);
                filterAndRefreshUI();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
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
                                if (user.leaguePoints == null) user.leaguePoints = 0L;
                                if (user.league == null) user.league = "Bronze";
                                tempList.add(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user: " + data.getKey(), e);
                        }
                    }
                }
                Log.d(TAG, "Data loaded. Count: " + tempList.size());
                allUsers = tempList;
                filterAndRefreshUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    private void filterAndRefreshUI() {
        filteredList.clear();
        String selected = currentSelectedLeague.toLowerCase();
        
        for (LeaderboardUser user : allUsers) {
            String userLeague = (user.league != null) ? user.league.toLowerCase() : "";
            if (userLeague.equals(selected) || userLeague.contains(selected)) {
                filteredList.add(user);
            }
        }

        Log.d(TAG, "Filtered for " + currentSelectedLeague + ": " + filteredList.size());

        Collections.sort(filteredList, (u1, u2) -> {
            long p1 = u1.leaguePoints != null ? u1.leaguePoints : 0;
            long p2 = u2.leaguePoints != null ? u2.leaguePoints : 0;
            return Long.compare(p2, p1);
        });

        updateUI();
    }

    private void updateUI() {
        if (filteredList.isEmpty()) {
            podiumContainer.setVisibility(View.GONE);
            adapter.updateData(new ArrayList<>());
            Log.d(TAG, "UI Update: Empty list, podium hidden.");
            return;
        }

        podiumContainer.setVisibility(View.VISIBLE);
        Log.d(TAG, "UI Update: Showing podium with " + filteredList.size() + " users.");

        if (filteredList.size() >= 1) {
            fillPodium(podium1, filteredList.get(0), 1);
            podium1.setVisibility(View.VISIBLE);
        } else {
            podium1.setVisibility(View.INVISIBLE);
        }

        if (filteredList.size() >= 2) {
            fillPodium(podium2, filteredList.get(1), 2);
            podium2.setVisibility(View.VISIBLE);
        } else {
            podium2.setVisibility(View.INVISIBLE);
        }

        if (filteredList.size() >= 3) {
            fillPodium(podium3, filteredList.get(2), 3);
            podium3.setVisibility(View.VISIBLE);
        } else {
            podium3.setVisibility(View.INVISIBLE);
        }

        List<LeaderboardUser> recyclerList = new ArrayList<>();
        if (filteredList.size() > 3) {
            recyclerList.addAll(filteredList.subList(3, filteredList.size()));
        }
        adapter.updateData(recyclerList);
    }

    private void fillPodium(View view, LeaderboardUser user, int rank) {
        TextView tvName = view.findViewById(R.id.tvPlayerName);
        TextView tvScore = view.findViewById(R.id.tvPlayerScore);
        TextView tvRank = view.findViewById(R.id.tvRankBadge);
        
        if (tvName != null) tvName.setText(user.nickname != null ? user.nickname : "---");
        if (tvScore != null) {
            long p = user.leaguePoints != null ? user.leaguePoints : 0;
            tvScore.setText(p + " pts");
        }
        if (tvRank != null) tvRank.setText("#" + rank);
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
