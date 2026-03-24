package client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ServerConnection {
    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;
    private volatile boolean running;

    private Consumer<JsonObject> onMessage;
    private Consumer<String> onError;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        if (running) return;
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;
        startReader();
    }

    private void startReader() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    JsonObject msg = JsonParser.parseString(line).getAsJsonObject();
                    if (onMessage != null) onMessage.accept(msg);
                }
            } catch (Exception e) {
                if (running && onError != null) onError.accept("Error de lectura: " + e.getMessage());
            } finally {
                close();
            }
        }, "server-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public synchronized void send(JsonObject json) {
        if (!running || out == null) return;
        out.println(json.toString());
    }

    public void register(String username, String password) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "REGISTER");
        o.addProperty("username", username);
        o.addProperty("password", password);
        send(o);
    }

    public void login(String username, String password) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "LOGIN");
        o.addProperty("username", username);
        o.addProperty("password", password);
        send(o);
    }

    public void selectAvatar(String avatar) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "SELECT_AVATAR");
        o.addProperty("avatar", avatar);
        send(o);
    }

    public void selectMap(String map) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "SELECT_MAP");
        o.addProperty("map", map);
        send(o);
    }

    public void joinQueue() {
        JsonObject o = new JsonObject();
        o.addProperty("type", "JOIN_QUEUE");
        send(o);
    }

    public void stateUpdate(int hp, int score, int level) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "STATE_UPDATE");
        o.addProperty("hp", hp);
        o.addProperty("score", score);
        o.addProperty("level", level);
        send(o);
    }

    public void playerDead(int finalScore) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "PLAYER_DEAD");
        o.addProperty("score", finalScore);
        send(o);
    }

    public synchronized void close() {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null;
        in = null;
        out = null;
    }

    public void setOnMessage(Consumer<JsonObject> onMessage) { this.onMessage = onMessage; }
    public void setOnError(Consumer<String> onError) { this.onError = onError; }
}