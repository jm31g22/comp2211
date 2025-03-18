package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.Utils.DatabaseCacher;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader;

    @Override


    public void start(Stage stage) throws SQLException {

        initialise(stage);
        sceneManager.switchTo("dashboard");
        stage.show();

    }

    private void initialise(Stage stage) throws SQLException {
        DatabaseCacher.cacheDatabase(DatabaseConnection.getRemoteLogsConnection());
        sceneManager = new SceneManager(stage);
        metricsLoader = new MetricsLoader();
        sceneManager.loadScenes();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}

}