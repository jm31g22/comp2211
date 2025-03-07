package uk.ac.soton.adauction.example;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.ac.soton.adauction.example.Controller.SceneController;
import uk.ac.soton.adauction.example.Controller.SettingsController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class SceneManager {

    private final Stage primaryStage;

    private final HashMap<String, Scene> sceneCache = new HashMap<>();
    private final HashMap<String, SceneController> controllerCache = new HashMap<>();
    public SceneManager(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadScene(String name, String fxmlPath) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        SceneController controller = loader.getController();

        sceneCache.put(name, scene);
        controllerCache.put(name, controller);

    }

    public void switchTo(String name) {

        Scene scene = sceneCache.get(name);
        primaryStage.setTitle("AdGuru - " + name);

        if (scene != null) {
            if (name.equals("settings")) {
                try {
                    controllerCache.get("settings").refreshScene();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            fadeInScene(scene);
        } else {
            System.out.println("Scene not found");
        }
    }

    private void fadeInScene(Scene scene) {
        scene.getRoot().setOpacity(0);
        primaryStage.setScene(scene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), scene.getRoot());
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}
