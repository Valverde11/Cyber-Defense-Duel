package ui;

import javax.swing.*;

public class GameScreen extends JFrame {
    private GamePanel gamePanel;

    public GameScreen() {
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
        SwingUtilities.invokeLater(() -> new GameScreen());
    }
}