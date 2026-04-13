package model;

public class UserRecord {
    public String username; // Identificador unico de usuario
    public String passwordHash; // Hash SHA-256 de contrasena
    public String avatar; // Avatar seleccionado por usuario
    public int highScore; // Puntuacion maxima alcanzada
    public int totalScore = 0; // Puntos totales acumulados
    public int gamesPlayed = 0; // Cantidad de partidas jugadas
    public int xpYellow = 0; // Experiencia por ataques DDoS
    public int xpRed = 0; // Experiencia por ataques Malware
    public int xpBlue = 0; // Experiencia por ataques Credential

    public UserRecord(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatar = "";
        this.highScore = 0;
        
    }
}
