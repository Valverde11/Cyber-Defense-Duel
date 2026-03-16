package logic;

import javafx.scene.image.Image;

public class Bullet {
    private int x;
    private int y;
    private int width = 32;
    private int height = 32;
    private int speed = 8;
    private AttackType type;
    private Image sprite;

    public Bullet(int startX, int startY, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        loadSprite();
    }

    private void loadSprite() {
        if (type == AttackType.YELLOW)
            sprite = new Image(getClass().getResourceAsStream("/assets/bullets/Firewall.png"));

        if (type == AttackType.RED)
            sprite = new Image(getClass().getResourceAsStream("/assets/bullets/Antivirus.png"));

        if (type == AttackType.BLUE)
            sprite = new Image(getClass().getResourceAsStream("/assets/bullets/Crypto Shield.png"));
    }

    public Image getSprite() {
        return sprite;
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
    
    public AttackType getType() {
        return type;    
    }

    public boolean isOutOfBounds(int panelHeight) {
        return y < -height;
    }
}
