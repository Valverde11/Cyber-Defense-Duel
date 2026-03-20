package server;

import com.google.gson.JsonObject; // por si se usa gson aca

public class SessionManager {

    private final DatabaseManager db;
    private ClientHandler waitingPlayer; // jugador esperando oponente
 
    public SessionManager(DatabaseManager db) {
        this.db = db;
    }
 
    /**
     * Un jugador solicita entrar a una partida.
     * Si ya hay uno esperando, se crea una sesion inmediatamente.
     * Si no, el jugador queda en espera.
     * synchronized evita condiciones de carrera entre hilos.
     */
    public synchronized void joinQueue(ClientHandler player) {
        if (waitingPlayer == null) {
            // Primer jugador: espera
            waitingPlayer = player;
            player.send("{\"type\":\"WAITING\",\"message\":\"Esperando oponente...\"}");
            System.out.println("[SessionManager] " + player.getUsername() + " en cola, esperando oponente.");
        } else {
            // Segundo jugador: crear sesion
            System.out.println("[SessionManager] Emparejando "
                    + waitingPlayer.getUsername() + " vs " + player.getUsername());
 
            GameSession session = new GameSession(waitingPlayer, player, db);
            waitingPlayer = null;
            session.start(); // envia CONFIG y arranca la partida
        }
    }
 
    /** Elimina al jugador de la cola si se desconecto antes de encontrar pareja */
    public synchronized void removeFromQueue(ClientHandler player) {
        if (waitingPlayer == player) {
            waitingPlayer = null;
            System.out.println("[SessionManager] " + player.getUsername() + " removido de la cola.");
        }
    }
}

