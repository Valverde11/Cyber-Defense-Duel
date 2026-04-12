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
import javafx.stage.Stage;
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
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean playerPositioned = false;
    private GameConfig config;
    private final ServerConnection connection;
    private final Image playerImage;
    private Image backgroundImage; // ← campo agregado

    private final DatabaseManager db;
    private final String username;

    private long lastEnemySpawn = 0;
    private long lastStatePush = 0;
    private final long statePushIntervalMs = 200;

    private long getSpawnCooldown() {
        int level = gameLogic.getLevel();
        double multiplier = Math.pow(config.spawnMultiplierPerLevel, level);
        return Math.max(100, (long) (500 / multiplier));
    }

    private boolean playerWon = false;
    private boolean gameEnded = false;
    private boolean opponentDead = false;
    private boolean opponentDisconnected = false;
    private boolean deadNotified = false;
    private boolean playerIsDead = false;
    private boolean showFinalResult = false;

    private int opponentHp;
    private int opponentScore = 0;
    private int opponentLevel = 0;
    private String opponentUsername = "Opponent";
    
    private final Stage stage;

    // ── Constructor actualizado con selectedMap ────────────────
    public GameView(GameConfig config, ServerConnection connection,
            String selectedAvatar, String selectedMap,
            String username, DatabaseManager db, Stage stage) {
        this.config = config;
        this.connection = connection;
        this.playerImage = loadAvatarImage(selectedAvatar);
        this.backgroundImage = loadMapImage(selectedMap); // ← carga el mapa
        this.username = username;
        this.db = db;
        this.stage = stage;
        canvas = new Canvas(1280, 720);
        gc = canvas.getGraphicsContext2D();
        gameLogic = new GameLogic(config);
        opponentHp = config.initialHp;
        getChildren().add(canvas);
        setFocusTraversable(true);
        setupControls();
        setupMouseControls();
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
                opponentHp = msg.get("hp").getAsInt();
                opponentScore = msg.get("score").getAsInt();
                opponentLevel = msg.get("level").getAsInt();
                if (msg.has("username"))
                    opponentUsername = msg.get("username").getAsString();
                break;
            case "OPPONENT_DEAD":
                opponentDead = true;
                if (playerIsDead) {
                    // Ambos están muertos, mostrar resultado final
                    showFinalResult = true;
                    gameEnded = true;
                    AudioManager.stopMusic();
                    AudioManager.playSound("/sounds/smb_stage_clear.wav");
                    // Determinar ganador
                    int myScore = gameLogic.getScore();
                    if (myScore > opponentScore) {
                        playerWon = true;
                    } else if (myScore < opponentScore) {
                        playerWon = false;
                    }
                    // Si es empate, playerWon sigue siendo false pero mostraremos "DRAW"
                }
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
            if (playerIsDead) return; // Ignorar entrada si el jugador está muerto
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = true;
                case RIGHT, D -> rightPressed = true;
                case Q -> gameLogic.shootYellow();
                case W -> gameLogic.shootRed();
                case E -> gameLogic.shootBlue();
                default -> {
                }
            }
        });
        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = false;
                case RIGHT, D -> rightPressed = false;
                default -> {
                }
            }
        });
    }

    private void setupMouseControls() {
        setOnMouseClicked(e -> {
            // Solo procesar clics si se está mostrando el resultado final
            if (!showFinalResult) return;
            
            double mouseX = e.getX();
            double mouseY = e.getY();
            
            // Detectar clic en botón de menú
            if (mouseX >= menuButtonX && mouseX <= menuButtonX + menuButtonWidth &&
                mouseY >= menuButtonY && mouseY <= menuButtonY + menuButtonHeight) {
                returnToMenu();
            }
        });
    }
    
    private void returnToMenu() {
        AudioManager.stopMusic();
        new MenuScreen(stage, username, connection, db).show();
    }

    // ── Game loop ─────────────────────────────────────────────

    public void startGame() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();
    }

    private void update() {
        if (gameEnded)
            return;
        if (!playerPositioned) {
            gameLogic.centerPlayer((int) canvas.getWidth(), (int) canvas.getHeight());
            playerPositioned = true;
        }
        // Si el jugador está muerto, congelar el juego pero seguir recibiendo actualizaciones del servidor
        if (playerIsDead) {
            pushStateToServer();
            return;
        }
        if (leftPressed)
            gameLogic.moveLeft();
        if (rightPressed)
            gameLogic.moveRight();
        spawnEnemies();
        gameLogic.update((int) canvas.getWidth(), (int) canvas.getHeight());
        pushStateToServer();
        if (gameLogic.isGameOver()) {
            playerIsDead = true;
            AudioManager.stopMusic();
            AudioManager.playSound("/sounds/smb_gameover.wav");
            if (!deadNotified) {
                connection.playerDead(
                    gameLogic.getScore(),
                    gameLogic.getYellowKills(),
                    gameLogic.getRedKills(),
                    gameLogic.getBlueKills()
                );
                deadNotified = true;
            }
        }
    }

    private void pushStateToServer() {
        long now = System.currentTimeMillis();
        if (now - lastStatePush < statePushIntervalMs)
            return;
        Player player = gameLogic.getPlayer();
        connection.stateUpdate(player.getHp(), gameLogic.getScore(), gameLogic.getLevel());
        lastStatePush = now;
    }

    private void triggerGameOver() {
        gameEnded = true;
        playerWon = false;
    }

    private void triggerGameWin() {
        gameEnded = true;
        playerWon = true;
    }

    private void checkFinalResult() {
        // Detectar automáticamente cuando ambos jugadores están muertos
        if (playerIsDead && opponentDead && !showFinalResult) {
            showFinalResult = true;
            gameEnded = true;
            AudioManager.stopMusic();
            AudioManager.playSound("/sounds/smb_stage_clear.wav");
            
            // Determinar ganador comparando puntajes
            int myScore = gameLogic.getScore();
            if (myScore > opponentScore) {
                playerWon = true;
            } else if (myScore < opponentScore) {
                playerWon = false;
            }
            // Si es empate, playerWon sigue siendo false pero mostraremos "DRAW"
        }
    }

    // ── Renderizado ───────────────────────────────────────────

    private void render() {
        drawBackground();
        drawPlayer();
        drawBullets();
        drawEnemies();
        drawHealthBar(gc);
        drawOpponentHud(gc);
        drawScore(gc);
        drawLevel(gc);
        checkFinalResult(); // Detectar si ambos están muertos
        drawEndGame();
    }

    private void drawBackground() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0,
                    canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.color(0, 0, 0, 0.45));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

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

    // ── HUD ───────────────────────────────────────────────────

    private void drawHealthBar(GraphicsContext gc) {
        Player player = gameLogic.getPlayer();
        double percent = (double) player.getHp() / player.getMaxHp();
        double barWidth = 300;
        double barHeight = 25;
        double x = 20, y = 20;

        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        Color healthColor = percent > 0.6 ? Color.LIMEGREEN
                : percent > 0.3 ? Color.ORANGE : Color.RED;
        gc.setFill(healthColor);
        gc.fillRoundRect(x, y, barWidth * percent, barHeight, 10, 10);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, barWidth, barHeight, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText(player.getHp() + " / " + player.getMaxHp(),
                x + barWidth / 2 - 20, y + 17);
    }

    private void drawScore(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Score: " + gameLogic.getScore(),
                canvas.getWidth() - 200, 40);
    }

    private void drawLevel(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Level: " + gameLogic.getLevel(),
                canvas.getWidth() - 200, 70);
    }

    private void drawOpponentHud(GraphicsContext gc) {
        double barWidth = 250;
        double barHeight = 20;
        double x = canvas.getWidth() - 300;
        double y = 100;
        double percent = Math.max(0, Math.min(1, opponentHp / 100.0));

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText(opponentUsername, x, y - 10);

        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        gc.setFill(opponentDisconnected ? Color.GRAY : Color.CORNFLOWERBLUE);
        gc.fillRoundRect(x, y, barWidth * percent, barHeight, 10, 10);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, barWidth, barHeight, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("HP Rival: " + opponentHp, x, y + 38);
        gc.fillText("Score Rival: " + opponentScore, x, y + 58);
        gc.fillText("Level Rival: " + opponentLevel, x, y + 78);
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

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.fillText("GAME OVER",
                canvas.getWidth() / 2 - 180, canvas.getHeight() / 2 - 40);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        gc.fillText("Score: " + gameLogic.getScore(),
                canvas.getWidth() / 2 - 80, canvas.getHeight() / 2 + 20);
    }

    private void drawGameWin() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GREEN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.fillText("YOU WIN",
                canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 - 40);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        gc.fillText("Score: " + gameLogic.getScore(),
                canvas.getWidth() / 2 - 80, canvas.getHeight() / 2 + 20);
    }

    private void drawEndGame() {
        if (opponentDisconnected) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 34));
            gc.fillText("Opponent disconnected",
                    canvas.getWidth() / 2 - 190, canvas.getHeight() / 2 - 20);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.fillText("Session ended",
                    canvas.getWidth() / 2 - 90, canvas.getHeight() / 2 + 20);
            return;
        }
        
        // Mostrar resultado final cuando ambos jugadores estén muertos
        if (showFinalResult) {
            drawFinalResult();
            return;
        }
        
        // Mostrar pantalla de espera si el jugador está muerto pero el rival no
        if (playerIsDead && !opponentDead) {
            drawWaitingForOpponent();
            return;
        }
        
        if (gameEnded) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (playerWon)
                drawGameWin();
            else
                drawGameOver();
        }
    }
    
    private void drawWaitingForOpponent() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText("Esperando al oponente...",
                canvas.getWidth() / 2 - 220, canvas.getHeight() / 2 - 80);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("Tu Puntaje: " + gameLogic.getScore(),
                canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 20);
        gc.fillText("Tu Nivel: " + gameLogic.getLevel(),
                canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 + 80);
    }
    
    private void drawFinalResult() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        int myScore = gameLogic.getScore();
        int myLevel = gameLogic.getLevel();
        boolean isWin = myScore > opponentScore;
        boolean isDraw = myScore == opponentScore;
        
        // Título del resultado
        if (isDraw) {
            gc.setFill(Color.CYAN);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            gc.fillText("DRAW", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 - 100);
        } else if (isWin) {
            gc.setFill(Color.LIME);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            gc.fillText("WIN", canvas.getWidth() / 2 - 80, canvas.getHeight() / 2 - 100);
        } else {
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            gc.fillText("LOST", canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 - 100);
        }
        
        // Información del jugador
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.fillText("Your Score: " + myScore, canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 - 20);
        gc.fillText("Your Level: " + myLevel, canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 20);
        
        // Información del rival
        gc.setFill(Color.CORNFLOWERBLUE);
        gc.fillText("Rival Score: " + opponentScore, canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 80);
        gc.fillText("Rival Level: " + opponentLevel, canvas.getWidth() / 2 - 150, canvas.getHeight() / 2 + 120);
        
        // Botón de menú
        drawMenuButton();
    }
    
    private double menuButtonX = 0;
    private double menuButtonY = 0;
    private double menuButtonWidth = 180;
    private double menuButtonHeight = 50;
    
    private void drawMenuButton() {
        menuButtonX = canvas.getWidth() / 2 - menuButtonWidth / 2;
        menuButtonY = canvas.getHeight() - 100;
        
        gc.setFill(Color.rgb(50, 50, 50));
        gc.fillRoundRect(menuButtonX, menuButtonY, menuButtonWidth, menuButtonHeight, 10, 10);
        
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRoundRect(menuButtonX, menuButtonY, menuButtonWidth, menuButtonHeight, 10, 10);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Menu", menuButtonX + 60, menuButtonY + 35);
    }

    // ── Carga de imágenes ─────────────────────────────────────

    private Image loadAvatarImage(String avatarId) {
        if (avatarId == null || avatarId.isBlank())
            avatarId = "character_1";
        try {
            return new Image(getClass().getResourceAsStream(
                    "/assets/characters/" + avatarId + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    private Image loadMapImage(String mapName) {
        if (mapName == null)
            return null;
        String mapId = mapName.toLowerCase().replace(" ", "_");
        try {
            return new Image(getClass().getResourceAsStream(
                    "/assets/backgrounds/" + mapId + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helper colores fallback ───────────────────────────────

    private Color typeColor(logic.AttackType type) {
        return switch (type) {
            case YELLOW -> Color.web("#ffd232");
            case RED -> Color.web("#ff3c3c");
            case BLUE -> Color.web("#00aaff");
        };
    }
}