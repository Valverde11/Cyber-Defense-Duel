package logic;

import java.awt.Color;

public class Bullet {
    private int x;
    private int y;
    private int width = 10;
    private int height = 10;
    private int speed = 8;
    private Color color;

    public Bullet(int startX, int startY, Color color) {
        this.x = startX;
        this.y = startY;
        this.color = color;
    }

    public void update() {
        y -= speed;  // Las balas suben
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public boolean isOutOfBounds(int panelHeight) {
        return y < -height;
    }
}
