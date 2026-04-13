package main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginScreen;

public class Main extends Application {

    @Override
    public void start(Stage stage) { // Punto de entrada de la aplicacion JavaFX
        new LoginScreen(stage).show(); // Muestra pantalla de login como primer nivel
    }

    public static void main(String[] args) { // Punto de entrada de la JVM
        launch(); // Inicializa la aplicacion JavaFX
    }
}