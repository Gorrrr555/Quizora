package gor.alaverdyan.myapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardUser> userList;
    private Context context;
    private String currentUid;

    public LeaderboardAdapter(List<LeaderboardUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.currentUid = FirebaseAuth.getInstance().getUid();
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
        holder.tvRank.setText(String.valueOf(position + 4)); 
        holder.tvNickname.setText(user.nickname);
        holder.tvScore.setText(context.getString(R.string.points_format, user.totalScore));
        
        boolean isCurrentUser = user.uid != null && user.uid.equals(currentUid);
        
        if (isCurrentUser) {
            holder.mainCard.setStrokeColor(ContextCompat.getColor(context, R.color.primaryBlue));
            holder.mainCard.setStrokeWidth(4);
            holder.tvNickname.setTypeface(null, Typeface.BOLD);
            holder.tvNickname.setTextColor(ContextCompat.getColor(context, R.color.primaryBlue));
        } else {
            holder.mainCard.setStrokeColor(ContextCompat.getColor(context, R.color.cardStroke));
            holder.mainCard.setStrokeWidth(2);
            holder.tvNickname.setTypeface(null, Typeface.NORMAL);
            holder.tvNickname.setTextColor(ContextCompat.getColor(context, R.color.textDark));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvNickname, tvScore;
        MaterialCardView mainCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvScore = itemView.findViewById(R.id.tvScore);
            mainCard = (MaterialCardView) itemView;
        }
    }
}
