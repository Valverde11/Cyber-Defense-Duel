package ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import logic.GameLogic;
import logic.AttackType;
import logic.Bullet;
import logic.Enemy;
import logic.Player;

public class GameView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;
    private GameLogic gameLogic;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean playerPositioned = false;

    // ── Colores ───────────────────────────────────────────────
    private static final Color C_BG = Color.web("#04060e");
    private static final Color C_GRID = Color.web("#001830");
    private static final Color C_ACCENT = Color.web("#00dcff");
    private static final Color C_GOLD = Color.web("#ffd232");
    private static final Color C_GREEN = Color.web("#00dc50");
    private static final Color C_RED = Color.web("#ff3c3c");
    private static final Color C_HUD = Color.color(0, 0, 0, 0.82);

    public GameView() {
        canvas = new Canvas(1280, 720);
        gc = canvas.getGraphicsContext2D();
        gameLogic = new GameLogic();
        getChildren().add(canvas);
        setFocusTraversable(true);
        setupControls();
    }

    // ── Teclado ───────────────────────────────────────────────

    private void setupControls() {
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = true;
                case RIGHT, D -> rightPressed = true;
                case Q -> gameLogic.shootYellow();
                case W -> gameLogic.shootRed();
                case E -> gameLogic.shootBlue();
                default -> {
                }
            }
        });
        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftPressed = false;
                case RIGHT, D -> rightPressed = false;
                default -> {
                }
            }
        });
    }

    // ── Game loop ─────────────────────────────────────────────

    public void startGame() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();
    }

    private void update() {
        if (!playerPositioned) {
            gameLogic.centerPlayer((int) canvas.getWidth(), (int) canvas.getHeight());
            playerPositioned = true;
        }
        if (leftPressed)
            gameLogic.moveLeft();
        if (rightPressed)
            gameLogic.moveRight();
        gameLogic.spawnEnemy((int) canvas.getWidth());
        gameLogic.update((int) canvas.getWidth(), (int) canvas.getHeight());
    }

    // ── Renderizado ───────────────────────────────────────────

    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        drawBackground(w, h);
        drawEnemies();
        drawBullets();
        drawPlayer();
        drawHUD(w, h);
    }

    private void drawBackground(double w, double h) {
        // Fondo base
        gc.setFill(C_BG);
        gc.fillRect(0, 0, w, h);

        // Grid tenue
        gc.setStroke(C_GRID);
        gc.setLineWidth(0.5);
        for (int x = 0; x < w; x += 60)
            gc.strokeLine(x, 0, x, h);
        for (int y = 0; y < h; y += 60)
            gc.strokeLine(0, y, w, y);

        // Línea de suelo neón
        Player p = gameLogic.getPlayer();
        int baseY = p.getY() + p.getHeight() + 10;
        gc.setStroke(Color.color(0, 0.7, 1, 0.25));
        gc.setLineWidth(1);
        gc.strokeLine(0, baseY, w, baseY);
    }

    private void drawEnemies() {
        Enemy[] enemies = gameLogic.getEnemies();
        for (int i = 0; i < gameLogic.getEnemyCount(); i++) {
            Enemy en = enemies[i];
            Color c = typeColor(en.getType());
            int x = en.getX(), y = en.getY();
            int ew = en.getWidth(), eh = en.getHeight();

            // Aura externa
            gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.12));
            gc.fillRoundRect(x - 7, y - 7, ew + 14, eh + 14, 16, 16);

            // Cuerpo semitransparente
            gc.setFill(Color.color(c.getRed() / 3, c.getGreen() / 3, c.getBlue() / 3, 0.85));
            gc.fillRoundRect(x, y, ew, eh, 8, 8);

            // Borde neón
            gc.setStroke(c);
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, y, ew, eh, 8, 8);

            // Etiqueta tecla
            String key = en.getType() == AttackType.YELLOW ? "[Q]"
                    : en.getType() == AttackType.RED ? "[W]" : "[E]";
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
            gc.fillText(key, x + ew / 2.0 - 11, y + eh / 2.0 + 5);
        }
    }

    private void drawBullets() {
        Bullet[] bullets = gameLogic.getBullets();
        for (int i = 0; i < gameLogic.getBulletCount(); i++) {
            Bullet b = bullets[i];
            Color c = typeColor(b.getType());

            // Halo
            gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), 0.28));
            gc.fillRoundRect(b.getX() - 4, b.getY() - 4,
                    b.getWidth() + 8, b.getHeight() + 8, 8, 8);

            // Núcleo
            gc.setFill(c);
            gc.fillRoundRect(b.getX(), b.getY(), b.getWidth(), b.getHeight(), 4, 4);
        }
    }

    private void drawPlayer() {
        Player p = gameLogic.getPlayer();

        // Sombra suelo
        gc.setFill(Color.color(0, 0.7, 1, 0.12));
        gc.fillOval(p.getX() - 12, p.getY() + p.getHeight() - 6,
                p.getWidth() + 24, 14);

        // Cuerpo
        gc.setFill(C_ACCENT);
        gc.fillRoundRect(p.getX(), p.getY(), p.getWidth(), p.getHeight(), 10, 10);

        // Borde blanco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(p.getX(), p.getY(), p.getWidth(), p.getHeight(), 10, 10);
    }

    private void drawHUD(double w, double h) {
        Player p = gameLogic.getPlayer();

        // Fondo HUD superior
        gc.setFill(C_HUD);
        gc.fillRect(0, 0, w, 74);

        // Línea inferior del HUD
        gc.setStroke(Color.color(0, 0.7, 1, 0.25));
        gc.setLineWidth(1);
        gc.strokeLine(0, 74, w, 74);

        // ── HP ────────────────────────────────────────────
        gc.setFill(Color.web("#445566"));
        gc.setFont(Font.font("Courier New", 11));
        gc.fillText("HP", 20, 22);

        double hpRatio = Math.max(0, p.getHp() / 100.0);
        Color hpColor = hpRatio > 0.5 ? C_GREEN : hpRatio > 0.25 ? C_GOLD : C_RED;

        // Fondo barra
        gc.setFill(Color.web("#111a2e"));
        gc.fillRoundRect(20, 28, 200, 14, 4, 4);
        // Relleno HP
        gc.setFill(hpColor);
        gc.fillRoundRect(20, 28, 200 * hpRatio, 14, 4, 4);
        // Borde barra
        gc.setStroke(Color.web("#223344"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(20, 28, 200, 14, 4, 4);
        // Texto HP
        gc.setFill(hpColor);
        gc.setFont(Font.font("Courier New", 11));
        gc.fillText(p.getHp() + " / 100", 26, 58);

        // ── Score centrado ────────────────────────────────
        gc.setFill(Color.web("#445566"));
        gc.setFont(Font.font("Courier New", 10));
        gc.fillText("SCORE", w / 2 - 22, 20);

        gc.setFill(C_GOLD);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        gc.fillText(String.format("%06d", p.getScore()), w / 2 - 52, 52);

        // ── Controles lado derecho ────────────────────────
        gc.setFont(Font.font("Courier New", 11));
        gc.setFill(typeColor(AttackType.YELLOW));
        gc.fillText("[Q] Firewall    → DDoS", w - 270, 22);
        gc.setFill(typeColor(AttackType.RED));
        gc.fillText("[W] Antivirus   → Malware", w - 270, 40);
        gc.setFill(typeColor(AttackType.BLUE));
        gc.fillText("[E] CryptoShield → Cred", w - 270, 58);
    }

    // ── Helper colores ────────────────────────────────────────

    private Color typeColor(AttackType type) {
        return switch (type) {
            case YELLOW -> Color.web("#ffd232");
            case RED -> Color.web("#ff3c3c");
            case BLUE -> Color.web("#00aaff");
        };
    }
}