package logic;

public class Player {

    private int x;
    private int y;
    private int width = 100;
    private int height = 100;
    private int speed = 7;

    private int hp;
    private int maxHp;

    private long lastDamageTime = 0;
    private final long invulnerabilityTime = 800;

    public Player(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
    }

    public void damage(int amount) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime < invulnerabilityTime) {
            return;
        }
        hp -= amount;
        if (hp < 0) {
            hp = 0;
        }
    }

    public boolean isInvulnerable() {
        return System.currentTimeMillis() - lastDamageTime < invulnerabilityTime;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void moveLeft() {
        x -= speed;
    }

    public void moveRight() {
        x += speed;
    }

    public void update(int panelWidth) {
        if (x < 0)
            x = 0;
        if (x + width > panelWidth)
            x = panelWidth - width;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
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
}