package model;

public class UserRecord {
    public String username;
    public String passwordHash;
    public String avatar;
    public int highScore;

    public UserRecord(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatar = "";
        this.highScore = 0;
    }
}
