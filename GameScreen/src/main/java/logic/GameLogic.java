package logic;

import audio.AudioManager;

public class GameLogic {
    private GameConfig config;                  // Configuración de daños y puntuación
    private Player player;                      // Jugador principal
    private Bullet[] bullets;                   // Array de balas en juego
    private int bulletCount = 0;                // Número actual de balas activas
    private Enemy[] enemies;                    // Array de enemigos en juego
    private int enemyCount = 0;                 // Número actual de enemigos activos
    private long lastShotTime = 0;              // Timestamp del último disparo (para cooldown)
    private final long shootCooldown = 300;     // Milisegundos entre disparos
    private int lastLevel = 0;                  // Nivel anterior (para detectar subida y sonar)
    private int score = 0;                      // Puntuación total
    private int yellowKills = 0, redKills = 0, blueKills = 0; // Contadores de muertes por tipo

    public GameLogic(GameConfig config) {
        this.config = config;
        bullets = new Bullet[100];               // Maximo 100 balas en pantalla
        enemies = new Enemy[50];                 // Maximo 50 enemigos simultaneos
        player = new Player(150, 1200, config.initialHp); // Posicion inicial del jugador
    }

    // ── Movimiento ────────────────────────────────────────────

    public void moveLeft()  { player.moveLeft(); }
    public void moveRight() { player.moveRight(); }

    // ── Disparo ───────────────────────────────────────────────

    public void shootYellow() { shoot(AttackType.YELLOW); }
    public void shootRed()    { shoot(AttackType.RED); }
    public void shootBlue()   { shoot(AttackType.BLUE); }

    private void shoot(AttackType type) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < shootCooldown) return; // Espera el cooldown
        lastShotTime = now; // Actualiza tiempo de ultimo disparo

        // Centrar la bala respecto al jugador
        int centerX = player.getX() + player.getWidth() / 2; // Centro horizontal P
        int topY = player.getY(); // Origen en tope del jugador

        if (bulletCount < bullets.length) {
            bullets[bulletCount++] = new Bullet(centerX, topY, type); // Crea nueva bala
        }
        AudioManager.playSound("/sounds/smb_fireball.wav"); // Efecto de sonido
    }

    // ── Posición inicial del jugador ──────────────────────────

    public void centerPlayer(int panelWidth, int panelHeight) {
        int margin = (int) (panelHeight * 0.1);      // 10% de margen inferior
        int x = panelWidth / 2 - player.getWidth() / 2; // Centra horizontalmente
        int y = panelHeight - player.getHeight() - margin; // Posiciona en base
        player.setPosition(x, y);
    }

    // ── Update principal (llamado en cada frame) ──────────────

    public void update(int panelWidth, int panelHeight) {
        updatePlayer(panelWidth); // Actualiza restricciones del jugador
        updateBullets(panelHeight); // Mueve balas y las elimina si salen
        updateEnemies(panelHeight); // Mueve enemigos y los elimina si salen
        checkBulletCollisions(); // Detecta colisiones bala-enemigo
        checkPlayerCollisions(); // Detecta colisiones jugador-enemigo

        // Detectar subida de nivel y sonar efecto
        int currentLevel = getLevel();
        if (currentLevel > lastLevel) { // Nuevo nivel alcanzado
            AudioManager.playSound("/sounds/smb_1-up.wav"); // Sonido de nivel
            lastLevel = currentLevel; // Actualiza nivel anterior
        }
    }

    private void updatePlayer(int panelWidth) {
        player.update(panelWidth); // Limita posición dentro del panel
    }

    private void updateBullets(int panelHeight) {
        // Mover todas las balas
        for (int i = 0; i < bulletCount; i++) {
            bullets[i].update();
        }
        // Eliminar las que salieron de la pantalla
        for (int i = 0; i < bulletCount; i++) {
            if (bullets[i].isOutOfBounds(panelHeight)) {
                removeBullet(i);
                i--;
            }
        }
    }

    private void updateEnemies(int panelHeight) {
        // Mover todos los enemigos (bajan)
        for (int i = 0; i < enemyCount; i++) {
            enemies[i].update();
        }
        // Eliminar los que salieron por abajo
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
                // Colision AABB (cajas alineadas a ejes)
                boolean collision = bullet.getX() < enemy.getX() + enemy.getWidth() &&
                                    bullet.getX() + bullet.getWidth() > enemy.getX() &&
                                    bullet.getY() < enemy.getY() + enemy.getHeight() &&
                                    bullet.getY() + bullet.getHeight() > enemy.getY();

                // Solo daña si el color coincide
                if (collision && bullet.getType() == enemy.getType()) { // Validacion de tipo
                    // Contabilizar muerte segun tipo
                    switch (enemy.getType()) {
                        case YELLOW: yellowKills++; break; // Suma kill por tipo de enemigo
                        case RED:    redKills++;    break;
                        case BLUE:   blueKills++;   break;
                    }
                    removeEnemy(j); // Elimina enemigo
                    removeBullet(i); // Elimina bala
                    score += config.scorePerKill; // Suma puntos
                    AudioManager.playSound("/sounds/smb_kick.wav"); // Sonido de golpe
                    i--; // Salir del bucle interno y seguir con la siguiente bala
                    break;
                }
            }
        }
    }

    private void checkPlayerCollisions() {
        for (int i = 0; i < enemyCount; i++) {
            Enemy enemy = enemies[i];
            if (collision(enemy, player)) {
                int damage = switch (enemy.getType()) {
                    case YELLOW -> config.damageYellow;
                    case RED    -> config.damageRed;
                    case BLUE   -> config.damageBlue;
                };
                player.damage(damage);
                removeEnemy(i);
                AudioManager.playSound("/sounds/smb_fireworks.wav"); // Sonido de daño
                i--;
            }
        }
    }

    // Método auxiliar para colisión entre Enemy y Player
    private boolean collision(Enemy e, Player p) {
        return e.getX() < p.getX() + p.getWidth() &&
               e.getX() + e.getWidth() > p.getX() &&
               e.getY() < p.getY() + p.getHeight() &&
               e.getY() + e.getHeight() > p.getY();
    }

    // ── Spawn de enemigos ─────────────────────────────────────

    public void spawnEnemy(int panelWidth) {
        int level = getLevel();
        int startX = (int) (Math.random() * (panelWidth - 40));   // Posicion X aleatoria
        double speed = config.baseAttackSpeed + config.speedAddPerLevel * level; // Velocidad aumenta con nivel
        AttackType type = AttackType.values()[(int) (Math.random() * AttackType.values().length)]; // Tipo random

        if (enemyCount < enemies.length) {
            enemies[enemyCount++] = new Enemy(startX, -40, (int) speed, type); // Crea enemigo fuera de pantalla
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
        return player.getHp() <= 0; // Fin del juego cuando HP llega a cero
    }

    // ── Getters ───────────────────────────────────────────────

    public Player getPlayer() { return player; }
    public Bullet[] getBullets() { return bullets; }
    public int getBulletCount() { return bulletCount; }
    public Enemy[] getEnemies() { return enemies; }
    public int getEnemyCount() { return enemyCount; }
    public int getScore() { return score; }
    public int getLevel() { return score / config.difficultyStepScore; } // Cada X puntos sube nivel
    public int getYellowKills() { return yellowKills; }
    public int getRedKills()    { return redKills; }
    public int getBlueKills()   { return blueKills; }
}