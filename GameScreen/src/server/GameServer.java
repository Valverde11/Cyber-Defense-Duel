package server;

public class GameServer {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------
    private static final int PORT = 5000;
 
    // -------------------------------------------------------------------------
    // Estado del servidor
    // -------------------------------------------------------------------------
    private ServerSocket serverSocket;
    private DatabaseManager db;         // Maneja database.json
    private SessionManager sessionMgr;  // Empareja jugadores en sesiones 1 vs 1
 
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public GameServer() {
        db         = new DatabaseManager();   // carga database.json al arrancar
        sessionMgr = new SessionManager(db);
    }
 
    // -------------------------------------------------------------------------
    // Inicio del servidor
    // -------------------------------------------------------------------------
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("=== Cyber Defense Duel - Servidor iniciado en puerto " + PORT + " ===");
 
            while (true) {
                // Bloquea hasta que llega un nuevo cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Servidor] Nueva conexion desde: "
                        + clientSocket.getInetAddress().getHostAddress());
 
                // Cada cliente se atiende en su propio hilo para no bloquear al servidor
                ClientHandler handler = new ClientHandler(clientSocket, db, sessionMgr);
                Thread t = new Thread(handler);
                t.setDaemon(true); // el hilo muere si el servidor se cierra
                t.start();
            }
 
        } catch (IOException e) {
            System.err.println("[Servidor] Error critico: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
 
    // -------------------------------------------------------------------------
    // Cierre limpio del servidor
    // -------------------------------------------------------------------------
    private void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("[Servidor] Apagado correctamente.");
        } catch (IOException e) {
            System.err.println("[Servidor] Error al cerrar: " + e.getMessage());
        }
    }
 
    // -------------------------------------------------------------------------
    // Punto de entrada
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        new GameServer().start();
    }
}