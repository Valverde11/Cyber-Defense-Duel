package persistence;

import model.UserRecord;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;

public class DatabaseManager {

    private static final String DB_FILE = "database.json";
    private UserRecord[] users = new UserRecord[100];
    private int userCount = 0;

    public DatabaseManager() {
        load();
    }

    // Registra un usuario nuevo, retorna false si ya existe
    public boolean register(String username, String password) {
        if (findUser(username) != null)
            return false;
        users[userCount++] = new UserRecord(username, hash(password));
        save();
        return true;
    }

    // Retorna el UserRecord si las credenciales son correctas, null si no
    public UserRecord login(String username, String password) {
        UserRecord u = findUser(username);
        if (u == null)
            return null;
        return u.passwordHash.equals(hash(password)) ? u : null;
    }

    // Guarda el avatar elegido
    public void saveAvatar(String username, String avatar) {
        UserRecord u = findUser(username);
        if (u != null) {
            u.avatar = avatar;
            save();
        }
    }

    // Actualiza el highScore si el nuevo es mayor
    public void updateHighScore(String username, int score) {
        UserRecord u = findUser(username);
        if (u != null && score > u.highScore) {
            u.highScore = score;
            save();
        }
    }

    private UserRecord findUser(String username) {
        for (int i = 0; i < userCount; i++)
            if (users[i].username.equals(username))
                return users[i];
        return null;
    }

    // SHA-256 sin librerías externas
    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(DB_FILE)) {
            pw.print("[");
            for (int i = 0; i < userCount; i++) {
                pw.print(SimpleJson.toJson(users[i]));
                if (i < userCount - 1)
                    pw.print(",");
            }
            pw.print("]");
        } catch (Exception e) {
            System.err.println("Error guardando base de datos: " + e.getMessage());
        }
    }

    public void updateStats(String username, int score, int y, int r, int b) {
        UserRecord u = findUser(username);
        if (u != null) {
            u.totalScore += score;

            u.xpYellow += y;
            u.xpRed += r;
            u.xpBlue += b;

            if (score > u.highScore) {
                u.highScore = score;
            }

            save();
        }
    }

    public void registerGame(String username) {
        UserRecord u = findUser(username);
        if (u != null) {
            u.gamesPlayed++;
            save();
        }
    }

    private void load() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(DB_FILE)));
            content = content.trim().replaceAll("^\\[|\\]$", "");
            if (content.isEmpty())
                return;
            String[] entries = content.split("\\},\\{");
            for (String entry : entries) {
                if (!entry.startsWith("{"))
                    entry = "{" + entry;
                if (!entry.endsWith("}"))
                    entry = entry + "}";
                String username = SimpleJson.readString(entry, "username");
                String passHash = SimpleJson.readString(entry, "passwordHash");
                String avatar = SimpleJson.readString(entry, "avatar");
                int highScore = SimpleJson.readInt(entry, "highScore");
                int totalScore = SimpleJson.readInt(entry, "totalScore");
                int gamesPlayed = SimpleJson.readInt(entry, "gamesPlayed");
                int xpYellow = SimpleJson.readInt(entry, "xpYellow");
                int xpRed = SimpleJson.readInt(entry, "xpRed");
                int xpBlue = SimpleJson.readInt(entry, "xpBlue");
                if (!username.isEmpty() && userCount < users.length) {
                    UserRecord u = new UserRecord(username, passHash);
                    u.avatar = avatar;
                    u.highScore = highScore;
                    u.totalScore = totalScore;
                    u.gamesPlayed = gamesPlayed;
                    u.xpYellow = xpYellow;
                    u.xpRed = xpRed;
                    u.xpBlue = xpBlue;
                    users[userCount++] = u;
                }
            }
        } catch (Exception e) {
            // Primera vez que corre, el archivo no existe aún
        }
    }
}