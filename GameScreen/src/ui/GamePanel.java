package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import logic.GameLogic;
import logic.Bullet;

public class GamePanel extends JPanel {
    private GameLogic gameLogic;
    private boolean aPressed = false;
    private boolean dPressed = false;
    private Image backgroundImage;
    private Image playerImage;

    public GamePanel() {
        gameLogic = new GameLogic();
        setPreferredSize(new Dimension(500, 650));
        setFocusable(true);
        backgroundImage = new ImageIcon(getClass().getResource("/assets/backgrounds/background-p.jpg")).getImage();
        playerImage = new ImageIcon(getClass().getResource("/assets/characters/character_1.png")).getImage();

        System.out.println(backgroundImage);
        System.out.println(playerImage);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    aPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
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
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    aPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    dPressed = false;
                }
            }
        });

        Timer timer = new Timer(30, e -> {
            if (aPressed) {
                gameLogic.moveLeft();
            }
            if (dPressed) {
                gameLogic.moveRight();
            }
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
        g2d.drawImage(playerImage, gameLogic.getRectX(), gameLogic.getRectY(), gameLogic.getRectWidth(), gameLogic.getRectHeight(), this);

        // Dibujar balas
        Bullet[] bullets = gameLogic.getBullets();

        for (int i = 0; i < gameLogic.getBulletCount(); i++) {
            Bullet bullet = bullets[i];

            g2d.setColor(bullet.getColor());
            g2d.fillRect(
                bullet.getX() - bullet.getWidth() / 2,
                bullet.getY(),
                bullet.getWidth(),
                bullet.getHeight()
            );
        }

        // Dibujar instrucciones
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("A/D: Mover | Q: Bala Amarilla | W: Bala Roja | E: Bala Azul", 50, 50);
    }
}