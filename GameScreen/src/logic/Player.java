package logic;

public class Player {

    private int x;
    private int y;
    private int width = 100;
    private int height = 100;
    private int speed = 15;

    private int hp;

    public Player(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;
    }

    public void moveLeft() {
        x -= speed;
    }

    public void moveRight() {
        x += speed;
    }

    public void update(int panelWidth) {

        if (x < 0) {
            x = 0;
        }

        if (x + width > panelWidth) {
            x = panelWidth - width;
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void takeDamage(int damage) {
        hp -= damage;
    }

    public int getHp() {
        return hp;
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

    public void clamp(int panelWidth) {
        if (x < 0) x = 0;
        if (x + width > panelWidth) x = panelWidth - width;
    }
}
