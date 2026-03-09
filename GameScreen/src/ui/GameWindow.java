package ui;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    public GameWindow() {

        setTitle("Cyber Defense Duel");

        GamePanel panel = new GamePanel();
        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        setUndecorated(true); 
        device.setFullScreenWindow(this);

        setVisible(true);
    }
}
