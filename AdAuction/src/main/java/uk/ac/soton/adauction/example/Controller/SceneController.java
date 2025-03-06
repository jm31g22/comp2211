package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import uk.ac.soton.adauction.example.App;

import java.io.IOException;

abstract class SceneController {

    public void switchToDashboard(ActionEvent event) throws IOException {
        App.getSceneController().switchTo("dashboard");
    }
    public void switchToCharts(ActionEvent event) throws IOException {
        App.getSceneController().switchTo("charts");
    }

}
