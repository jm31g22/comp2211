package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import uk.ac.soton.adauction.example.App;

import java.io.IOException;
import java.sql.SQLException;

public abstract class SceneController {

    public void switchToDashboard(ActionEvent event) throws IOException {

        App.getSceneController().switchTo("dashboard");
    }
    public void switchToCharts(ActionEvent event) throws IOException {
        App.getSceneController().switchTo("charts");
    }
    public void switchToSettings(ActionEvent event) throws IOException {
        App.getSceneController().switchTo("settings");
    }

    /**
     * Anything that needs to be done each time the scene is opened should be included in this method.
     * @throws SQLException
     */
    public abstract void refreshScene() throws SQLException;

    /**
     * Switches to the last used scene.
     * @param event Mouse click (not really useful)
     * @throws IOException
     */
    public void switchToLastScene(ActionEvent event) throws IOException {
        App.getSceneController().switchToLastScene();
    }
}
