package logic;

import audio.AudioManager;

public class GameLogic {
    private GameConfig config;
    private Player player;
    private Bullet[] bullets;
    private int bulletCount = 0;
    private Enemy[] enemies;
    private int enemyCount = 0;
    private long lastShotTime = 0;
    private final long shootCooldown = 300;
    private int lastLevel = 0;
    private int score = 0;
    private int yellowKills = 0;
    private int redKills = 0;
    private int blueKills = 0;

    public GameLogic(GameConfig config) {
        this.config = config;
        bullets = new Bullet[100];
        enemies = new Enemy[50];
        player = new Player(150, 1200, config.initialHp);
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
        AudioManager.playSound("/sounds/smb_fireball.wav");
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
        updatePlayer(panelWidth);
        updateBullets(panelHeight);
        updateEnemies(panelHeight);
        checkBulletCollisions();
        checkPlayerCollisions();
        int currentLevel = getLevel();
            if (currentLevel > lastLevel) {
                AudioManager.playSound("/sounds/smb_1-up.wav");
                lastLevel = currentLevel;
            }        
    }

    private void updatePlayer(int panelWidth) {
        player.update(panelWidth);
    }

    private void updateBullets(int panelHeight) {
        // Mover balas
        for (int i = 0; i < bulletCount; i++) {
            bullets[i].update();
        }

        // Eliminar balas fuera de pantalla
        for (int i = 0; i < bulletCount; i++) {
            if (bullets[i].isOutOfBounds(panelHeight)) {
                removeBullet(i);
                i--;
            }
        }
    }

    private void updateEnemies(int panelHeight) {
        // Mover enemigos
        for (int i = 0; i < enemyCount; i++) {
            enemies[i].update();
        }

        // Eliminar enemigos fuera de pantalla
        for (int i = 0; i < enemyCount; i++) {
            if (enemies[i].isOutOfBounds(panelHeight)) {
                removeEnemy(i);
                i--;
            }
        }
    }

    // ── Colisiones ────────────────────────────────────────────

    private void checkBulletCollisions() {
        for (int i = 0; i < bulletCount; i++) {
            Bullet bullet = bullets[i];
            for (int j = 0; j < enemyCount; j++) {
                Enemy enemy = enemies[j];
                boolean collision = bullet.getX() < enemy.getX() + enemy.getWidth() &&
                        bullet.getX() + bullet.getWidth() > enemy.getX() &&
                        bullet.getY() < enemy.getY() + enemy.getHeight() &&
                        bullet.getY() + bullet.getHeight() > enemy.getY();

                if (collision && bullet.getType() == enemy.getType()) {
                    switch (enemy.getType()) {
                        case YELLOW:
                            yellowKills++;
                            break;
                        case RED:
                            redKills++;
                            break;
                        case BLUE:
                            blueKills++;
                            break;
                    }
                    removeEnemy(j);
                    removeBullet(i);
                    score += config.scorePerKill;
                    AudioManager.playSound("/sounds/smb_kick.wav");
                    i--;
                    break;
                }
            }
        }
    }

    private void checkPlayerCollisions() {
        for (int i = 0; i < enemyCount; i++) {
            Enemy enemy = enemies[i];
            if (collision(enemy, player)) {
                int damage = 0;
                switch (enemy.getType()) {
                    case YELLOW:
                        damage = config.damageYellow;
                        break;
                    case RED:
                        damage = config.damageRed;
                        break;
                    case BLUE:
                        damage = config.damageBlue;
                        break;
                }
                player.damage(damage);
                removeEnemy(i);
                AudioManager.playSound("/sounds/smb_fireworks.wav");
                i--;
            }
        }
    }

    private boolean collision(Enemy e, Player p) {
        return e.getX() < p.getX() + p.getWidth() &&
                e.getX() + e.getWidth() > p.getX() &&
                e.getY() < p.getY() + p.getHeight() &&
                e.getY() + e.getHeight() > p.getY();
    }

    // ── Spawn de enemigos ─────────────────────────────────────

    public void spawnEnemy(int panelWidth) {
        int level = getLevel();
        int startX = (int) (Math.random() * (panelWidth - 40));
        double speed = config.baseAttackSpeed + config.speedAddPerLevel * level;
        AttackType type = AttackType.values()[(int) (Math.random() * AttackType.values().length)];
        if (enemyCount < enemies.length) {
            enemies[enemyCount++] = new Enemy(startX, -40, (int) speed, type);
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

    // ── Estado del juego ──────────────────────────────────────
    public boolean isGameOver() {
        return player.getHp() <= 0;
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

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return score / config.difficultyStepScore;
    }
    public int getYellowKills() {
        return yellowKills;
    }
    public int getRedKills() {
        return redKills;
    }
    public int getBlueKills() {
        return blueKills;
    }
}
