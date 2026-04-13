package ui;

import com.google.gson.JsonObject;

import audio.AudioManager;
import client.ServerConnection;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.Bullet;
import logic.Enemy;
import logic.GameConfig;
import logic.GameLogic;
import logic.Player;
import persistence.DatabaseManager;

public class GameView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;
    private final GameLogic gameLogic;
    private boolean leftPressed      = false;
    private boolean rightPressed     = false;
    private boolean playerPositioned = false;
    private GameConfig config;
    private final ServerConnection connection;
    private final Image playerImage;
    private Image backgroundImage;

    private final DatabaseManager db;
    private final String username;

    private long lastEnemySpawn    = 0;
    private long lastStatePush     = 0;
    private final long statePushIntervalMs = 200;

    private long getSpawnCooldown() {
        int level = gameLogic.getLevel();
        double multiplier = Math.pow(config.spawnMultiplierPerLevel, level);
        return Math.max(100, (long)(500 / multiplier));
    }

    private boolean playerWon          = false;
    private boolean gameEnded          = false;
    private boolean opponentDead       = false;
    private boolean opponentDisconnected = false;
    private boolean deadNotified       = false;

    private int    opponentHp       = 100;
    private int    opponentScore    = 0;
    private int    opponentLevel    = 0;
    private String opponentUsername = "Opponent";

    public GameView(GameConfig config, ServerConnection connection,
                    String selectedAvatar, String selectedMap,
                    String username, DatabaseManager db) {
        this.config          = config;
        this.connection      = connection;
        this.playerImage     = loadAvatarImage(selectedAvatar);
        this.backgroundImage = loadMapImage(selectedMap);
        this.username        = username;
        this.db              = db;

        // Canvas dinámico — se ajusta al tamaño del Pane
        canvas = new Canvas();
        gc     = canvas.getGraphicsContext2D();

        // Bind canvas al tamaño del Pane
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());

        gameLogic  = new GameLogic(config);
        opponentHp = config.initialHp;
        getChildren().add(canvas);
        setFocusTraversable(true);
        setupControls();
        setupNetworkHandlers();
        AudioManager.playMusic("/sounds/Coconut Mall - Mario Kart Wii OST.mp3");
    }

    private void setupNetworkHandlers() {
        connection.setOnMessage(msg -> Platform.runLater(() -> handleServerMessage(msg)));
        connection.setOnError(err -> Platform.runLater(() -> {
            opponentDisconnected = true;
            gameEnded = true;
            playerWon = true;
        }));
    }

    private void handleServerMessage(JsonObject msg) {
        String type = msg.get("type").getAsString();
        switch (type) {
            case "OPPONENT_UPDATE":
                opponentHp    = msg.get("hp").getAsInt();
                opponentScore = msg.get("score").getAsInt();
                opponentLevel = msg.get("level").getAsInt();
                if (msg.has("username"))
                    opponentUsername = msg.get("username").getAsString();
                break;
            case "OPPONENT_DEAD":
                opponentDead = true;
                break;
            case "OPPONENT_DISCONNECTED":
                opponentDisconnected = true;
                gameEnded = true;
                playerWon = true;
                AudioManager.stopMusic();
                AudioManager.playSound("/sounds/smb_stage_clear.wav");
                break;
            default:
                break;
        }
    }

    // ── Teclado ───────────────────────────────────────────────

    private void setupControls() {
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT,  A -> leftPressed  = true;
                case RIGHT, D -> rightPressed = true;
                case Q -> gameLogic.shootYellow();
                case W -> gameLogic.shootRed();
                case E -> gameLogic.shootBlue();
                default -> {}
            }
        });
        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT,  A -> leftPressed  = false;
                case RIGHT, D -> rightPressed = false;
                default -> {}
            }
        });
    }

    // ── Game loop ─────────────────────────────────────────────

    public void startGame() {
        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();
    }

    private void update() {
        if (gameEnded) return;

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Esperar a que el canvas tenga tamaño real
        if (w <= 0 || h <= 0) return;

        if (!playerPositioned) {
            gameLogic.centerPlayer((int) w, (int) h);
            playerPositioned = true;
        }
        if (leftPressed)  gameLogic.moveLeft();
        if (rightPressed) gameLogic.moveRight();
        spawnEnemies();
        gameLogic.update((int) w, (int) h);
        pushStateToServer();

        if (gameLogic.isGameOver()) {
            triggerGameOver();
            AudioManager.stopMusic();
            AudioManager.playSound("/sounds/smb_gameover.wav");
            if (!deadNotified) {
                connection.playerDead(
                    gameLogic.getScore(),
                    gameLogic.getYellowKills(),
                    gameLogic.getRedKills(),
                    gameLogic.getBlueKills());
                deadNotified = true;
            }
        }
    }

    private void pushStateToServer() {
        long now = System.currentTimeMillis();
        if (now - lastStatePush < statePushIntervalMs) return;
        Player player = gameLogic.getPlayer();
        connection.stateUpdate(player.getHp(), gameLogic.getScore(), gameLogic.getLevel());
        lastStatePush = now;
    }

    private void triggerGameOver() { gameEnded = true; playerWon = false; }
    private void triggerGameWin()  { gameEnded = true; playerWon = true;  }

    // ── Renderizado ───────────────────────────────────────────

    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        drawBackground(w, h);
        drawEnemies();
        drawBullets();
        drawPlayer();
        drawHUD(w, h);
        drawEndGame(w, h);
    }

    // ── Fondo ─────────────────────────────────────────────────

    private void drawBackground(double w, double h) {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, w, h);
            gc.setFill(Color.color(0, 0, 0, 0.45));
            gc.fillRect(0, 0, w, h);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, w, h);
        }
    }

    // ── Jugador ───────────────────────────────────────────────

    private void drawPlayer() {
        Player player = gameLogic.getPlayer();
        if (playerImage != null) {
            gc.drawImage(playerImage,
                player.getX(), player.getY(),
                player.getWidth(), player.getHeight());
        } else {
            gc.setFill(player.isInvulnerable() ? Color.YELLOW : Color.GREEN);
            gc.fillRect(player.getX(), player.getY(),
                player.getWidth(), player.getHeight());
        }
    }

    // ── Balas ─────────────────────────────────────────────────

    private void drawBullets() {
        Bullet[] bullets = gameLogic.getBullets();
        for (int i = 0; i < gameLogic.getBulletCount(); i++) {
            Bullet bullet = bullets[i];
            if (bullet.getSprite() != null) {
                gc.drawImage(bullet.getSprite(),
                    bullet.getX(), bullet.getY(),
                    bullet.getWidth(), bullet.getHeight());
            } else {
                gc.setFill(typeColor(bullet.getType()));
                gc.fillRoundRect(bullet.getX(), bullet.getY(),
                    bullet.getWidth(), bullet.getHeight(), 4, 4);
            }
        }
    }

    // ── Enemigos ──────────────────────────────────────────────

    private void drawEnemies() {
        Enemy[] enemies = gameLogic.getEnemies();
        for (int i = 0; i < gameLogic.getEnemyCount(); i++) {
            Enemy enemy = enemies[i];
            if (enemy.getSprite() != null) {
                gc.drawImage(enemy.getSprite(),
                    enemy.getX(), enemy.getY(),
                    enemy.getWidth(), enemy.getHeight());
            } else {
                gc.setFill(typeColor(enemy.getType()));
                gc.fillRoundRect(enemy.getX(), enemy.getY(),
                    enemy.getWidth(), enemy.getHeight(), 8, 8);
            }
        }
    }

    // ── HUD completo ──────────────────────────────────────────

    private void drawHUD(double w, double h) {
        // Fondo HUD superior
        gc.setFill(Color.color(0, 0, 0, 0.75));
        gc.fillRect(0, 0, w, 80);

        // Línea inferior del HUD
        gc.setStroke(Color.color(0, 0.8, 1, 0.3));
        gc.setLineWidth(1);
        gc.strokeLine(0, 80, w, 80);

        Player player = gameLogic.getPlayer();
        double percent = (double) player.getHp() / player.getMaxHp();

        // ── HP izquierda ──────────────────────────────────
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        gc.fillText("HP  " + username.toUpperCase(), 20, 22);

        double barW = 260, barH = 16;
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRoundRect(20, 30, barW, barH, 6, 6);

        Color hpColor = percent > 0.6 ? Color.LIMEGREEN
                      : percent > 0.3 ? Color.ORANGE : Color.RED;
        gc.setFill(hpColor);
        gc.fillRoundRect(20, 30, barW * percent, barH, 6, 6);

        gc.setStroke(Color.color(1, 1, 1, 0.3));
        gc.setLineWidth(1);
        gc.strokeRoundRect(20, 30, barW, barH, 6, 6);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Courier New", 11));
        gc.fillText(player.getHp() + " / " + player.getMaxHp(), 24, 60);

        // ── Score centrado ────────────────────────────────
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.setFont(Font.font("Courier New", 10));
        String scoreLabel = "SCORE";
        double slw = scoreLabel.length() * 6.5;
        gc.fillText(scoreLabel, w / 2 - slw / 2, 18);

        gc.setFill(Color.web("#ffd232"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        String scoreStr = String.format("%06d", gameLogic.getScore());
        double sw2 = scoreStr.length() * 15.5;
        gc.fillText(scoreStr, w / 2 - sw2 / 2, 50);

        // ── Nivel ─────────────────────────────────────────
        gc.setFill(Color.color(0.4, 0.7, 1, 0.9));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        String lvlStr = "NVL " + gameLogic.getLevel();
        gc.fillText(lvlStr, w / 2 - lvlStr.length() * 4, 68);

        // ── Oponente derecha ──────────────────────────────
        double ox = w - 290;
        double opPercent = Math.max(0, Math.min(1, opponentHp / (double) config.initialHp));

        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        gc.fillText("HP  " + opponentUsername.toUpperCase(), ox, 22);

        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRoundRect(ox, 30, barW, barH, 6, 6);

        Color oppColor = opponentDisconnected ? Color.GRAY : Color.CORNFLOWERBLUE;
        gc.setFill(oppColor);
        gc.fillRoundRect(ox, 30, barW * opPercent, barH, 6, 6);

        gc.setStroke(Color.color(1, 1, 1, 0.3));
        gc.setLineWidth(1);
        gc.strokeRoundRect(ox, 30, barW, barH, 6, 6);

        gc.setFill(Color.color(0.6, 0.7, 0.9, 0.9));
        gc.setFont(Font.font("Courier New", 11));
        gc.fillText(opponentHp + " HP  |  " + opponentScore + " pts  |  NVL "
                + opponentLevel, ox, 60);

        // ── Controles esquina derecha abajo ───────────────
        gc.setFont(Font.font("Courier New", 11));
        gc.setFill(Color.web("#ffd232")); gc.fillText("[Q] Firewall",  w - 160, h - 56);
        gc.setFill(Color.web("#ff4444")); gc.fillText("[W] Antivirus", w - 160, h - 38);
        gc.setFill(Color.web("#4488ff")); gc.fillText("[E] CryptoShield", w - 160, h - 20);
    }

    // ── Spawn ─────────────────────────────────────────────────

    private void spawnEnemies() {
        long now = System.currentTimeMillis();
        if (now - lastEnemySpawn >= getSpawnCooldown()) {
            gameLogic.spawnEnemy((int) canvas.getWidth());
            lastEnemySpawn = now;
        }
    }

    // ── Fin de juego ──────────────────────────────────────────

    private void drawEndGame(double w, double h) {
        if (opponentDisconnected) {
            gc.setFill(Color.color(0, 0, 0, 0.75));
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 34));
            drawCentered("Oponente desconectado", w, h / 2 - 20);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
            drawCentered("Sesión terminada", w, h / 2 + 20);
            return;
        }
        if (opponentDead && !gameEnded) {
            gc.setFill(Color.web("#00c85a"));
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
            drawCentered("¡Oponente derrotado! Sigue jugando...", w, 100);
        }
        if (gameEnded) {
            gc.setFill(Color.color(0, 0, 0, 0.78));
            gc.fillRect(0, 0, w, h);
            if (playerWon) drawGameWin(w, h);
            else           drawGameOver(w, h);
        }
    }

    private void drawGameOver(double w, double h) {
        gc.setFill(Color.web("#ff3c3c"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 60));
        drawCentered("GAME OVER", w, h / 2 - 40);
        gc.setFill(Color.web("#ffd232"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        drawCentered("Score: " + String.format("%06d", gameLogic.getScore()), w, h / 2 + 20);
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Courier New", 14));
        drawCentered("Esperando al oponente...", w, h / 2 + 60);
    }

    private void drawGameWin(double w, double h) {
        gc.setFill(Color.web("#00c85a"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 60));
        drawCentered("YOU WIN", w, h / 2 - 40);
        gc.setFill(Color.web("#ffd232"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        drawCentered("Score: " + String.format("%06d", gameLogic.getScore()), w, h / 2 + 20);
    }

    // ── Helper centrado ───────────────────────────────────────

    private void drawCentered(String text, double w, double y) {
        double approxW = text.length() * (gc.getFont().getSize() * 0.6);
        gc.fillText(text, (w - approxW) / 2, y);
    }

    // ── Carga de imágenes ─────────────────────────────────────

    private Image loadAvatarImage(String avatarId) {
        if (avatarId == null || avatarId.isBlank()) avatarId = "character_1";
        try {
            var stream = getClass().getResourceAsStream(
                "/assets/characters/" + avatarId + ".png");
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) { return null; }
    }

    private Image loadMapImage(String mapName) {
        if (mapName == null) return null;
        String mapId = mapName.toLowerCase().replace(" ", "_");
        try {
            var stream = getClass().getResourceAsStream(
                "/assets/backgrounds/" + mapId + ".png");
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) { return null; }
    }

    private Color typeColor(logic.AttackType type) {
        return switch (type) {
            case YELLOW -> Color.web("#ffd232");
            case RED    -> Color.web("#ff3c3c");
            case BLUE   -> Color.web("#00aaff");
        };
    }
}