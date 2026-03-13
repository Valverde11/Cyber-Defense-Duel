package logic;

public class Bullet {
    private int x;
    private int y;
    private int width = 10;
    private int height = 10;
    private int speed = 8;
    private AttackType type;

    public Bullet(int startX, int startY, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
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
