package ui;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;

import client.ServerConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.GameConfig;
import persistence.DatabaseManager;

public class MenuScreen {

    private static final String DEFAULT_AVATAR = "character_1";
    private static final List<String> AVATAR_IDS = Arrays.asList(
            "character_1", "character_2", "character_3");
    private static final List<String> MAP_IDS = Arrays.asList(
            "data_center_dojo", "packet_bay_carnival");
    private static final List<String> MAP_NAMES = Arrays.asList(
            "Data Center Dojo", "Packet Bay Carnival");

    private static String getAvatarName(String avatarId) {
        return switch (avatarId) {
            case "character_1" -> "Devil";
            case "character_2" -> "Last Standing";
            case "character_3" -> "Red Angel";
            default -> "Devil";
        };
    }

    private final Stage stage;
    private final String username;
    private final ServerConnection connection;
    private final DatabaseManager db; // ← agregado
    private String selectedAvatar;
    private String selectedMap;
    private GameConfig pendingConfig;
    private boolean matchFoundReceived;

    private ImageView mapPreview;
    private Label mapNameLabel;

    // ── Constructores ────────────────────────────────────

    public MenuScreen(Stage stage, String username, ServerConnection connection,
            String initialAvatar, DatabaseManager db) {
        this.stage = stage;
        this.username = username;
        this.connection = connection;
        this.db = db;
        this.selectedAvatar = (initialAvatar == null || initialAvatar.isBlank())
                ? DEFAULT_AVATAR
                : initialAvatar;
    }

    public MenuScreen(Stage stage, String username,
            ServerConnection connection, DatabaseManager db) {
        this(stage, username, connection, DEFAULT_AVATAR, db);
    }

    public void show() {

        // ── Título ───────────────────────────────────────
        Text title = new Text("CYBER DEFENSE DUEL");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 36));
        title.setFill(Color.web("#00dcff"));

        Text welcome = new Text("OPERADOR: " + username.toUpperCase());
        welcome.setFont(Font.font("Courier New", 14));
        welcome.setFill(Color.web("#445566"));

        // ── Botones ──────────────────────────────────────
        Button playBtn = cyberButton("▶  JUGAR", "#00dcff");
        Button avatarBtn = cyberButton("◈  AVATAR", "#ffaa00");
        Button mapBtn = cyberButton("⬡  MAPA", "#8888ff");
        Button logoutBtn = cyberButton("✕  CERRAR SESIÓN", "#ff4444");

        Label avatarStatusLabel = infoLabel("Avatar: " + getAvatarName(selectedAvatar));
        Label statusLabel = infoLabel("Selecciona avatar y mapa para jugar");

        // ── Preview del mapa ─────────────────────────────
        mapPreview = new ImageView();
        mapPreview.setFitWidth(320);
        mapPreview.setFitHeight(180);
        mapPreview.setPreserveRatio(false);
        mapPreview.setStyle("-fx-border-color: #223344; -fx-border-width: 1;");

        mapNameLabel = new Label("Sin mapa seleccionado");
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

        // ── Acciones ─────────────────────────────────────
        initServerHandlers(statusLabel);
        connection.selectAvatar(selectedAvatar);

        playBtn.setOnAction(e -> joinQueue(statusLabel));
        avatarBtn.setOnAction(e -> chooseAvatar(statusLabel, avatarStatusLabel));
        mapBtn.setOnAction(e -> chooseMap(statusLabel));
        logoutBtn.setOnAction(e -> goToLogin());

        // ── Layout izquierdo ─────────────────────────────
        VBox leftPanel = new VBox(16,
                title, welcome,
                separator(),
                playBtn, avatarBtn, mapBtn, logoutBtn,
                separator(),
                avatarStatusLabel, statusLabel);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(50, 40, 50, 60));
        leftPanel.setPrefWidth(440);

        // ── Layout derecho ───────────────────────────────
        VBox rightPanel = new VBox(20);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(50, 60, 50, 40));

        Text previewTitle = new Text("VISTA PREVIA DEL MAPA");
        previewTitle.setFont(Font.font("Courier New", 12));
        previewTitle.setFill(Color.web("#445566"));

        rightPanel.getChildren().addAll(previewTitle, previewBox);

        // ── Layout principal ─────────────────────────────
        HBox root = new HBox(leftPanel, rightPanel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #04060e;");

        Separator divider = new Separator(javafx.geometry.Orientation.VERTICAL);
        divider.setStyle("-fx-background-color: #112233;");
        root.getChildren().add(1, divider);

        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel — Menú");
        stage.setFullScreen(true);
    }

    // ── Selección de mapa ────────────────────────────────

    private void chooseMap(Label statusLabel) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Selección de mapa");

        Label header = new Label("Elige el escenario");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Courier New", FontWeight.BOLD, 18));

        HBox mapsBox = new HBox(24);
        mapsBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < MAP_IDS.size(); i++) {
            String mapId = MAP_IDS.get(i);
            String mapName = MAP_NAMES.get(i);

            ImageView preview = new ImageView(loadMapImage(mapId));
            preview.setFitWidth(280);
            preview.setFitHeight(160);
            preview.setPreserveRatio(false);

            Label nameLabel = new Label(mapName);
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 13));

            VBox card = new VBox(10, preview, nameLabel);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(14));
            card.setStyle(mapCardStyle(mapName.equals(selectedMap)));
            card.setOnMouseClicked(e -> {
                selectedMap = mapName;
                connection.selectMap(mapName);
                statusLabel.setText("Mapa elegido: " + mapName);
                statusLabel.setTextFill(Color.web("#9fb3c8"));
                mapPreview.setImage(loadMapImage(mapId));
                mapNameLabel.setText(mapName);
                dialog.close();
            });
            card.setOnMouseEntered(e -> card.setStyle(mapCardStyle(true)));
            card.setOnMouseExited(e -> card.setStyle(mapCardStyle(mapName.equals(selectedMap))));
            mapsBox.getChildren().add(card);
        }

        Button closeBtn = cyberButton("CANCELAR", "#445566");
        closeBtn.setOnAction(e -> dialog.close());

        VBox dialogRoot = new VBox(20, header, mapsBox, closeBtn);
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setPadding(new Insets(30));
        dialogRoot.setStyle("-fx-background-color: #04060e;");

        dialog.setScene(new Scene(dialogRoot, 700, 360));
        dialog.showAndWait();
    }

    private String mapCardStyle(boolean selected) {
        String border = selected ? "#00dcff" : "#223344";
        String bg = selected ? "#0a1a2e" : "#08091a";
        int width = selected ? 2 : 1;
        return "-fx-background-color: " + bg + ";" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: " + width + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";
    }

    // ── Selección de avatar ──────────────────────────────

    private void chooseAvatar(Label statusLabel, Label avatarStatusLabel) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Selección de avatar");

        Label header = new Label("Elige tu operativo");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Courier New", FontWeight.BOLD, 18));

        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER);

        for (String avatarId : AVATAR_IDS) {
            ImageView img = new ImageView(loadAvatarImage(avatarId));
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(true);

            Label nameLabel = new Label(getAvatarName(avatarId));
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(Font.font("Courier New", 12));

            VBox card = new VBox(8, img, nameLabel);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(12));
            card.setStyle(tileStyle(avatarId.equals(selectedAvatar)));
            card.setUserData(avatarId);
            card.setOnMouseClicked(e -> {
                selectedAvatar = avatarId;
                connection.selectAvatar(selectedAvatar);
                avatarStatusLabel.setText("Avatar: " + getAvatarName(selectedAvatar));
                statusLabel.setText("Avatar elegido: " + getAvatarName(selectedAvatar));
                statusLabel.setTextFill(Color.web("#9fb3c8"));
                refreshAvatarSelection(avatarBox);
                dialog.close();
            });
            avatarBox.getChildren().add(card);
        }

        Button closeBtn = cyberButton("CERRAR", "#445566");
        closeBtn.setOnAction(e -> dialog.close());

        VBox dialogRoot = new VBox(20, header, avatarBox, closeBtn);
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setPadding(new Insets(30));
        dialogRoot.setStyle("-fx-background-color: #04060e;");

        dialog.setScene(new Scene(dialogRoot, 560, 300));
        dialog.showAndWait();
    }

    private void refreshAvatarSelection(HBox avatarBox) {
        for (javafx.scene.Node node : avatarBox.getChildren()) {
            if (node instanceof VBox card) {
                String id = (String) card.getUserData();
                card.setStyle(tileStyle(id.equals(selectedAvatar)));
            }
        }
    }

    private String tileStyle(boolean selected) {
        return selected
                ? "-fx-background-color: #0a1a2e; -fx-border-color: #00dcff; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;"
                : "-fx-background-color: #08091a; -fx-border-color: #223344; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;";
    }

    // ── Red ──────────────────────────────────────────────

    private void initServerHandlers(Label statusLabel) {
        connection.setOnMessage(msg -> Platform.runLater(() -> handleServerMessage(msg, statusLabel)));
        connection.setOnError(err -> Platform.runLater(() -> {
            statusLabel.setTextFill(Color.web("#ff4444"));
            statusLabel.setText(err);
        }));
    }

    private void handleServerMessage(JsonObject msg, Label statusLabel) {
        String type = msg.get("type").getAsString();
        switch (type) {
            case "AVATAR_OK", "MAP_OK" -> {
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText(msg.get("message").getAsString());
            }
            case "WAITING" -> {
                statusLabel.setTextFill(Color.web("#ffd166"));
                statusLabel.setText(msg.get("message").getAsString());
            }
            case "MATCH_FOUND" -> {
                matchFoundReceived = true;
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText("Partida encontrada. Preparando inicio...");
                showMatchFoundAlert();
                tryStartGame();
            }
            case "CONFIG" -> {
                pendingConfig = parseConfig(msg);
                tryStartGame();
            }
            case "ERROR" -> {
                statusLabel.setTextFill(Color.web("#ff4444"));
                statusLabel.setText(msg.get("message").getAsString());
            }
        }
    }

    private void joinQueue(Label statusLabel) {
        if (selectedAvatar == null || selectedAvatar.isBlank()) {
            statusLabel.setTextFill(Color.web("#ff4444"));
            statusLabel.setText("Debes seleccionar avatar antes de jugar");
            return;
        }
        if (selectedMap == null || selectedMap.isBlank()) {
            statusLabel.setTextFill(Color.web("#ff4444"));
            statusLabel.setText("Debes seleccionar mapa antes de jugar");
            return;
        }
        connection.joinQueue();
        statusLabel.setTextFill(Color.web("#ffd166"));
        statusLabel.setText("Buscando oponente...");
    }

    private void tryStartGame() {
        if (!matchFoundReceived || pendingConfig == null)
            return;
        goToGame(pendingConfig);
    }

    private void goToGame(GameConfig config) {
        GameView gameView = new GameView(config, connection,
                selectedAvatar, selectedMap, username, db, stage);
        Scene scene = new Scene(gameView, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        stage.setFullScreen(true);
        gameView.requestFocus();
        gameView.startGame();
    }

    private void goToLogin() {
        new LoginScreen(stage, connection).show();
    }

    private void showMatchFoundAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Emparejamiento");
        alert.setHeaderText("¡Oponente encontrado!");
        alert.setContentText("La partida está por comenzar.");
        alert.showAndWait();
    }

    private GameConfig parseConfig(JsonObject msg) {
        GameConfig config = new GameConfig();
        config.initialHp = msg.get("initialHp").getAsInt();
        config.baseSpawnRate = msg.get("baseSpawnRate").getAsDouble();
        config.baseAttackSpeed = msg.get("baseAttackSpeed").getAsDouble();
        config.scorePerKill = msg.get("scorePerKill").getAsInt();
        config.difficultyStepScore = msg.get("difficultyStepScore").getAsInt();
        config.spawnMultiplierPerLevel = msg.get("spawnMultiplierPerLevel").getAsDouble();
        config.speedAddPerLevel = msg.get("speedAddPerLevel").getAsDouble();
        JsonObject damage = msg.getAsJsonObject("damageByType");
        config.damageYellow = damage.get("DDOS").getAsInt();
        config.damageRed = damage.get("MALWARE").getAsInt();
        config.damageBlue = damage.get("CRED").getAsInt();
        return config;
    }

    // ── Carga de imágenes ────────────────────────────────

    private Image loadAvatarImage(String avatarId) {
        try {
            return new Image(getClass().getResourceAsStream(
                    "/assets/characters/" + avatarId + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    private Image loadMapImage(String mapId) {
        try {
            return new Image(getClass().getResourceAsStream(
                    "/assets/backgrounds/" + mapId + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helpers de estilo ────────────────────────────────

    private Button cyberButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(220);
        btn.setPrefHeight(44);
        String base = "-fx-background-color: transparent; -fx-border-color: " + color +
                "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + color + "33; -fx-border-color: " + color +
                "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
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