package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.Utils.CampaignImporter;

import java.sql.SQLException;
import java.util.List;

public class App extends Application {

    private static SceneManager sceneManager;
    private static MetricsLoader metricsLoader = new MetricsLoader();

    public void start(Stage stage) throws SQLException {

        List<String> args = getParameters().getRaw();

        long start = System.currentTimeMillis();
        sceneManager = new SceneManager(stage);
        String pathPrefix = "AdAuction/src/main/java/uk/ac/soton/adauction/example/";

        //Comment this out if you already have db (for quick testing)
        CampaignImporter.importCampaign(pathPrefix + args.get(0),
                pathPrefix + args.get(1), pathPrefix + args.get(2));


        sceneManager.loadScenes();
        sceneManager.switchTo("dashboard");
        stage.show();

        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start)/1000 + " s");
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static MetricsLoader getMetricsLoader() {return metricsLoader;}

}