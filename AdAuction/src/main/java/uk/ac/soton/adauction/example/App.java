package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.AgeMetricsLoader;
import uk.ac.soton.adauction.example.FetchData.GenderMetricsLoader;
import uk.ac.soton.adauction.example.FetchData.IncomeMetricsLoader;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader;
    private static GenderMetricsLoader genderMetricsLoader;
    private static AgeMetricsLoader ageMetricsLoader;
    private static IncomeMetricsLoader incomeMetricsLoader;

    @Override


    public void start(Stage stage) throws IOException, SQLException {

        initialise(stage);
        sceneManager.switchTo("login");
        stage.show();

    }

    private void initialise(Stage stage) throws IOException, SQLException {
        sceneManager = new SceneManager(stage);
        metricsLoader = new MetricsLoader();
        genderMetricsLoader = new GenderMetricsLoader();
        ageMetricsLoader = new AgeMetricsLoader();
        incomeMetricsLoader = new IncomeMetricsLoader();

        sceneManager.loadScenes();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneController() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}

    public static GenderMetricsLoader getGenderMetricsLoader() {return genderMetricsLoader;}

    public static AgeMetricsLoader getAgeMetricsLoader(){return ageMetricsLoader;}

    public static IncomeMetricsLoader getIncomeMetricsLoader(){return incomeMetricsLoader;}

}