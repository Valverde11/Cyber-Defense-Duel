package logic;

import javafx.scene.image.Image;

public class Enemy {
    private int x, y;                   // Posición del enemigo en pantalla
    private int width = 64;             // Tamaño para colisiones
    private int height = 64;
    private int speed;                  // Velocidad de bajada (píxeles por frame)
    private AttackType type;            // Color/tipo (determina qué balas lo dañan)
    private Image sprite;               // Imagen que se dibuja

    public Enemy(int startX, int startY, int speed, AttackType type) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.type = type;
        loadSprite();                   // Carga el sprite según el tipo de enemigo
    }

    private void loadSprite() {
        // Seleccionamos la imagen correspondiente a cada tipo de ataque
        String path = switch (type) {
            case YELLOW -> "/assets/enemies/ddos.png";
            case RED    -> "/assets/enemies/malware.png";
            case BLUE   -> "/assets/enemies/credential_attack.png";
        };
        try {
            sprite = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            sprite = null;              // Si no se encuentra la imagen, se ignora
        }
    }

    public Image getSprite() {
        return sprite;
    }

    public void update() {
        y += speed;                     // El enemigo baja en cada frame
    }

    // Getters necesarios para colisiones y renderizado
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public AttackType getType() { return type; }

    // Comprueba si el enemigo ha salido completamente de la pantalla por abajo
    public boolean isOutOfBounds(int panelHeight) {
        return y > panelHeight;
    }
}