package server;

import persistence.DatabaseManager;
import com.google.gson.JsonObject;

public class GameSession {

    private ClientHandler player1;
    private ClientHandler player2;
    private DatabaseManager db;

    private boolean player1Dead = false;
    private boolean player2Dead = false;

    public GameSession(ClientHandler p1, ClientHandler p2, DatabaseManager db) {
        this.player1 = p1;
        this.player2 = p2;
        this.db = db;

        p1.setSession(this);
        p2.setSession(this);
    }

    // ---------------------------------------------------------
    // INICIO DE PARTIDA
    // ---------------------------------------------------------
    public void start() {
        System.out.println("[GameSession] Partida iniciada");

        JsonObject configMsg = new JsonObject();
        configMsg.addProperty("type", "CONFIG");
        configMsg.addProperty("initialHp", 100);
        configMsg.addProperty("baseSpawnRate", 1.0);
        configMsg.addProperty("baseAttackSpeed", 2.0);
        configMsg.addProperty("scorePerKill", 50);
        configMsg.addProperty("difficultyStepScore", 100);
        configMsg.addProperty("spawnMultiplierPerLevel", 1.15);
        configMsg.addProperty("speedAddPerLevel", 0.3);

        JsonObject damageByType = new JsonObject();
        damageByType.addProperty("DDOS", 4);
        damageByType.addProperty("MALWARE", 8);
        damageByType.addProperty("CRED", 10);
        configMsg.add("damageByType", damageByType);

        player1.send(configMsg.toString());
        player2.send(configMsg.toString());

        JsonObject msg = new JsonObject();
        msg.addProperty("type", "MATCH_FOUND");

        player1.send(msg.toString());
        player2.send(msg.toString());
    }

    // ---------------------------------------------------------
    // Enviar al oponente
    // ---------------------------------------------------------
    public void sendToOpponent(ClientHandler sender, String message) {
        if (sender == player1 && player2 != null) {
            player2.send(message);
        } else if (sender == player2 && player1 != null) {
            player1.send(message);
        }
    }

    // ---------------------------------------------------------
    // Muerte de jugador
    // ---------------------------------------------------------
    public void playerDied(ClientHandler player, int score) {
        if (player == player1) {
            player1Dead = true;
        } else {
            player2Dead = true;
        }

        // Guardar score en DB
        if (player.getUsername() != null) {
            db.updateHighScore(player.getUsername(), score);
        }

        if (player1Dead && player2Dead) {
            endGame();
        }
    }

    // ---------------------------------------------------------
    // Desconexión
    // ---------------------------------------------------------
    public void playerDisconnected(ClientHandler player) {
        ClientHandler opponent = (player == player1) ? player2 : player1;

        if (opponent != null) {
            opponent.send("{\"type\":\"OPPONENT_DISCONNECTED\"}");
        }

        endGame();
    }

    // ---------------------------------------------------------
    // Finalizar partida
    // ---------------------------------------------------------
    private void endGame() {
        System.out.println("[GameSession] Partida finalizada");

        if (player1 != null) player1.setSession(null);
        if (player2 != null) player2.setSession(null);
    }
}
