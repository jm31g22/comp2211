package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader;


    @Override


    public void start(Stage stage) throws IOException, SQLException {
        sceneManager = new SceneManager(stage);
        initialise();

        sceneManager.switchTo("login");

        stage.show();

    }

    private void initialise() throws IOException, SQLException {
        metricsLoader = new MetricsLoader();
        metricsLoader.loadMetrics();

        sceneManager.loadScene("login", "Controller/loginPage-view.fxml");
        sceneManager.loadScene("dashboard", "Controller/dashboard.fxml");
        sceneManager.loadScene("charts", "Controller/charts.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}
}