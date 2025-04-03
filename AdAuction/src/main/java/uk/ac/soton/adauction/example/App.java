package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.Utils.DatabaseCacher;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader = new MetricsLoader();

    public void start(Stage stage) throws SQLException, IOException {

        // COMMENT THIS LINE OUT IF YOU ALREADY HAVE THE DATABASE ON YOUR MACHINE
        File dbFile = new File("logs.db");
        if (!dbFile.exists()){
            DatabaseCacher.cacheDatabase(DatabaseConnection.getLocalLogsConnection());

        }
        sceneManager = new SceneManager(stage);
        sceneManager.loadScenes();
        sceneManager.switchTo("dashboard");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}

}