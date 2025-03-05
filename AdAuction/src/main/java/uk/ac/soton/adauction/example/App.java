package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static SceneManager sceneController;


    @Override
    public void start(Stage stage) throws IOException {
        sceneController = new SceneManager(stage);

        sceneController.loadScene("dashboard", "dashboard.fxml");
        sceneController.loadScene("charts", "charts.fxml");

        sceneController.switchTo("dashboard");

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneController;
    }
}