package logic;

public class Enemy {
    private int x;
    private int y;
    private int width = 40;
    private int height = 40;
    private int speed;
    private AttackType type;

    public Enemy(int startX, int startY, int speed, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.type = type;
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