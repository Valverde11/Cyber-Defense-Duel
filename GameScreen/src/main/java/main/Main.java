package main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.GameView;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        GameView game = new GameView();
        Scene scene = new Scene(game, 1280, 720);
        stage.setTitle("Cyber Defense Duel");
        stage.setScene(scene);
        stage.show();
        
        game.requestFocus();
        game.startGame();
    }
    public static void main(String[] args) {
        launch();
    }
}
