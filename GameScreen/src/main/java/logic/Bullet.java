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
    String path = switch (type) {
        case YELLOW -> "/assets/bullets/firewall.png";
        case RED    -> "/assets/bullets/antivirus.png";
        case BLUE   -> "/assets/bullets/crypto_shield.png";
    };
    try {
        sprite = new Image(getClass().getResourceAsStream(path));
    } catch (Exception e) {
        sprite = null;
    }
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
