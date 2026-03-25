package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import audio.AudioManager;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.GameLogic;
import logic.Bullet;
import logic.Enemy;
import logic.GameConfig;
import logic.Player;

public class GameView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;
    private GameLogic gameLogic;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean playerPositioned = false;
    private GameConfig config;

    private long lastEnemySpawn = 0;
    private long getSpawnCooldown() {
        int level = gameLogic.getLevel();
        double multiplier = Math.pow(config.spawnMultiplierPerLevel, level);
        return Math.max(100, (long) (500 / multiplier));
    }

    private boolean playerWon = false;
    private boolean gameEnded = false;
    private boolean opponentDead = false;

    public GameView(GameConfig config) {
        this.config = config;
        canvas = new Canvas(1280, 720);
        gc = canvas.getGraphicsContext2D();
        gameLogic = new GameLogic(config);
        getChildren().add(canvas);
        setFocusTraversable(true);
        setupControls();
        AudioManager.playMusic("/sounds/Coconut Mall - Mario Kart Wii OST.mp3");
    }

    // ── Teclado ───────────────────────────────────────────────

    private void setupControls() {
        setOnKeyPressed(e -> {
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
        if (gameEnded) return;

        if (!playerPositioned) {
            gameLogic.centerPlayer((int) canvas.getWidth(), (int) canvas.getHeight());
            playerPositioned = true;
        }
        if (leftPressed)
            gameLogic.moveLeft();
        if (rightPressed)
            gameLogic.moveRight();

        spawnEnemies();

        gameLogic.update((int) canvas.getWidth(), (int) canvas.getHeight());

        if (gameLogic.getScore() >= 100 && !opponentDead) {
            opponentDead = true;
        }

        if (gameLogic.isGameOver()) {
            if(opponentDead) {
                triggerGameWin();
                AudioManager.stopMusic();
                AudioManager.playSound("/sounds/smb_stage_clear.wav");
            } else {
                triggerGameOver();
                AudioManager.stopMusic();
                AudioManager.playSound("/sounds/smb_gameover.wav");
            }
        }

    }

    // ── Game over y Game win ───────────────────────────────────────────
    private void triggerGameOver() {
        gameEnded = true;
        playerWon = false;
    }

    private void triggerGameWin() {
        gameEnded = true;
        playerWon = true;
    }

    // ── Renderizado ───────────────────────────────────────────

    private void render() {
        drawBackground();
        drawPlayer();
        drawBullets();
        drawEnemies();
        drawHealthBar(gc);
        drawScore(gc);
        drawLevel(gc);
        drawEndGame();
    }

    private void drawBackground() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawPlayer() {
        Player player = gameLogic.getPlayer();

        if (player.isInvulnerable()) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.GREEN);
        }

        gc.fillRect(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight());
    }

    private void drawBullets() {
        Bullet[] bullets = gameLogic.getBullets();

        for (int i = 0; i < gameLogic.getBulletCount(); i++) {
            Bullet bullet = bullets[i];

            gc.drawImage(
                    bullet.getSprite(),
                    bullet.getX(),
                    bullet.getY(),
                    bullet.getWidth(),
                    bullet.getHeight());
        }
    }

    private void drawEnemies() {
        Enemy[] enemies = gameLogic.getEnemies();

        for (int i = 0; i < gameLogic.getEnemyCount(); i++) {
            Enemy enemy = enemies[i];

            gc.drawImage(
                    enemy.getSprite(),
                    enemy.getX(),
                    enemy.getY(),
                    enemy.getWidth(),
                    enemy.getHeight());
        }
    }

    private void drawHealthBar(GraphicsContext gc) {
        Player player = gameLogic.getPlayer();

        double percent = (double) player.getHp() / player.getMaxHp();

        double barWidth = 300;
        double barHeight = 25;

        double x = 20;
        double y = 20;

        // fondo
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

        // color según vida
        Color healthColor;

        if (percent > 0.6)
            healthColor = Color.LIMEGREEN;
        else if (percent > 0.3)
            healthColor = Color.ORANGE;
        else
            healthColor = Color.RED;

        // vida actual
        gc.setFill(healthColor);
        gc.fillRoundRect(x, y, barWidth * percent, barHeight, 10, 10);

        // borde
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, barWidth, barHeight, 10, 10);

        // texto de vida
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText(
                player.getHp() + " / " + player.getMaxHp(),
                x + barWidth / 2 - 20,
                y + 17);
    }

    private void drawScore(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Score: " + gameLogic.getScore(), canvas.getWidth() - 160, 40);
    }

    private void drawLevel(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.fillText("Level: " + gameLogic.getLevel(), canvas.getWidth() - 160, 70);
    }

    private void spawnEnemies() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastEnemySpawn >= getSpawnCooldown()) {
            gameLogic.spawnEnemy((int) canvas.getWidth());
            lastEnemySpawn = currentTime;
        }
    }

    private void drawGameOver() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.RED);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));

        gc.fillText("GAME OVER", canvas.getWidth() / 2 - 180, canvas.getHeight() / 2 - 40);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));

        gc.fillText(
            "Score: " + gameLogic.getScore(),
            canvas.getWidth() / 2 - 80,
            canvas.getHeight() / 2 + 20
        );
    }

    private void drawGameWin() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.GREEN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));

        gc.fillText("YOU WIN", canvas.getWidth() / 2 - 120, canvas.getHeight() / 2 - 40);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));

        gc.fillText(
            "Score: " + gameLogic.getScore(),
            canvas.getWidth() / 2 - 80,
            canvas.getHeight() / 2 + 20
        );
    }

    private void drawEndGame() {
        if (opponentDead && !gameEnded) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.fillText(
                "Opponent defeated! Keep playing...",
                canvas.getWidth() / 2 - 180,
                100
            );
        }

        if (gameEnded) {

            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            if (playerWon) {
                drawGameWin();
            } else {
                drawGameOver();
            }
        }
    }
}
