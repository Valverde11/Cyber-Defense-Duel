package logic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private int rectX = 150;
    private int rectY = 600;
    private int rectWidth = 50;
    private int rectHeight = 50;
    private int speed = 10;
    private List<Bullet> bullets;

    public GameLogic() {
        bullets = new ArrayList<>();
    }

    public void moveLeft() {
        rectX -= speed;
    }

    public void moveRight() {
        rectX += speed;
    }

    public void shootYellow() {
        int centerX = rectX + rectWidth / 2;
        int topY = rectY;
        bullets.add(new Bullet(centerX, topY, Color.YELLOW));
    }

    public void shootRed() {
        int centerX = rectX + rectWidth / 2;
        int topY = rectY;
        bullets.add(new Bullet(centerX, topY, Color.RED));
    }

    public void shootBlue() {
        int centerX = rectX + rectWidth / 2;
        int topY = rectY;
        bullets.add(new Bullet(centerX, topY, Color.BLUE));
    }

    public void update(int panelWidth, int panelHeight) {
        if (rectX < 0) {
            rectX = 0;
        }
        if (rectX + rectWidth > panelWidth) {
            rectX = panelWidth - rectWidth;
        }

        // Actualizar balas y eliminar las que salen de pantalla
        for (Bullet bullet : bullets) {
            bullet.update();
        }
        bullets.removeIf(b -> b.isOutOfBounds(panelHeight));
    }

    public int getRectX() {
        return rectX;
    }

    public int getRectY() {
        return rectY;
    }

    public int getRectWidth() {
        return rectWidth;
    }

    public int getRectHeight() {
        return rectHeight;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }
}