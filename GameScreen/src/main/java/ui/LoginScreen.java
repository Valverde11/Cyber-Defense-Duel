package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import persistence.DatabaseManager;
import model.UserRecord;

public class LoginScreen {

    private final DatabaseManager db = new DatabaseManager();
    private final Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Campos
        TextField userField = new TextField();
        PasswordField passField = new PasswordField();
        Label status = new Label();

        userField.setPromptText("Usuario");
        passField.setPromptText("Contraseña");

        // Botones
        Button loginBtn = new Button("Iniciar sesión");
        Button registerBtn = new Button("Registrar");

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                status.setText("Completa todos los campos");
                return;
            }
            UserRecord record = db.login(user, pass);
            if (record != null) {
                goToGame(); // TODO: pasar a AvatarScreen primero
            } else {
                status.setText("Usuario o contraseña incorrectos");
            }
        });

        registerBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                status.setText("Completa todos los campos");
                return;
            }
            if (db.register(user, pass)) {
                status.setText("Cuenta creada, ahora inicia sesión");
            } else {
                status.setText("Ese usuario ya existe");
            }
        });

        // Layout
        HBox buttons = new HBox(10, loginBtn, registerBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(14,
                new Label("Usuario:"), userField,
                new Label("Contraseña:"), passField,
                buttons, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Cyber Defense Duel — Login");
        stage.show();
    }

    private void goToGame() {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Cyber Defense Duel");
        gameView.requestFocus();
        gameView.startGame();
    }
}