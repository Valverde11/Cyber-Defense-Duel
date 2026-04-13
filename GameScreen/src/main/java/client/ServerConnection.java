package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerConnection {
    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;      // Para leer respuestas del servidor
    private PrintWriter out;        // Para enviar mensajes al servidor
    private Thread readerThread;    // Hilo que escucha constantemente mensajes entrantes
    private volatile boolean running; // volatile para evitar problemas de visibilidad entre hilos

    private Consumer<JsonObject> onMessage; // Callback que se ejecuta al recibir un mensaje
    private Consumer<String> onError;       // Callback para notificar errores de conexión

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        if (running) return; // Ya estamos conectados
        socket = new Socket(host, port);
        // Configuramos los streams con codificación UTF-8
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        running = true;
        startReader(); // Lanza el hilo que escucha mensajes
    }

    private void startReader() {
        readerThread = new Thread(() -> {
            try {
                String line;
                // Mientras el flag running sea true, leemos línea a línea del servidor
                while (running && (line = in.readLine()) != null) {
                    // Convertimos el String JSON a JsonObject y llamamos al callback
                    JsonObject msg = JsonParser.parseString(line).getAsJsonObject();
                    if (onMessage != null) onMessage.accept(msg);
                }
            } catch (Exception e) {
                // Si ocurre un error y aún estamos en ejecución, avisamos por onError
                if (running && onError != null) onError.accept("Error de lectura: " + e.getMessage());
            } finally {
                close(); // Cerramos todo al salir del bucle
            }
        }, "server-reader");
        readerThread.setDaemon(true); // Hilo demonio para que no impida cerrar la app
        readerThread.start();
    }

    public synchronized void send(JsonObject json) {
        if (!running || out == null) return; // No enviamos si la conexión está caída
        out.println(json.toString());        // Enviamos el JSON como una línea de texto
    }

    // ----------------- Métodos de conveniencia para los distintos tipos de mensaje -----------------

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

    public void playerDead(int finalScore, int yellow, int red, int blue) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "PLAYER_DEAD");
        o.addProperty("score", finalScore);
        o.addProperty("yellowKills", yellow);
        o.addProperty("redKills", red);
        o.addProperty("blueKills", blue);
        send(o);
    }

    public synchronized void close() {
        running = false; // Señal para que el hilo de lectura termine
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null;
        in = null;
        out = null;
    }

    // Setters para los callbacks (se asignan desde la interfaz gráfica o controlador)
    public void setOnMessage(Consumer<JsonObject> onMessage) { this.onMessage = onMessage; }
    public void setOnError(Consumer<String> onError) { this.onError = onError; }
}