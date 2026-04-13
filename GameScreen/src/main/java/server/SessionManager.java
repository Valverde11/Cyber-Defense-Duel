package server;

import persistence.DatabaseManager;

public class SessionManager {

    private final DatabaseManager db;
    private ClientHandler waitingPlayer;

    public SessionManager(DatabaseManager db) {
        this.db = db;
    }

    public synchronized void joinQueue(ClientHandler player) {
        System.out.println("[Queue] joinQueue llamado por: " + player.getUsername());
        System.out.println("[Queue] waitingPlayer es: " + (waitingPlayer != null ? waitingPlayer.getUsername() : "null"));

        if (player.getUsername() == null) {
            player.send("{\"type\":\"ERROR\",\"message\":\"Debes iniciar sesión primero.\"}");
            return;
        }

        if (waitingPlayer == null) {
            waitingPlayer = player;
            player.send("{\"type\":\"WAITING\",\"message\":\"Esperando oponente...\"}");
            System.out.println("[SessionManager] " + player.getUsername() + " en cola, esperando oponente.");
        } else {
            System.out.println("[SessionManager] Emparejando "
                    + waitingPlayer.getUsername() + " vs " + player.getUsername());
            GameSession session = new GameSession(waitingPlayer, player, db);
            waitingPlayer = null;
            session.start();
        }
    }

    public synchronized void removeFromQueue(ClientHandler player) {
        if (waitingPlayer == player) {
            waitingPlayer = null;
            System.out.println("[SessionManager] " + player.getUsername() + " removido de la cola.");
        }
    }
}