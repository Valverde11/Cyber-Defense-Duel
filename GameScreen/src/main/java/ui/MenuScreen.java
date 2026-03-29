package ui;

import client.ServerConnection;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
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

import java.util.Arrays;
import java.util.List;

public class MenuScreen {

    private static final String DEFAULT_AVATAR = "character_1";
    private static final java.util.List<String> AVATAR_IDS = Arrays.asList(
            "character_1",
            "character_2",
            "character_3"
    );

    private static String getAvatarName(String avatarId) {
        return switch (avatarId) {
            case "character_1" -> "Devil";
            case "character_2" -> "Last Standing";
            case "character_3" -> "Red Angel";
            default -> "Devil";
        };
    }

    private Stage stage;
    private String username;
    private ServerConnection connection;
    private String selectedAvatar;
    private String selectedMap;
    private GameConfig pendingConfig;
    private boolean matchFoundReceived;

    public MenuScreen(Stage stage, String username, ServerConnection connection, String initialAvatar) {
        this.stage = stage;
        this.username = username;
        this.connection = connection;
        this.selectedAvatar = (initialAvatar == null || initialAvatar.isBlank()) ? DEFAULT_AVATAR : initialAvatar;
    }

    public MenuScreen(Stage stage, String username, ServerConnection connection) {
        this(stage, username, connection, DEFAULT_AVATAR);
    }

    public void show() {

        // ── TEXTO BIENVENIDA ─────────────────────────────
        Text welcome = new Text("Bienvenido " + username);
        welcome.setFill(Color.WHITE);
        welcome.setFont(Font.font("Courier New", FontWeight.BOLD, 28));

        // ── BOTONES ──────────────────────────────────────
        Button playButton = cyberButton("JUGAR", "#00dcff");
        Button avatarButton = cyberButton("AVATAR", "#ffaa00");
        Button mapButton = cyberButton("MAPA", "#8888ff");
        Button logoutButton = cyberButton("CERRAR SESIÓN", "#ff4444");
        Label avatarStatusLabel = new Label("Avatar actual: " + getAvatarName(selectedAvatar));
        avatarStatusLabel.setTextFill(Color.web("#9fb3c8"));
        avatarStatusLabel.setFont(Font.font("Courier New", 12));
        Label statusLabel = new Label("Selecciona avatar y mapa para entrar a cola");
        statusLabel.setTextFill(Color.web("#9fb3c8"));
        statusLabel.setFont(Font.font("Courier New", 12));

        initServerHandlers(statusLabel);
        connection.selectAvatar(selectedAvatar);

        // ── ACCIONES DE BOTONES ──────────────────────────
        playButton.setOnAction(e -> joinQueue(statusLabel));

        avatarButton.setOnAction(e -> chooseAvatar(statusLabel, avatarStatusLabel));

        mapButton.setOnAction(e -> chooseMap(statusLabel));

        logoutButton.setOnAction(e -> goToLogin());

        // ── LAYOUT ───────────────────────────────────────
        VBox root = new VBox(20,
                welcome,
                playButton,
                avatarButton,
                mapButton,
                logoutButton,
                avatarStatusLabel,
                statusLabel
        );

        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #04060e;");

        Scene scene = new Scene(root, 1280, 720);

        stage.setScene(scene);
        stage.setTitle("Menú Principal");
    }

    // ── FUNCIONES ───────────────────────────────────────

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
            case "AVATAR_OK":
            case "MAP_OK":
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText(msg.get("message").getAsString());
                break;
            case "WAITING":
                statusLabel.setTextFill(Color.web("#ffd166"));
                statusLabel.setText(msg.get("message").getAsString());
                break;
            case "MATCH_FOUND":
                matchFoundReceived = true;
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText("Partida encontrada. Preparando inicio...");
                showMatchFoundAlert();
                tryStartGame();
                break;
            case "CONFIG":
                pendingConfig = parseConfig(msg);
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText("Configuracion del servidor recibida");
                tryStartGame();
                break;
            case "ERROR":
                statusLabel.setTextFill(Color.web("#ff4444"));
                statusLabel.setText(msg.get("message").getAsString());
                break;
            default:
                break;
        }
    }

    private void showMatchFoundAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Emparejamiento");
        alert.setHeaderText("Oponente encontrado");
        alert.setContentText("Ya estan los 2 jugadores en cola.");
        alert.showAndWait();
    }

    private void chooseAvatar(Label statusLabel, Label avatarStatusLabel) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Seleccion de avatar");

        Label header = new Label("Elige tu avatar");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Courier New", FontWeight.BOLD, 16));

        HBox avatarBox = new HBox(20);
        avatarBox.setAlignment(Pos.CENTER);

        for (String avatarId : AVATAR_IDS) {
            ImageView avatarPreview = new ImageView(loadAvatarImage(avatarId));
            avatarPreview.setFitWidth(100);
            avatarPreview.setFitHeight(100);
            avatarPreview.setPreserveRatio(true);

            Label avatarLabel = new Label(getAvatarName(avatarId));
            avatarLabel.setTextFill(Color.WHITE);
            avatarLabel.setFont(Font.font("Courier New", 12));

            VBox card = new VBox(8, avatarPreview, avatarLabel);
            card.setAlignment(Pos.CENTER);
            card.setStyle(tileStyle(avatarId.equals(selectedAvatar)));
            card.setUserData(avatarId);
            card.setOnMouseClicked(e -> {
                selectedAvatar = avatarId;
                connection.selectAvatar(selectedAvatar);
                statusLabel.setTextFill(Color.web("#9fb3c8"));
                statusLabel.setText("Avatar elegido: " + getAvatarName(selectedAvatar));
                avatarStatusLabel.setText("Avatar actual: " + getAvatarName(selectedAvatar));
                refreshSelection(avatarBox);
                dialog.close();
            });

            avatarBox.getChildren().add(card);
        }

        refreshSelection(avatarBox);

        Button closeButton = cyberButton("CERRAR", "#4455ff");
        closeButton.setOnAction(e -> dialog.close());

        VBox dialogRoot = new VBox(20, header, avatarBox, closeButton);
        dialogRoot.setAlignment(Pos.CENTER);
        dialogRoot.setStyle("-fx-background-color: #04060e; -fx-padding: 20;");

        Scene dialogScene = new Scene(dialogRoot, 520, 260);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void refreshSelection(HBox avatarBox) {
        for (javafx.scene.Node node : avatarBox.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                String avatarId = (String) card.getUserData();
                card.setStyle(tileStyle(avatarId.equals(selectedAvatar)));
            }
        }
    }

    private String tileStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #111822; -fx-border-color: #00dcff; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
        }
        return "-fx-background-color: #111822; -fx-border-color: #445566; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
    }

    private Image loadAvatarImage(String avatarId) {
        try {
            return new Image(getClass().getResourceAsStream("/assets/characters/" + avatarId + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    private void chooseMap(Label statusLabel) {
        List<String> maps = Arrays.asList("Data Center Dojo", "Packet Bay Carnival");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(maps.get(0), maps);
        dialog.setTitle("Seleccion de mapa");
        dialog.setHeaderText("Elige el mapa");
        dialog.setContentText("Mapa:");

        dialog.showAndWait().ifPresent(map -> {
            selectedMap = map;
            connection.selectMap(map);
            statusLabel.setTextFill(Color.web("#9fb3c8"));
            statusLabel.setText("Mapa elegido: " + map);
        });
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
        statusLabel.setText("Entrando a cola...");
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

    private void tryStartGame() {
        if (!matchFoundReceived || pendingConfig == null) {
            return;
        }
        goToGame(pendingConfig);
    }

    private void goToGame(GameConfig config) {
        GameView gameView = new GameView(config, connection, selectedAvatar);
        Scene scene = new Scene(gameView, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        gameView.requestFocus();
        gameView.startGame();
    }

    private void goToLogin() {
        LoginScreen login = new LoginScreen(stage, connection);
        login.show();
    }

    // ── BOTÓN ESTILO CYBER ──────────────────────────────

    private Button cyberButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);

        String base = "-fx-background-color: transparent;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 4;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;";

        String hover = "-fx-background-color: " + color + "33;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 4;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;";

        btn.setStyle(base);

        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));

        return btn;
    }
}