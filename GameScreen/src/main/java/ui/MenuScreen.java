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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class MenuScreen {

    private Stage stage;
    private String username;
    private ServerConnection connection;
    private String selectedAvatar;
    private String selectedMap;

    public MenuScreen(Stage stage, String username, ServerConnection connection) {
        this.stage = stage;
        this.username = username;
        this.connection = connection;
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
        Label statusLabel = new Label("Selecciona avatar y mapa para entrar a cola");
        statusLabel.setTextFill(Color.web("#9fb3c8"));
        statusLabel.setFont(Font.font("Courier New", 12));

        initServerHandlers(statusLabel);

        // ── ACCIONES DE BOTONES ──────────────────────────
        playButton.setOnAction(e -> joinQueue(statusLabel));

        avatarButton.setOnAction(e -> chooseAvatar(statusLabel));

        mapButton.setOnAction(e -> chooseMap(statusLabel));

        logoutButton.setOnAction(e -> goToLogin());

        // ── LAYOUT ───────────────────────────────────────
        VBox root = new VBox(20,
                welcome,
                playButton,
                avatarButton,
                mapButton,
                logoutButton,
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
                statusLabel.setTextFill(Color.web("#00c85a"));
                statusLabel.setText("Partida encontrada. Preparando inicio...");
                showMatchFoundAlert();
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

    private void chooseAvatar(Label statusLabel) {
        List<String> avatars = Arrays.asList(
                "Captain Firewall",
                "Byte Ninja",
                "Malware Muncher",
                "Crypto Llama",
                "Packet Pirate",
                "Null Pointer Paladin"
        );

        ChoiceDialog<String> dialog = new ChoiceDialog<>(avatars.get(0), avatars);
        dialog.setTitle("Seleccion de avatar");
        dialog.setHeaderText("Elige tu avatar");
        dialog.setContentText("Avatar:");

        dialog.showAndWait().ifPresent(avatar -> {
            selectedAvatar = avatar;
            connection.selectAvatar(avatar);
            statusLabel.setTextFill(Color.web("#9fb3c8"));
            statusLabel.setText("Avatar elegido: " + avatar);
        });
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

    private void goToGame() {
        // El inicio de partida real se activara cuando llegue CONFIG desde el servidor.
        System.out.println("Esperando CONFIG para iniciar GameView");
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