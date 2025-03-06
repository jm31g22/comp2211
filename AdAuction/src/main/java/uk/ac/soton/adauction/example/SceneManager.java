package uk.ac.soton.adauction.example;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;

public class SceneManager {

    private final Stage primaryStage;

    private final HashMap<String, Scene> sceneCache = new HashMap<>();

    public SceneManager(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadScene(String name, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        sceneCache.put(name, scene);
    }

    public void switchTo(String name) {
        Scene scene = sceneCache.get(name);
        primaryStage.setTitle("AdGuru - " + name);

        if (scene != null) {

            scene.getRoot().setOpacity(0);
            primaryStage.setScene(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(100), scene.getRoot());
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            System.out.println("Scene not found");
        }
    }
}
