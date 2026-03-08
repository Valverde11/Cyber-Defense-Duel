import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameScreeen extends JFrame {
    private GamePanel gamePanel;

    public GameScreeen() {
        setTitle("Game Screen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameScreeen());
    }
}

class GamePanel extends JPanel {
    private int rectX = 150;  // Posición X del rectángulo
    private int rectY = 620;  // Posición Y del rectángulo
    private int rectWidth = 50;
    private int rectHeight = 50;
    private int speed = 10;   // Velocidad de movimiento

    private boolean aPressed = false;  // Tecla A presionada
    private boolean dPressed = false;  // Tecla D presionada

    public GamePanel() {
        setPreferredSize(new Dimension(550, 700));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Listener para teclas presionadas
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    aPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    dPressed = true;
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

        // Timer para actualizar la posición
        Timer timer = new Timer(30, e -> {
            if (aPressed) {
                rectX -= speed;  // Mover a la izquierda
            }
            if (dPressed) {
                rectX += speed;  // Mover a la derecha
            }

            // Limitar el rectángulo dentro de la pantalla
            if (rectX < 0) {
                rectX = 0;
            }
            if (rectX + rectWidth > getWidth()) {
                rectX = getWidth() - rectWidth;
            }

            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dibujar el rectángulo de color blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(rectX, rectY, rectWidth, rectHeight);

        // Dibujar instrucciones
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Usa A para mover izquierda | Usa D para mover derecha", 50, 50);
    }
}