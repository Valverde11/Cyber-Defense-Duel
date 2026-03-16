package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;

import logic.GameLogic;
import logic.Bullet;
import logic.Enemy;
import logic.Player;

public class GameView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;
    private GameLogic gameLogic;

    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private boolean playerPositioned = false;

    private long lastEnemySpawn = 0;
    private final long enemySpawnCooldown = 500; 

    public GameView() {

        canvas = new Canvas(1280, 720);
        gc = canvas.getGraphicsContext2D();

        gameLogic = new GameLogic();

        getChildren().add(canvas);

        setupControls();
    }

    private void setupControls() {

        setOnKeyPressed(e -> {

            switch (e.getCode()) {

                case LEFT -> leftPressed = true;
                case RIGHT -> rightPressed = true;

                case Q -> gameLogic.shootYellow();
                case W -> gameLogic.shootRed();
                case E -> gameLogic.shootBlue();

                default -> {}
            }

        });

        setOnKeyReleased(e -> {

            switch (e.getCode()) {

                case LEFT -> leftPressed = false;
                case RIGHT -> rightPressed = false;

                default -> {}
            }

        });
    }

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
    }

    private void render() {
        
        drawBackground();
        drawPlayer();
        drawBullets();
        drawEnemies();
        drawHealthBar(gc);

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
            player.getHeight()
        );
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
                bullet.getHeight()
            );
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
                enemy.getHeight()
            );
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
        gc.fillText(
            player.getHp() + " / " + player.getMaxHp(),
            x + barWidth / 2 - 20,
            y + 17
        );
    }

    private void spawnEnemies() {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastEnemySpawn >= enemySpawnCooldown) {

            gameLogic.spawnEnemy((int) canvas.getWidth());

            lastEnemySpawn = currentTime;
        }
    }

}