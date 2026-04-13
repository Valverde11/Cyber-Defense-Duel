package ui;

import com.google.gson.JsonObject;
import client.ServerConnection;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import logic.GameConfig;
import persistence.DatabaseManager;

public class MenuScreen {

    private static final String DEFAULT_AVATAR = "character_1";
    private static final String[] AVATAR_IDS = {
        "character_1", "character_2", "character_3"
    };
    private static final String[] MAP_IDS = {
        "data_center_dojo", "packet_bay_carnival"
    };
    private static final String[] MAP_NAMES = {
        "Data Center Dojo", "Packet Bay Carnival"
    };

    private static String getAvatarName(String avatarId) {
        return switch (avatarId) {
            case "character_1" -> "Devil";
            case "character_2" -> "Last Standing";
            case "character_3" -> "Red Angel";
            default -> "Devil";
        };
    }

    private final Stage            stage;
    private final String           username;
    private final ServerConnection connection;
    private final DatabaseManager  db;
    private String                 selectedAvatar;
    private String                 selectedMap;
    private GameConfig             pendingConfig;
    private boolean                matchFoundReceived;

    private ImageView mapPreview;
    private Label     mapNameLabel;
    private Label     avatarStatusLabel;
    private Label     statusLabel;
    private StackPane rootPane;

    public MenuScreen(Stage stage, String username, ServerConnection connection,
                      String initialAvatar, DatabaseManager db) {
        this.stage          = stage;
        this.username       = username;
        this.connection     = connection;
        this.db             = db;
        this.selectedAvatar = (initialAvatar == null || initialAvatar.isBlank())
                ? DEFAULT_AVATAR : initialAvatar;
    }

    public MenuScreen(Stage stage, String username,
                      ServerConnection connection, DatabaseManager db) {
        this(stage, username, connection, DEFAULT_AVATAR, db);
    }

    public void show() {
        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().add(buildMenuPanel());

        Scene scene = new Scene(rootPane, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel — Menú");
        stage.setFullScreen(true);

        initServerHandlers(); // Registra callbacks de servidor
        connection.selectAvatar(selectedAvatar); // Envia avatar al servidor
    }

    // ── Panel principal ───────────────────────────────────

    private HBox buildMenuPanel() {
        Text title = new Text("CYBER DEFENSE DUEL");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
        title.setFill(Color.web("#00dcff"));

        Text welcome = new Text("OPERADOR: " + username.toUpperCase());
        welcome.setFont(Font.font("Courier New", 14));
        welcome.setFill(Color.web("#445566"));

        Button playBtn   = cyberButton("▶  JUGAR",         "#00dcff");
        Button avatarBtn = cyberButton("◈  AVATAR",        "#ffaa00");
        Button mapBtn    = cyberButton("⬡  MAPA",          "#8888ff");
        Button logoutBtn = cyberButton("✕  CERRAR SESIÓN", "#ff4444");

        avatarStatusLabel = infoLabel("Avatar: " + getAvatarName(selectedAvatar));
        statusLabel       = infoLabel("Selecciona avatar y mapa para jugar");

        mapPreview = new ImageView();
        mapPreview.setFitWidth(320);
        mapPreview.setFitHeight(180);
        mapPreview.setPreserveRatio(false);
        mapPreview.setStyle("-fx-border-color: #223344; -fx-border-width: 1;");

        if (selectedMap != null)
            mapPreview.setImage(loadMapImage(getMapIdForName(selectedMap)));

        mapNameLabel = new Label(selectedMap != null ? selectedMap : "Sin mapa seleccionado");
        mapNameLabel.setFont(Font.font("Courier New", 11));
        mapNameLabel.setTextFill(Color.web("#445566"));

        VBox previewBox = new VBox(8, mapPreview, mapNameLabel);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.setPadding(new Insets(10));
        previewBox.setStyle(
                "-fx-background-color: #08091a;" +
                "-fx-border-color: #223344;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;");

        playBtn.setOnAction(e   -> joinQueue());
        avatarBtn.setOnAction(e -> showAvatarPanel());
        mapBtn.setOnAction(e    -> showMapPanel());
        logoutBtn.setOnAction(e -> goToLogin());

        VBox leftPanel = new VBox(16,
                title, welcome, separator(),
                playBtn, avatarBtn, mapBtn, logoutBtn,
                separator(), avatarStatusLabel, statusLabel);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(50, 40, 50, 60));
        leftPanel.setPrefWidth(440);

        Text previewTitle = new Text("VISTA PREVIA DEL MAPA");
        previewTitle.setFont(Font.font("Courier New", 12));
        previewTitle.setFill(Color.web("#445566"));

        VBox rightPanel = new VBox(20, previewTitle, previewBox);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(50, 60, 50, 40));

        Separator divider = new Separator(javafx.geometry.Orientation.VERTICAL);
        divider.setStyle("-fx-background-color: #112233;");

        HBox root = new HBox(leftPanel, divider, rightPanel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #04060e;");
        return root;
    }

    // ── Panel de avatar ───────────────────────────────────

    private void showAvatarPanel() {
        Text header = new Text("ELIGE TU OPERATIVO");
        header.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        header.setFill(Color.web("#00dcff"));

        HBox avatarBox = new HBox(24);
        avatarBox.setAlignment(Pos.CENTER);

        for (String avatarId : AVATAR_IDS) {
            ImageView img = new ImageView(loadAvatarImage(avatarId));
            img.setFitWidth(120);
            img.setFitHeight(120);
            img.setPreserveRatio(true);

            Label nameLabel = new Label(getAvatarName(avatarId));
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 13));

            VBox card = new VBox(10, img, nameLabel);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(16));
            card.setStyle(tileStyle(avatarId.equals(selectedAvatar)));
            card.setUserData(avatarId);

            card.setOnMouseClicked(e -> {
                selectedAvatar = (String) card.getUserData();
                connection.selectAvatar(selectedAvatar);
                for (javafx.scene.Node n : avatarBox.getChildren()) {
                    if (n instanceof VBox c)
                        c.setStyle(tileStyle(c.getUserData().equals(selectedAvatar)));
                }
                rootPane.getChildren().setAll(buildMenuPanel());
            });
            card.setOnMouseEntered(e -> {
                if (!card.getUserData().equals(selectedAvatar))
                    card.setStyle(tileStyle(true));
            });
            card.setOnMouseExited(e ->
                card.setStyle(tileStyle(card.getUserData().equals(selectedAvatar))));

            avatarBox.getChildren().add(card);
        }

        Button backBtn = cyberButton("← VOLVER", "#445566");
        backBtn.setOnAction(e -> rootPane.getChildren().setAll(buildMenuPanel()));

        VBox panel = new VBox(30, header, avatarBox, backBtn);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().setAll(panel);
    }

    // ── Panel de mapa ─────────────────────────────────────

    private void showMapPanel() {
        Text header = new Text("ELIGE EL ESCENARIO");
        header.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        header.setFill(Color.web("#00dcff"));

        HBox mapsBox = new HBox(30);
        mapsBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < MAP_IDS.length; i++) {
            String mapId   = MAP_IDS[i];
            String mapName = MAP_NAMES[i];
            final String fId   = mapId;
            final String fName = mapName;

            Image img = loadMapImage(mapId);
            ImageView preview = new ImageView(img);
            preview.setFitWidth(320);
            preview.setFitHeight(180);
            preview.setPreserveRatio(false);

            Label nameLabel = new Label(mapName);
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

            VBox card = new VBox(12, preview, nameLabel);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(16));
            card.setStyle(mapCardStyle(mapName.equals(selectedMap)));
            card.setUserData(mapId);

            card.setOnMouseClicked(e -> {
                selectedMap = fName;
                connection.selectMap(fName);
                rootPane.getChildren().setAll(buildMenuPanel());
            });
            card.setOnMouseEntered(e -> card.setStyle(mapCardStyle(true)));
            card.setOnMouseExited(e  -> card.setStyle(mapCardStyle(fName.equals(selectedMap))));
            mapsBox.getChildren().add(card);
        }

        Button backBtn = cyberButton("← VOLVER", "#445566");
        backBtn.setOnAction(e -> rootPane.getChildren().setAll(buildMenuPanel()));

        VBox panel = new VBox(30, header, mapsBox, backBtn);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().setAll(panel);
    }

    // ── Pantalla de espera ────────────────────────────────

    private void showWaitingScreen() {
        Text title = new Text("BUSCANDO OPONENTE");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 32));
        title.setFill(Color.web("#00dcff"));

        Label dots = new Label("●  ○  ○");
        dots.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        dots.setTextFill(Color.web("#00dcff"));

        String[] frames = {"●  ○  ○", "●  ●  ○", "●  ●  ●", "○  ●  ●", "○  ○  ●", "○  ○  ○"};
        Timeline anim = new Timeline();
        for (int i = 0; i < frames.length; i++) {
            final String frame = frames[i];
            anim.getKeyFrames().add(
                new KeyFrame(Duration.millis(300 * i), e -> dots.setText(frame)));
        }
        anim.setCycleCount(Timeline.INDEFINITE);
        anim.play();

        Text sub = new Text("OPERADOR: " + username.toUpperCase());
        sub.setFont(Font.font("Courier New", 13));
        sub.setFill(Color.web("#445566"));

        Text mapInfo = new Text("ESCENARIO: " + (selectedMap != null
                ? selectedMap.toUpperCase() : "—"));
        mapInfo.setFont(Font.font("Courier New", 13));
        mapInfo.setFill(Color.web("#445566"));

        ImageView mapImg = new ImageView();
        if (selectedMap != null)
            mapImg.setImage(loadMapImage(getMapIdForName(selectedMap)));
        mapImg.setFitWidth(400);
        mapImg.setFitHeight(225);
        mapImg.setPreserveRatio(false);

        Button cancelBtn = cyberButton("✕  CANCELAR", "#ff4444");
        cancelBtn.setOnAction(e -> {
            anim.stop();
            rootPane.getChildren().setAll(buildMenuPanel());
        });

        VBox panel = new VBox(24, title, dots, mapImg, sub, mapInfo, cancelBtn);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().setAll(panel);
    }

    // ── Pantalla de partida encontrada ────────────────────

    private void showMatchFoundScreen() {
        Text title = new Text("¡OPONENTE ENCONTRADO!");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
        title.setFill(Color.web("#00c85a"));

        Text sub = new Text("La partida está por comenzar...");
        sub.setFont(Font.font("Courier New", 16));
        sub.setFill(Color.web("#9fb3c8"));

        Label countdown = new Label("3");
        countdown.setFont(Font.font("Courier New", FontWeight.BOLD, 80));
        countdown.setTextFill(Color.web("#00dcff"));

        Timeline ct = new Timeline(
            new KeyFrame(Duration.seconds(0), e -> countdown.setText("3")),
            new KeyFrame(Duration.seconds(1), e -> countdown.setText("2")),
            new KeyFrame(Duration.seconds(2), e -> countdown.setText("1"))
        );
        ct.setCycleCount(1);
        ct.play();

        VBox panel = new VBox(24, title, sub, countdown);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().setAll(panel);
    }

    // ── Red ───────────────────────────────────────────────

    private void initServerHandlers() {
        connection.setOnMessage(msg -> Platform.runLater(() ->
            handleServerMessage(msg)));
        connection.setOnError(err -> Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.web("#ff4444"));
                statusLabel.setText(err);
            }
        }));
    }

    private void handleServerMessage(JsonObject msg) {
        String type = msg.get("type").getAsString();
        switch (type) {
            case "AVATAR_OK", "MAP_OK" -> {
                if (statusLabel != null) {
                    statusLabel.setTextFill(Color.web("#00c85a"));
                    statusLabel.setText(msg.get("message").getAsString());
                }
            }
            case "WAITING" -> {
                if (statusLabel != null) {
                    statusLabel.setTextFill(Color.web("#ffd166"));
                    statusLabel.setText(msg.get("message").getAsString());
                }
            }
            case "MATCH_FOUND" -> {
                matchFoundReceived = true;
                showMatchFoundScreen();
            }
            case "CONFIG" -> {
                // El servidor ya esperó 3.5s — arrancar directo
                pendingConfig = parseConfig(msg);
                goToGame(pendingConfig);
            }
            case "ERROR" -> {
                if (statusLabel != null) {
                    statusLabel.setTextFill(Color.web("#ff4444"));
                    statusLabel.setText(msg.get("message").getAsString());
                }
            }
        }
    }

    private void joinQueue() {
        if (selectedAvatar == null || selectedAvatar.isBlank()) {
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.web("#ff4444"));
                statusLabel.setText("Debes seleccionar avatar antes de jugar");
            }
            return;
        }
        if (selectedMap == null || selectedMap.isBlank()) {
            if (statusLabel != null) {
                statusLabel.setTextFill(Color.web("#ff4444"));
                statusLabel.setText("Debes seleccionar mapa antes de jugar");
            }
            return;
        }
        showWaitingScreen();
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(e -> connection.joinQueue());
        pause.play();
    }

    private void goToGame(GameConfig config) {
        GameView gameView = new GameView(config, connection,
                selectedAvatar, selectedMap, username, db);
        Scene scene = new Scene(gameView,
                stage.getScene().getWidth(),
                stage.getScene().getHeight());
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        stage.setFullScreen(true);
        gameView.requestFocus();
        gameView.startGame();
    }

    private void goToLogin() {
        new LoginScreen(stage, connection).show();
    }

    private GameConfig parseConfig(JsonObject msg) {
        GameConfig config = new GameConfig();
        config.initialHp               = msg.get("initialHp").getAsInt();
        config.baseSpawnRate           = msg.get("baseSpawnRate").getAsDouble();
        config.baseAttackSpeed         = msg.get("baseAttackSpeed").getAsDouble();
        config.scorePerKill            = msg.get("scorePerKill").getAsInt();
        config.difficultyStepScore     = msg.get("difficultyStepScore").getAsInt();
        config.spawnMultiplierPerLevel = msg.get("spawnMultiplierPerLevel").getAsDouble();
        config.speedAddPerLevel        = msg.get("speedAddPerLevel").getAsDouble();
        JsonObject damage = msg.getAsJsonObject("damageByType");
        config.damageYellow = damage.get("DDOS").getAsInt();
        config.damageRed    = damage.get("MALWARE").getAsInt();
        config.damageBlue   = damage.get("CRED").getAsInt();
        return config;
    }

    private String getMapIdForName(String mapName) {
        for (int i = 0; i < MAP_NAMES.length; i++)
            if (MAP_NAMES[i].equals(mapName)) return MAP_IDS[i];
        return MAP_IDS[0];
    }

    private Image loadAvatarImage(String avatarId) {
        try {
            var stream = getClass().getResourceAsStream(
                    "/assets/characters/" + avatarId + ".png");
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) { return null; }
    }

    private Image loadMapImage(String mapId) {
        try {
            var stream = getClass().getResourceAsStream(
                    "/assets/backgrounds/" + mapId + ".png");
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) { return null; }
    }

    private String mapCardStyle(boolean selected) {
        String border = selected ? "#00dcff" : "#223344";
        String bg     = selected ? "#0a1a2e" : "#08091a";
        int    width  = selected ? 2 : 1;
        return "-fx-background-color: " + bg + ";" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: " + width + ";" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;";
    }

    private String tileStyle(boolean selected) {
        return selected
            ? "-fx-background-color: #0a1a2e; -fx-border-color: #00dcff; " +
              "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; " +
              "-fx-padding: 10; -fx-cursor: hand;"
            : "-fx-background-color: #08091a; -fx-border-color: #223344; " +
              "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; " +
              "-fx-padding: 10; -fx-cursor: hand;";
    }

    private Button cyberButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(220);
        btn.setPrefHeight(44);
        String base  = "-fx-background-color: transparent; -fx-border-color: " + color +
                       "; -fx-border-width: 1; -fx-border-radius: 4; " +
                       "-fx-background-radius: 4; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + color + "33; -fx-border-color: " + color +
                       "; -fx-border-width: 1; -fx-border-radius: 4; " +
                       "-fx-background-radius: 4; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Courier New", 12));
        l.setTextFill(Color.web("#445566"));
        return l;
    }

    private javafx.scene.control.Separator separator() {
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: #112233;");
        sep.setPrefWidth(300);
        return sep;
    }
}