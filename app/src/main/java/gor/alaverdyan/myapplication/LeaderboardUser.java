package gor.alaverdyan.myapplication;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Map;

@IgnoreExtraProperties
public class LeaderboardUser {
    public String uid;
    public String nickname;
    public Long totalScore;
    public Long quizCoins;
    public String email;
    public Integer gamesPlayed;
    public String lastLoginDate;
    public Map<String, Object> progress;

    public LeaderboardUser() {}

    public LeaderboardUser(String uid, String nickname, Long totalScore, Long quizCoins) {
        this.uid = uid;
        this.nickname = nickname;
        this.totalScore = totalScore;
        this.quizCoins = quizCoins;
    }
}
