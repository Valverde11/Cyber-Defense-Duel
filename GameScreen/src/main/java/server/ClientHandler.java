package server;

import persistence.DatabaseManager;
import model.UserRecord;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
 
    private final Socket socket;
    private final DatabaseManager db;
    private final SessionManager sessionMgr;
 
    private BufferedReader in;
    private PrintWriter    out;
 
    private String username;          // se llena luego del login exitoso
    private String selectedMap = "";  // seleccion local hasta que Session arme la partida
    private GameSession currentSession; // sesion a la que pertenece este cliente
 
    public ClientHandler(Socket socket, DatabaseManager db, SessionManager sessionMgr) {
        this.socket     = socket;
        this.db         = db;
        this.sessionMgr = sessionMgr;
    }
 
    // -------------------------------------------------------------------------
    // Ciclo principal: leer mensajes del cliente
    // -------------------------------------------------------------------------
    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); // autoFlush para respuestas inmediatas
 
            String rawMessage;
            while ((rawMessage = in.readLine()) != null) { // Lee mensajes linea a linea
                System.out.println("[" + (username != null ? username : "?") + "] >> " + rawMessage);
                processMessage(rawMessage); // Procesa cada mensaje JSON recibido
            }
 
        } catch (IOException e) {
            System.out.println("[Servidor] Cliente desconectado: "
                    + (username != null ? username : socket.getInetAddress()));
        } finally {
            handleDisconnect();
        }
    }
 
    // -------------------------------------------------------------------------
    // Despacha cada mensaje segun su "type"
    // -------------------------------------------------------------------------
    private void processMessage(String raw) {
        try {
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject(); // Parsea string JSON a objeto
            String type = json.get("type").getAsString(); // Extrae tipo de mensaje
 
            switch (type) { // Despacha segun tipo para manejar cada flujo
 
                case "REGISTER":
                    handleRegister(json);
                    break;
 
                case "LOGIN":
                    handleLogin(json);
                    break;
 
                case "SELECT_AVATAR":
                    handleSelectAvatar(json);
                    break;
 
                case "SELECT_MAP":
                    handleSelectMap(json);
                    break;
 
                case "JOIN_QUEUE":
                    // El cliente ya eligio avatar y mapa; quiere entrar a una partida
                    sessionMgr.joinQueue(this);
                    break;
 
                case "STATE_UPDATE":
                    // El cliente envia su HP y score actuales durante la partida
                    handleStateUpdate(json);
                    break;
 
                case "PLAYER_DEAD":
                    // El jugador llego a HP = 0
                    handlePlayerDead(json);
                    break;
 
                default:
                    send(buildError("Tipo de mensaje desconocido: " + type));
                    break;
            }
 
        } catch (Exception e) {
            System.err.println("[Servidor] Error procesando mensaje: " + e.getMessage());
            send(buildError("Mensaje mal formado."));
        }
    }
 
    // -------------------------------------------------------------------------
    // Handlers individuales
    // -------------------------------------------------------------------------
 
    /** Registra un usuario nuevo en database.json */
    private void handleRegister(JsonObject json) {
        String user = json.get("username").getAsString().trim();
        String pass = json.get("password").getAsString().trim();

        if (db.register(user, pass)) {
            send(buildResponse("REGISTER_OK", "Usuario registrado correctamente."));
        } else {
            send(buildResponse("REGISTER_FAIL", "El usuario ya existe."));
        }
    }
 
    /** Valida credenciales contra database.json */
    private void handleLogin(JsonObject json) {
        String user = json.get("username").getAsString().trim();
        String pass = json.get("password").getAsString().trim();

        UserRecord record = db.login(user, pass); // Verifica credenciales
        if (record == null) {
            send(buildResponse("LOGIN_FAIL", "Usuario o contrasena incorrectos."));
            return;
        }
 
        this.username = user;
        System.out.println("[Servidor] Login exitoso: " + username);
        System.out.println("  Partidas jugadas: " + record.gamesPlayed);
        System.out.println("  Kills DDoS: " + record.xpYellow);
        System.out.println("  Kills Malware: " + record.xpRed);
        System.out.println("  Kills Credential Attack: " + record.xpBlue);
 
        // Respuesta exitosa + estadisticas del jugador
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "LOGIN_OK");
        resp.addProperty("username", username);

        JsonObject stats = new JsonObject();
        stats.addProperty("avatar", record.avatar);
        stats.addProperty("highScore", record.highScore);
        stats.addProperty("gamesPlayed", record.gamesPlayed);
        stats.addProperty("xpYellow", record.xpYellow);
        stats.addProperty("xpRed", record.xpRed);
        stats.addProperty("xpBlue", record.xpBlue);
        resp.add("stats", stats);
        send(resp.toString());
    }
 
    /** Guarda el avatar elegido (persistencia en database.json) */
    private void handleSelectAvatar(JsonObject json) {
        if (username == null) { send(buildError("Debes iniciar sesion primero.")); return; }
        String avatar = json.get("avatar").getAsString();
        db.saveAvatar(username, avatar);
        send(buildResponse("AVATAR_OK", "Avatar seleccionado: " + avatar));
    }
 
    /** Confirma la seleccion de mapa (se reenvía al oponente cuando ambos estén listos) */
    private void handleSelectMap(JsonObject json) {
        if (username == null) { send(buildError("Debes iniciar sesion primero.")); return; }
        selectedMap = json.get("map").getAsString();
        send(buildResponse("MAP_OK", "Mapa seleccionado: " + selectedMap));
    }
 
    /**
     * Retransmite el estado de este jugador a su oponente.
     * El oponente lo muestra en su scoreboard en vivo.
     * Formato esperado del cliente:
     *   {"type":"STATE_UPDATE", "hp":75, "score":240, "level":2}
     */
    private void handleStateUpdate(JsonObject json) {
        if (currentSession == null) return; // No hay sesion activa
 
        JsonObject opponentUpdate = new JsonObject();
        opponentUpdate.addProperty("type", "OPPONENT_UPDATE"); // Etiqueta para el oponente
        opponentUpdate.addProperty("hp",    json.get("hp").getAsInt());
        opponentUpdate.addProperty("score", json.get("score").getAsInt());
        opponentUpdate.addProperty("level", json.get("level").getAsInt());
        opponentUpdate.addProperty("username", username);
 
        currentSession.sendToOpponent(this, opponentUpdate.toString()); // Envia al jugador rival
    }
 
    /** El jugador murio (HP = 0). Notifica al oponente y verifica si la sesion debe cerrarse. */
    private void handlePlayerDead(JsonObject json) {
        if (currentSession == null) return; // No hay sesion activa
 
        int finalScore = json.has("score") ? json.get("score").getAsInt() : 0;
        int y = json.has("yellowKills") ? json.get("yellowKills").getAsInt() : 0;
        int r = json.has("redKills") ? json.get("redKills").getAsInt() : 0;
        int b = json.has("blueKills") ? json.get("blueKills").getAsInt() : 0;

        if (username != null) {
            db.updateStats(username, finalScore, y, r, b); // Actualiza estadisticas del jugador
        }
 
        // Notificar al oponente
        JsonObject notif = new JsonObject();
        notif.addProperty("type", "OPPONENT_DEAD"); // Señal de muerte del contrincante
        notif.addProperty("username", username);
        currentSession.sendToOpponent(this, notif.toString());
 
        // Registrar muerte en la sesion
        currentSession.playerDied(this, finalScore); // Sesion registra la muerte
    }
 
    // -------------------------------------------------------------------------
    // Desconexion inesperada
    // -------------------------------------------------------------------------
    private void handleDisconnect() {
        if (currentSession != null) {
            currentSession.playerDisconnected(this);
        }
        // Si estaba en cola esperando, el SessionManager lo limpia
        sessionMgr.removeFromQueue(this);
 
        try { socket.close(); } catch (IOException ignored) {}
    }
 
    // -------------------------------------------------------------------------
    // Metodos de envio
    // -------------------------------------------------------------------------
 
    /** Envia un mensaje JSON al cliente de este handler */
    public void send(String jsonMessage) {
        out.println(jsonMessage);
    }
 
    // -------------------------------------------------------------------------
    // Helpers para construir respuestas rapidas
    // -------------------------------------------------------------------------
    private String buildResponse(String type, String message) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        o.addProperty("message", message);
        return o.toString();
    }
 
    private String buildError(String message) {
        return buildResponse("ERROR", message);
    }
 
    // -------------------------------------------------------------------------
    // Getters usados por GameSession y SessionManager
    // -------------------------------------------------------------------------
    public String getUsername()          { return username; }
    public String getSelectedMap()       { return selectedMap; }
    public void   setSession(GameSession s) { this.currentSession = s; }
}
