package ui;

import client.ServerConnection;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MenuScreen {

    private Stage stage;
    private String username;
    private ServerConnection connection;

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
        Button settingsButton = cyberButton("CONFIGURACIONES", "#8888ff");
        Button logoutButton = cyberButton("CERRAR SESIÓN", "#ff4444");

        // ── ACCIONES DE BOTONES ──────────────────────────
        playButton.setOnAction(e -> goToGame());

        avatarButton.setOnAction(e -> goToAvatar());

        settingsButton.setOnAction(e -> goToSettings());

        logoutButton.setOnAction(e -> goToLogin());

        // ── LAYOUT ───────────────────────────────────────
        VBox root = new VBox(20,
                welcome,
                playButton,
                avatarButton,
                settingsButton,
                logoutButton
        );

        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #04060e;");

        Scene scene = new Scene(root, 1280, 720);

        stage.setScene(scene);
        stage.setTitle("Menú Principal");
    }

    // ── FUNCIONES ───────────────────────────────────────

    private void goToGame() {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView, 1280, 720);

        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");

        gameView.requestFocus(); // IMPORTANTE
        gameView.startGame();    // inicia el juego
    }

    private void goToAvatar() {
        System.out.println("Pantalla de avatar (por hacer)");
    }

    private void goToSettings() {
        System.out.println("Configuraciones (por hacer)");
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