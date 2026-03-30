package ui;

import com.google.gson.JsonObject;

import client.ServerConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage stage;

    //si no funciona la IP revisar en cmd con ipconfig (IPv4)
    private final ServerConnection connection;

    public LoginScreen(Stage stage) {
        this.stage = stage;
        this.connection = new ServerConnection("192.168.100.10", 5000);
    }

    public LoginScreen(Stage stage, ServerConnection connection) {
        this.stage = stage;
        this.connection = connection;
    }



    private void goToMenu(String username) {
        MenuScreen menu = new MenuScreen(stage, username, connection);
        menu.show();
    }

    private void initConnection(Label status) {
        connection.setOnMessage(msg -> Platform.runLater(() -> handleServerMessage(msg, status)));
        connection.setOnError(err -> Platform.runLater(() -> {
            status.setTextFill(Color.web("#ff4444"));
            status.setText(err);
        }));

        try {
            connection.connect();
            status.setTextFill(Color.web("#00c85a"));
            status.setText("Conectado al servidor");
        } catch (Exception e) {
            status.setTextFill(Color.web("#ff4444"));
            status.setText("No se pudo conectar: " + e.getMessage());
        }
    }

    private void handleServerMessage(JsonObject msg, Label status) {
        String type = msg.get("type").getAsString();

        switch (type) {
            case "LOGIN_OK":
                String username = msg.get("username").getAsString();
                goToMenu(username);
                break;

            case "LOGIN_FAIL":
            case "REGISTER_FAIL":
            case "ERROR":
                status.setTextFill(Color.web("#ff4444"));
                status.setText(msg.get("message").getAsString());
                break;

            case "REGISTER_OK":
                status.setTextFill(Color.web("#00c85a"));
                status.setText(msg.get("message").getAsString());
                break;

            default:
                break;
        }
    }

    public void show() {

        // ── Campos ──────────────────────────────────────────
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Label status = new Label();

        styleField(userField, "Usuario");
        styleField(passField, "Contraseña");

        status.setTextFill(Color.web("#ff4444"));
        status.setFont(Font.font("Courier New", 13));

        initConnection(status);

        // ── Botones ──────────────────────────────────────────
        Button loginBtn = cyberButton("INICIAR SESIÓN", "#00dcff");
        Button registerBtn = cyberButton("REGISTRAR", "#00c85a");

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                status.setTextFill(Color.web("#ff4444"));
                status.setText("Completa todos los campos");
                return;
            }
            connection.login(user, pass);
        });

        registerBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                status.setTextFill(Color.web("#ff4444"));
                status.setText("Completa todos los campos");
                return;
            }
            connection.register(user, pass);
        });

        // ── Título ───────────────────────────────────────────
        Text title = new Text("CYBER DEFENSE DUEL");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 32));
        title.setFill(Color.web("#00dcff"));

        Text subtitle = new Text("ACCESO AL SISTEMA");
        subtitle.setFont(Font.font("Courier New", 13));
        subtitle.setFill(Color.web("#445566"));

        // ── Separador ────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #004488;");
        sep.setPrefWidth(340);

        // ── Tarjeta central ──────────────────────────────────
        HBox buttons = new HBox(10, loginBtn, registerBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12,
                title,
                subtitle,
                sep,
                fieldLabel("USUARIO"),
                userField,
                fieldLabel("CONTRASEÑA"),
                passField,
                buttons,
                status);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(44));
        card.setMaxWidth(440);
        card.setStyle(
                "-fx-background-color: #08091a;" +
                        "-fx-border-color: #003366;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;");

        // ── Fondo pantalla completa ───────────────────────────
        StackPane root = new StackPane(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #04060e;");

        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        stage.setFullScreen(true);
        stage.show();
    }

    // ── Helpers de estilo ─────────────────────────────────────

    private void styleField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setPrefWidth(340);
        field.setStyle(
                "-fx-background-color: #050a1c;" +
                        "-fx-text-fill: #c8dcff;" +
                        "-fx-prompt-text-fill: #334466;" +
                        "-fx-border-color: #004488;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 14;");
    }

    private Label fieldLabel(String text) {
        Label l = new Label("▸ " + text);
        l.setFont(Font.font("Courier New", 10));
        l.setTextFill(Color.web("#445566"));
        return l;
    }

    private Button cyberButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(160);
        btn.setPrefHeight(42);

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