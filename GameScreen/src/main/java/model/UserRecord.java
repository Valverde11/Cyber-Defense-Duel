package model;

public class UserRecord {
    public String username;
    public String passwordHash;
    public String avatar;
    public int highScore;
    public int totalScore = 0;
    public int gamesPlayed = 0;
    public int xpYellow = 0;
    public int xpRed = 0;
    public int xpBlue = 0;

    public UserRecord(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatar = "";
        this.highScore = 0;
        
    }
}
