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

    public GamePanel() {
        gameLogic = new GameLogic();
        setPreferredSize(new Dimension(500, 650));
        setBackground(Color.BLACK);
        setFocusable(true);

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

        // Dibujar rectángulo
        g2d.setColor(Color.WHITE);
        g2d.fillRect(gameLogic.getRectX(), gameLogic.getRectY(), gameLogic.getRectWidth(), gameLogic.getRectHeight());

        // Dibujar balas
        for (Bullet bullet : gameLogic.getBullets()) {
            g2d.setColor(bullet.getColor());
            g2d.fillRect(bullet.getX() - bullet.getWidth() / 2, bullet.getY(), bullet.getWidth(), bullet.getHeight());
        }

        // Dibujar instrucciones
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("A/D: Mover | Q: Bala Amarilla | W: Bala Roja | E: Bala Azul", 50, 50);
    }
}