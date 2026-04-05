package main;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginScreen;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        new LoginScreen(stage).show();
    }

    public static void main(String[] args) {
        launch();
    }
}