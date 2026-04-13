package ui;

import com.google.gson.JsonObject;
import client.ServerConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
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

    private static String getAvatarName(String id) {
        return switch (id) {
            case "character_1" -> "Devil";
            case "character_2" -> "Last Standing";
            case "character_3" -> "Red Angel";
            default -> "Devil";
        };
    }

    private final Stage stage;
    private final String username;
    private final ServerConnection connection;
    private final DatabaseManager db;

    private String selectedAvatar;
    private String selectedMap;

    private GameConfig pendingConfig;

    private StackPane rootPane;
    private Label statusLabel;
    private ImageView mapPreview;
    private Label mapNameLabel;

    public MenuScreen(Stage stage, String username,
                      ServerConnection connection, DatabaseManager db) {
        this.stage = stage;
        this.username = username;
        this.connection = connection;
        this.db = db;
        this.selectedAvatar = DEFAULT_AVATAR;
    }

    public void show() {
        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #04060e;");
        rootPane.getChildren().add(buildMainMenu());

        Scene scene = new Scene(rootPane, 1280, 720);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("Cyber Defense Duel");

        initServerHandlers();
        connection.selectAvatar(selectedAvatar);
    }

    // ── MAIN MENU ───────────────────────────────────────

    private HBox buildMainMenu() {

        Text title = new Text("CYBER DEFENSE DUEL");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
        title.setFill(Color.web("#00dcff"));

        Text welcome = new Text("OPERADOR: " + username.toUpperCase());
        welcome.setFill(Color.web("#445566"));

        Button playBtn = cyberButton("▶  JUGAR", "#00dcff");
        Button avatarBtn = cyberButton("◈  AVATAR", "#ffaa00");
        Button mapBtn = cyberButton("⬡  MAPA", "#8888ff");
        Button logoutBtn = cyberButton("✕  SALIR", "#ff4444");

        statusLabel = new Label("Selecciona avatar y mapa");
        statusLabel.setTextFill(Color.web("#445566"));

        mapPreview = new ImageView();
        mapPreview.setFitWidth(320);
        mapPreview.setFitHeight(180);

        mapNameLabel = new Label("Sin mapa");

        playBtn.setOnAction(e -> joinQueue());
        avatarBtn.setOnAction(e -> showAvatarScreen());
        mapBtn.setOnAction(e -> showMapScreen());
        logoutBtn.setOnAction(e -> goToLogin());

        VBox left = new VBox(15,
                title, welcome,
                playBtn, avatarBtn, mapBtn, logoutBtn,
                statusLabel);

        VBox right = new VBox(10,
                new Label("MAPA"), mapPreview, mapNameLabel);

        left.setAlignment(Pos.CENTER_LEFT);
        left.setPadding(new Insets(40));

        right.setAlignment(Pos.CENTER);

        return new HBox(left, right);
    }

    // ── AVATAR SCREEN ───────────────────────────────────

    private void showAvatarScreen() {

        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);

        for (String id : AVATAR_IDS) {
            ImageView img = new ImageView(loadAvatar(id));
            img.setFitWidth(100);
            img.setFitHeight(100);

            VBox card = new VBox(img, new Label(getAvatarName(id)));
            card.setAlignment(Pos.CENTER);

            card.setOnMouseClicked(e -> {
                selectedAvatar = id;
                connection.selectAvatar(id);
                rootPane.getChildren().setAll(buildMainMenu());
            });

            box.getChildren().add(card);
        }

        Button back = new Button("VOLVER");
        back.setOnAction(e -> rootPane.getChildren().setAll(buildMainMenu()));

        rootPane.getChildren().setAll(new VBox(box, back));
    }

    // ── MAP SCREEN ──────────────────────────────────────

    private void showMapScreen() {

        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);

        for (int i = 0; i < MAP_IDS.length; i++) {

            String id = MAP_IDS[i];
            String name = MAP_NAMES[i];

            ImageView img = new ImageView(loadMap(id));
            img.setFitWidth(200);
            img.setFitHeight(120);

            VBox card = new VBox(img, new Label(name));
            card.setAlignment(Pos.CENTER);

            card.setOnMouseClicked(e -> {
                selectedMap = name;
                connection.selectMap(name);
                rootPane.getChildren().setAll(buildMainMenu());
            });

            box.getChildren().add(card);
        }

        Button back = new Button("VOLVER");
        back.setOnAction(e -> rootPane.getChildren().setAll(buildMainMenu()));

        rootPane.getChildren().setAll(new VBox(box, back));
    }

    // ── WAITING ─────────────────────────────────────────

    private void showWaiting() {

        Label text = new Label("Buscando oponente...");
        text.setTextFill(Color.WHITE);

        rootPane.getChildren().setAll(new VBox(text));
    }

    private void showMatchFound() {

        Label text = new Label("¡Match encontrado!");
        text.setTextFill(Color.LIME);

        rootPane.getChildren().setAll(new VBox(text));
    }

    // ── NETWORK ─────────────────────────────────────────

    private void initServerHandlers() {
        connection.setOnMessage(msg ->
                Platform.runLater(() -> handleServer(msg)));

        connection.setOnError(err ->
                Platform.runLater(() -> statusLabel.setText(err)));
    }

    private void handleServer(JsonObject msg) {

        String type = msg.get("type").getAsString();

        switch (type) {

            case "WAITING" -> showWaiting();

            case "MATCH_FOUND" -> showMatchFound();

            case "CONFIG" -> {
                pendingConfig = parseConfig(msg);
                goToGame(pendingConfig);
            }

            case "ERROR" -> statusLabel.setText(msg.get("message").getAsString());
        }
    }

    private void joinQueue() {

        if (selectedMap == null) {
            statusLabel.setText("Selecciona mapa");
            return;
        }

        showWaiting();
        connection.joinQueue();
    }

    private void goToGame(GameConfig config) {

        GameView game = new GameView(
                config,
                connection,
                selectedAvatar,
                selectedMap,
                username,
                db,
                stage
        );

        Scene scene = new Scene(game, 1280, 720);
        stage.setScene(scene);
        stage.setFullScreen(true);

        game.requestFocus();
        game.startGame();
    }

    private void goToLogin() {
        new LoginScreen(stage, connection).show();
    }

    // ── UTILS ───────────────────────────────────────────

    private Image loadAvatar(String id) {
        return new Image(getClass().getResourceAsStream("/assets/characters/" + id + ".png"));
    }

    private Image loadMap(String id) {
        return new Image(getClass().getResourceAsStream("/assets/backgrounds/" + id + ".png"));
    }

    private Button cyberButton(String text, String color) {
        Button b = new Button(text);
        b.setTextFill(Color.WHITE);
        b.setStyle("-fx-border-color:" + color);
        return b;
    }

    private GameConfig parseConfig(JsonObject msg) {
        GameConfig config = new GameConfig();

        config.initialHp = msg.get("initialHp").getAsInt();
        config.baseSpawnRate = msg.get("baseSpawnRate").getAsDouble();
        config.spawnMultiplierPerLevel = msg.get("spawnMultiplierPerLevel").getAsDouble();

        return config;
    }
}