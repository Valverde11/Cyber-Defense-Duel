package main;

import javafx.application.Application;
import javafx.stage.Stage;
import logic.GameConfig;
import ui.LoginScreen;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GameConfig config = new GameConfig();

        config.initialHp = 100;
        config.baseSpawnRate = 1.0;
        config.baseAttackSpeed = 2.0;
        config.scorePerKill = 50;
        config.difficultyStepScore = 100;
        config.spawnMultiplierPerLevel = 1.15;
        config.speedAddPerLevel = 0.3;

        config.damageYellow = 4;
        config.damageRed = 8;
        config.damageBlue = 10;

        new LoginScreen(stage, config).show();
    }

    public static void main(String[] args) {
        launch();
    }
}