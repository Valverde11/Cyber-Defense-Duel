package logic;

import javafx.scene.image.Image;

public class Bullet {
    private int x, y;               // Posición actual de la bala
    private int width = 70;         // Tamaño para detección de colisiones
    private int height = 70;
    private int speed = 8;          // Velocidad hacia arriba (píxeles por frame)
    private AttackType type;        // Color/tipo de bala (afecta a qué enemigo daña)
    private Image sprite;           // Imagen que se dibuja en pantalla

    public Bullet(int startX, int startY, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        loadSprite();               // Carga la imagen correspondiente al tipo
    }

    private void loadSprite() {
        // Elegimos la imagen según el tipo de ataque
        String path = switch (type) {
            case YELLOW -> "/assets/bullets/firewall.png";
            case RED    -> "/assets/bullets/antivirus.png";
            case BLUE   -> "/assets/bullets/crypto_shield.png";
        };
        try {
            sprite = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            sprite = null;          // Si no encuentra la imagen, no se dibuja (evita crashear)
        }
    }

    public Image getSprite() {
        return sprite;
    }

    public void update() {
        y -= speed;                 // La bala se mueve hacia arriba en cada frame
    }

    // Getters básicos para colisiones y renderizado
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public AttackType getType() { return type; }

    // Comprueba si la bala ha salido completamente de la pantalla por arriba
    public boolean isOutOfBounds(int panelHeight) {
        return y < -height;
    }
}