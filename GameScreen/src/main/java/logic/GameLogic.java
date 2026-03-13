package logic;

public class GameLogic {
    private Player player;
    private Bullet[] bullets;
    private int bulletCount = 0;
    private Enemy[] enemies;
    private int enemyCount = 0;
    private long lastShotTime = 0;
    private final long shootCooldown = 300;

    public GameLogic() {
        bullets = new Bullet[100];
        enemies = new Enemy[50];
        player = new Player(150, 1200, 100);
    }

    public void moveLeft() {
        player.moveLeft();
    }

    public void moveRight() {
        player.moveRight();
    }

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
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < shootCooldown) {
            return;
        }
        lastShotTime = currentTime;
        int centerX = player.getX() + player.getWidth() / 2;
        int topY = player.getY();

        if (bulletCount < bullets.length) {
            bullets[bulletCount] = new Bullet(centerX, topY, type);
            bulletCount++;
        }
    }
    public void centerPlayer(int panelWidth, int panelHeight) {

        int margin = (int) (panelHeight * 0.1);

        int x = (panelWidth / 2) - (player.getWidth() / 2);
        int y = panelHeight - player.getHeight() - margin;

        player.setPosition(x, y);
    }

    public void update(int panelWidth, int panelHeight) {
        player.update(panelWidth);

        // Actualizar balas y eliminar las que salen de pantalla
        for (int i = 0; i < bulletCount; i++) {
            bullets[i].update();
        }
        for (int i = 0; i < bulletCount; i++) {

            if (bullets[i].isOutOfBounds(panelHeight)) {

                for (int j = i; j < bulletCount - 1; j++) {
                    bullets[j] = bullets[j + 1];
                }

                bulletCount--;
                i--;
            }
        }
        // actualizar enemigos
        for (int i = 0; i < enemyCount; i++) {
            enemies[i].update();
        }
        // eliminar enemigos fuera de pantalla
        for (int i = 0; i < enemyCount; i++) {
            if (enemies[i].isOutOfBounds(panelHeight)) {
                for (int j = i; j < enemyCount - 1; j++) {
                    enemies[j] = enemies[j + 1];
                }
                enemyCount--;
                i--;
            }
        }
        // colisiones
        for (int i = 0; i < bulletCount; i++) {
            Bullet bullet = bullets[i];
            for (int j = 0; j < enemyCount; j++) {
                Enemy enemy = enemies[j];
                boolean collision =
                        bullet.getX() < enemy.getX() + enemy.getWidth() &&
                        bullet.getX() + bullet.getWidth() > enemy.getX() &&
                        bullet.getY() < enemy.getY() + enemy.getHeight() &&
                        bullet.getY() + bullet.getHeight() > enemy.getY();
                if (collision) {
                    // verificar tipo correcto
                    if (bullet.getType() == enemy.getType()) {
                        removeEnemy(j);
                        removeBullet(i);
                        i--;
                        break;
                    }
                }
            }
        }
    }

    public void spawnEnemy(int panelWidth) {
        int startX = (int) (Math.random() * (panelWidth - 40));
        int startY = -40;
        int speed = 2 + (int) (Math.random() * 3);
        AttackType type = AttackType.values()[(int) (Math.random() * AttackType.values().length)];

        if (enemyCount < enemies.length) {
            enemies[enemyCount] = new Enemy(startX, startY, speed, type);
            enemyCount++;
        }
    }
    private void removeBullet(int index) {
        for (int i = index; i < bulletCount - 1; i++) {
            bullets[i] = bullets[i + 1];
        }
        bulletCount--;
    }

    private void removeEnemy(int index) {
        for (int i = index; i < enemyCount - 1; i++) {
            enemies[i] = enemies[i + 1];
        }
        enemyCount--;
    }
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