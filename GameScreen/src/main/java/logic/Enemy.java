package logic;

import javafx.scene.image.Image;

public class Enemy {
    private int x;
    private int y;
    private int width = 64;
    private int height = 64;
    private int speed;
    private AttackType type;
    private Image sprite;

    public Enemy(int startX, int startY, int speed, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.type = type;

        loadSprite();
    }

    private void loadSprite() {
        if (type == AttackType.YELLOW)
            sprite = new Image(getClass().getResourceAsStream("/assets/enemies/DDoS.png"));

        if (type == AttackType.RED)
            sprite = new Image(getClass().getResourceAsStream("/assets/enemies/Malware.png"));

        if (type == AttackType.BLUE)
            sprite = new Image(getClass().getResourceAsStream("/assets/enemies/Credential Attack.png"));
        }

    public Image getSprite() {
        return sprite;
    }

    public void update() {
        y += speed;  
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

    public AttackType getType() {
        return type;
    }

    public boolean isOutOfBounds(int panelHeight) {
        return y > panelHeight;
    }
}