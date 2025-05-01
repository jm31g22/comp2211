package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.Utils.CampaignImporter;

import java.sql.SQLException;
import java.util.List;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader = new MetricsLoader();
    private static AppState appState = new AppState();

    @Override
    public void start(Stage stage) throws SQLException {
        List<String> args = getParameters().getRaw();
        String pathPrefix = "AdAuction/src/main/java/uk/ac/soton/adauction/example/";

        sceneManager = new SceneManager(stage);
        sceneManager.loadScenes();
        sceneManager.switchTo("loading"); // Show loading screen immediately
        stage.show();

        long start = System.currentTimeMillis();

        // Background task to import campaign
        Task<Void> importTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                CampaignImporter.importCampaign(
                        pathPrefix + args.get(0),
                        pathPrefix + args.get(1),
                        pathPrefix + args.get(2)
                );
                return null;
            }
        };

        importTask.setOnSucceeded(e -> {
            long end = System.currentTimeMillis();
            System.out.println("Elapsed time: " + (end - start) / 1000 + " s");
            sceneManager.loadScenes();
            Platform.runLater(() -> sceneManager.switchTo("login"));
        });

        importTask.setOnFailed(e -> {
            Throwable ex = importTask.getException();
            ex.printStackTrace();
            Platform.runLater(() -> {
                // Optional: switch to error scene or display error message
                System.out.println("Campaign import failed: " + ex.getMessage());
            });
        });

        new Thread(importTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {
        return metricsLoader;
    }

    public static AppState getAppState() {
        return appState;
    }
}
