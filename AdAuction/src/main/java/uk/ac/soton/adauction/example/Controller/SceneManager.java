package uk.ac.soton.adauction.example.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class SceneManager {

    private Stage primaryStage;

    private HashMap<String, Scene> sceneCache = new HashMap<>();

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

        if (scene != null) {
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            System.out.println("Scene not found");
        }
    }
}
