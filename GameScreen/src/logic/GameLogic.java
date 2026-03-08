package logic;

import java.awt.Color;

public class GameLogic {
    private int rectX = 150;
    private int rectY = 600;
    private int rectWidth = 50;
    private int rectHeight = 50;
    private int speed = 10;
    private Bullet[] bullets;
    private int bulletCount = 0;

    public GameLogic() {
        bullets = new Bullet[100];
    }

    public void moveLeft() {
        rectX -= speed;
    }

    public void moveRight() {
        rectX += speed;
    }

    public void shootYellow() {
        shoot(Color.YELLOW);
    }

    public void shootRed() {
        shoot(Color.RED);
    }

    public void shootBlue() {
        shoot(Color.BLUE);
    }

    private void shoot(Color color) {
        int centerX = rectX + rectWidth / 2;
        int topY = rectY;

        if (bulletCount < bullets.length) {
            bullets[bulletCount] = new Bullet(centerX, topY, color);
            bulletCount++;
        }
    }

    public void update(int panelWidth, int panelHeight) {
        if (rectX < 0) {
            rectX = 0;
        }
        if (rectX + rectWidth > panelWidth) {
            rectX = panelWidth - rectWidth;
        }

        // Actualizar balas y eliminar las que salen de pantalla
        for (int i = 0; i < bulletCount; i++) {
            bullets[i].update();
        }
        for (int i = 0; i < bulletCount; i++) {

            if (bullets[i].isOutOfBounds(panelHeight)) {

                for (int j = i; j < bulletCount - 1; j++) {
                    bullets[j] = bullets[j + 1];
                }

                bulletCount--;
                i--;
            }
        }
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

    public Bullet[] getBullets() {
        return bullets;
    }

    public int getBulletCount() {
        return bulletCount;
    }
}