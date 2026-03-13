package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;

import logic.GameLogic;
import logic.AttackType;
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

        gameLogic.spawnEnemy((int) canvas.getWidth());

        gameLogic.update((int) canvas.getWidth(), (int) canvas.getHeight());
    }

    private void render() {

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Player player = gameLogic.getPlayer();

        gc.setFill(Color.GREEN);
        gc.fillRect(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight());

        Bullet[] bullets = gameLogic.getBullets();

        for (int i = 0; i < gameLogic.getBulletCount(); i++) {

            Bullet bullet = bullets[i];

            if (bullet.getType() == AttackType.YELLOW)
                gc.setFill(Color.YELLOW);

            if (bullet.getType() == AttackType.RED)
                gc.setFill(Color.RED);

            if (bullet.getType() == AttackType.BLUE)
                gc.setFill(Color.BLUE);

            gc.fillRect(
                bullet.getX() - bullet.getWidth() / 2,
                bullet.getY(),
                bullet.getWidth(),
                bullet.getHeight()
            );
        }

        Enemy[] enemies = gameLogic.getEnemies();

        for (int i = 0; i < gameLogic.getEnemyCount(); i++) {

            Enemy enemy = enemies[i];

            if (enemy.getType() == AttackType.YELLOW)
                gc.setFill(Color.YELLOW);

            if (enemy.getType() == AttackType.RED)
                gc.setFill(Color.RED);

            if (enemy.getType() == AttackType.BLUE)
                gc.setFill(Color.BLUE);

            gc.fillRect(
                enemy.getX(),
                enemy.getY(),
                enemy.getWidth(),
                enemy.getHeight()
            );
        }
    }
}