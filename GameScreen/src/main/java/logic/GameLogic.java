package logic;

public class GameLogic {

    private Player player;
    private Bullet[] bullets;
    private int bulletCount = 0;
    private Enemy[] enemies;
    private int enemyCount = 0;
    private long lastShotTime = 0;
    private final long shootCooldown = 300;

    private static final int SCORE_PER_KILL = 10;

    public GameLogic() {
        bullets = new Bullet[100];
        enemies = new Enemy[50];
        player = new Player(150, 1200, 100);
    }

    // ── Movimiento ────────────────────────────────────────────

    public void moveLeft() {
        player.moveLeft();
    }

    public void moveRight() {
        player.moveRight();
    }

    // ── Disparo ───────────────────────────────────────────────

    public void shootYellow() {
        shoot(AttackType.YELLOW);
    }

    public void shootRed() {
        shoot(AttackType.RED);
    }

    public void shootBlue() {
        shoot(AttackType.BLUE);
    }

    private void shoot(AttackType type) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < shootCooldown)
            return;
        lastShotTime = now;
        int centerX = player.getX() + player.getWidth() / 2;
        int topY = player.getY();
        if (bulletCount < bullets.length) {
            bullets[bulletCount++] = new Bullet(centerX, topY, type);
        }
    }

    // ── Posición inicial del jugador ──────────────────────────

    public void centerPlayer(int panelWidth, int panelHeight) {
        int margin = (int) (panelHeight * 0.1);
        int x = panelWidth / 2 - player.getWidth() / 2;
        int y = panelHeight - player.getHeight() - margin;
        player.setPosition(x, y);
    }

    // ── Update principal ──────────────────────────────────────

    public void update(int panelWidth, int panelHeight) {
        player.update(panelWidth);

        // Mover balas
        for (int i = 0; i < bulletCount; i++)
            bullets[i].update();

        // Eliminar balas fuera de pantalla
        for (int i = 0; i < bulletCount; i++) {
            if (bullets[i].isOutOfBounds(panelHeight)) {
                removeBullet(i);
                i--;
            }
        }

        // Mover enemigos
        for (int i = 0; i < enemyCount; i++)
            enemies[i].update();

        // Eliminar enemigos fuera de pantalla
        for (int i = 0; i < enemyCount; i++) {
            if (enemies[i].isOutOfBounds(panelHeight)) {
                removeEnemy(i);
                i--;
            }
        }

        // Colisiones bala-enemigo
        for (int i = 0; i < bulletCount; i++) {
            Bullet b = bullets[i];
            for (int j = 0; j < enemyCount; j++) {
                Enemy e = enemies[j];
                boolean hit = b.getX() < e.getX() + e.getWidth() &&
                        b.getX() + b.getWidth() > e.getX() &&
                        b.getY() < e.getY() + e.getHeight() &&
                        b.getY() + b.getHeight() > e.getY();

                if (hit && b.getType() == e.getType()) {
                    removeEnemy(j);
                    removeBullet(i);
                    player.addScore(SCORE_PER_KILL);
                    i--;
                    break;
                }
            }
        }
    }

    // ── Spawn de enemigos ─────────────────────────────────────

    public void spawnEnemy(int panelWidth) {
        int x = (int) (Math.random() * (panelWidth - 40));
        int speed = 2 + (int) (Math.random() * 3);
        AttackType type = AttackType.values()[(int) (Math.random() * AttackType.values().length)];
        if (enemyCount < enemies.length) {
            enemies[enemyCount++] = new Enemy(x, -40, speed, type);
        }
    }

    // ── Helpers privados ──────────────────────────────────────

    private void removeBullet(int index) {
        for (int i = index; i < bulletCount - 1; i++)
            bullets[i] = bullets[i + 1];
        bulletCount--;
    }

    private void removeEnemy(int index) {
        for (int i = index; i < enemyCount - 1; i++)
            enemies[i] = enemies[i + 1];
        enemyCount--;
    }

    // ── Getters ───────────────────────────────────────────────

    public Player getPlayer() {
        return player;
    }

    public Bullet[] getBullets() {
        return bullets;
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public Enemy[] getEnemies() {
        return enemies;
    }

    public int getEnemyCount() {
        return enemyCount;
    }
}