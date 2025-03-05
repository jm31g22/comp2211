package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneController;
    private static MetricsLoader metricsLoader;


    @Override
    public void start(Stage stage) throws IOException, SQLException {
        sceneController = new SceneManager(stage);

        initialise();

        sceneController.switchTo("dashboard");

        stage.show();
    }

    private void initialise() throws IOException, SQLException {
        metricsLoader = new MetricsLoader();
        metricsLoader.loadMetrics();

        sceneController.loadScene("dashboard", "Controller/dashboard.fxml");
        sceneController.loadScene("charts", "Controller/charts.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneController;
    }
    public static MetricsLoader getMetricsLoader() {return metricsLoader;}
}