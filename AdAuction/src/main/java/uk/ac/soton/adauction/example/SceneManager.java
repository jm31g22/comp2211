package uk.ac.soton.adauction.example;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.ac.soton.adauction.example.Controller.SceneController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class SceneManager {

    private final Stage primaryStage;

    private final HashMap<String, Scene> sceneCache = new HashMap<>();
    private final HashMap<String, SceneController> controllerCache = new HashMap<>();

    private String lastScene = "dashboard";
    private String currentScene = "dashboard";

    public SceneManager(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Loads all the scenes beforehand to reduce lag during use.
     */
    public void loadScenes() {
        loadScene("login", "Controller/login.fxml");
        loadScene("dashboard", "Controller/dashboard.fxml");
        loadScene("settings", "Controller/settings.fxml");
        loadScene("charts", "Controller/charts.fxml");
        loadScene("login", "Controller/login.fxml");

    }

    /**
     * Loads a scene by reading its fxml file
     * @param name Name of the scene. will be saved as this from here onwards
     * @param fxmlPath Path to the relevant fxml file that the scene is loaded from
     */
    public void loadScene(String name, String fxmlPath) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root);
        SceneController controller = loader.getController();

        sceneCache.put(name, scene);
        controllerCache.put(name, controller);

    }

    /**
     * Switches to a new scene with a given name.
     *
     * @param name Name of the scene. Must match the name in Hashmap sceneCache
     */
    public void switchTo(String name) {
        lastScene = currentScene;
        currentScene = name;


        System.out.println("Switching to scene: " + name);
        Scene scene = sceneCache.get(name);
        primaryStage.setTitle("AdGuru - " + name);

        //Carries out any refreshing that needs to be done
        try {
            controllerCache.get(name).refreshScene();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        fadeInScene(scene);
    }



    /**
     * Displays the scene using a fade transition. Used to make the transition between scenes smoother.
     * @param scene Scene object that will now be loaded on the primaryStage
     */
    private void fadeInScene(Scene scene) {
        scene.getRoot().setOpacity(0);
        primaryStage.setScene(scene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), scene.getRoot());
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Switches to last scene by calling switchTo on the lastScene field that we've stored.
     */
    public void switchToLastScene() {
        switchTo(lastScene);
    }
}
