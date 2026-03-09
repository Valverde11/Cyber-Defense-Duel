package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import logic.GameLogic;
import logic.AttackType;
import logic.Bullet;
import logic.Enemy;

public class GamePanel extends JPanel {
    private GameLogic gameLogic;
    private boolean aPressed = false;
    private boolean dPressed = false;
    private Image backgroundImage;
    private Image playerImage;
    private boolean playerPositioned = false;

    public GamePanel() {
        gameLogic = new GameLogic();
        setFocusable(true);
        backgroundImage = new ImageIcon(getClass().getResource("/assets/backgrounds/data_center_dojo.png")).getImage();
        playerImage = new ImageIcon(getClass().getResource("/assets/characters/character_1.png")).getImage();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    aPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_Q) {
                    gameLogic.shootYellow();
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    gameLogic.shootRed();
                }
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    gameLogic.shootBlue();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    aPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    dPressed = false;
                }
            }
        });

        Timer timer = new Timer(16, e -> {
            if (!playerPositioned && getWidth() > 0) {
                gameLogic.centerPlayer(getWidth(), getHeight());
                playerPositioned = true;
            }
            if (aPressed) {
                gameLogic.moveLeft();
            }
            if (dPressed) {
                gameLogic.moveRight();
            }
            gameLogic.spawnEnemy(getWidth());
            gameLogic.update(getWidth(), getHeight());
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dibujar fondo
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Dibujar jugador
        g2d.drawImage(playerImage, gameLogic.getPlayer().getX(), gameLogic.getPlayer().getY(), gameLogic.getPlayer().getWidth(), gameLogic.getPlayer().getHeight(), this);

        // Dibujar balas
        Bullet[] bullets = gameLogic.getBullets();

        for (int i = 0; i < gameLogic.getBulletCount(); i++) {
            Bullet bullet = bullets[i];

            if (bullet.getType() == AttackType.YELLOW)
                g2d.setColor(Color.YELLOW);

            if (bullet.getType() == AttackType.RED)
                g2d.setColor(Color.RED);

            if (bullet.getType() == AttackType.BLUE)
                g2d.setColor(Color.BLUE);

            g2d.fillRect(
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
                g2d.setColor(Color.YELLOW);
            if (enemy.getType() == AttackType.RED)
                g2d.setColor(Color.RED);
            if (enemy.getType() == AttackType.BLUE)
                g2d.setColor(Color.BLUE);
            g2d.fillRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
            }

        // Dibujar instrucciones
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("A/D: Mover | Q: Bala Amarilla | W: Bala Roja | E: Bala Azul", 50, 50);
    }
}