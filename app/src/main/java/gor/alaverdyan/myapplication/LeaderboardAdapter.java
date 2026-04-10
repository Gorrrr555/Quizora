package gor.alaverdyan.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardUser> userList;
    private Context context;

    public LeaderboardAdapter(List<LeaderboardUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    public void updateData(List<LeaderboardUser> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardUser user = userList.get(position);
        holder.tvRank.setText(String.valueOf(position + 4)); // +4 because top 3 are in podium
        holder.tvNickname.setText(user.nickname);
        holder.tvScore.setText(user.leaguePoints + " pts");
        
        holder.tvScore.setTextColor(ContextCompat.getColor(context, R.color.primaryBlue));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvNickname, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
