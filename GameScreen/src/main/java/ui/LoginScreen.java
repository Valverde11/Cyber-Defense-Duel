package ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import logic.GameConfig;

public class LoginScreen {

    private final Stage stage;
    private final GameConfig config;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public LoginScreen(Stage stage, GameConfig config) {
        this.stage = stage;
        this.config = config;
    }

    public void show() {
        // conectar al servidor
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Conectado al servidor");
        } catch (Exception e) {
            System.out.println("No se pudo conectar al servidor");
            e.printStackTrace();
        }


        // ── Ui ──────────────────────────────────────────
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Label status = new Label();

        styleField(userField, "Usuario");
        styleField(passField, "Contraseña");

        status.setTextFill(Color.web("#ff4444"));
        status.setFont(Font.font("Courier New", 13));

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
            
            new Thread(() -> {
                try {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "LOGIN");
                    json.addProperty("username", user);
                    json.addProperty("password", pass);

                    out.println(json.toString());

                    String response = in.readLine();
                    JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
                    
                    javafx.application.Platform.runLater(() -> {
                        if (resp.get("type").getAsString().equals("LOGIN_OK")) {
                            status.setTextFill(Color.web("#00c85a"));
                            status.setText("Login exitoso");
                            goToGame();
                        } else {
                            status.setTextFill(Color.web("#ff4444"));
                            status.setText("Login fallido");
                        }
                    });
                
                } catch (Exception ex) {    
                    ex.printStackTrace();
                }   
            }).start(); 
        });

        registerBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                status.setTextFill(Color.web("#ff4444"));
                status.setText("Completa todos los campos");
                return;
            }

            new Thread(() -> {
                try {
                    JsonObject json = new JsonObject();
                    json.addProperty("type", "REGISTER");
                    json.addProperty("username", user);
                    json.addProperty("password", pass);

                    out.println(json.toString());

                    String response = in.readLine();
                    JsonObject resp = JsonParser.parseString(response).getAsJsonObject();

                    javafx.application.Platform.runLater(() -> {
                        if (resp.get("type").getAsString().equals("REGISTER_OK")) {
                            status.setTextFill(Color.web("#00c85a"));
                            status.setText("Cuenta creada, ahora inicia sesión");
                        } else {
                            status.setTextFill(Color.web("#ff4444"));
                            status.setText("Ese usuario ya existe");
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
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

    private void goToGame() {
        GameView gameView = new GameView(config);
        Scene scene = new Scene(gameView, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        gameView.requestFocus();
        gameView.startGame();
    }
}