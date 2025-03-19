package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.Utils.DatabaseCacher;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader = new MetricsLoader();

    public void start(Stage stage) throws SQLException {

        sceneManager = new SceneManager(stage);
        sceneManager.loadScenes();
        sceneManager.switchTo("login");

    }


    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}

}