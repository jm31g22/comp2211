package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import uk.ac.soton.adauction.example.App;

import java.io.IOException;
import java.sql.SQLException;

public abstract class SceneController {

    @FXML
    private Button backButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button chartsButton;
    @FXML
    private Button homeButton;
    @FXML
    private Button logOutButton;
    @FXML
    private Button importButton;
    @FXML
    private Button adminButton;
    @FXML
    private Button comparisonButton;

    /**
     * initialise, ensure button actions are set
     */
    @FXML
    protected void initialize() {
        if (backButton != null) {
            backButton.setOnAction(e -> switchToLastScene());
        }
        if (dashboardButton != null) {
            dashboardButton.setOnAction(e -> switchToDashboard());
        }
        if (settingsButton != null) {
            settingsButton.setOnAction(e -> switchToSettings());
        }
        if (chartsButton != null) {
            chartsButton.setOnAction(e -> switchToCharts());
        }
        if (homeButton != null) {
            homeButton.setOnAction(e -> switchToDashboard());
        }
        if (adminButton != null){
            adminButton.setOnAction(e -> switchToAdmin());
        }
        if (logOutButton != null) {
            logOutButton.setOnAction(e -> switchToLogin());
        }
        if (importButton != null) {
            importButton.setOnAction(e -> openImport());
        }
        if (comparisonButton != null) {
            comparisonButton.setOnAction(e -> switchToComparison());
        }
     }
    public void switchToDashboard(){
        App.getSceneManager().switchTo("dashboard");
    }
    public void switchToCharts(){
        App.getSceneManager().switchTo("charts");
    }
    public void switchToSettings() {
        App.getSceneManager().switchTo("settings");
    }
    public void switchToLogin() {
        App.getSceneManager().switchTo("login"); }
    public void switchToAdmin() {
        App.getSceneManager().switchTo("admin"); }
    public void switchToComparison(){
        App.getSceneManager().switchTo("comparison");
    }
    public void openImport() { App.getSceneManager().openNew("Controller/import.fxml", 500, 563
    ); }
    /**
     * Anything that needs to be done each time the scene is opened should be included in this method.
     * @throws SQLException
     */
    public abstract void refreshScene() throws SQLException;

    /**
     * Switches to the last used scene.
     */
    public void switchToLastScene()  {
        App.getSceneManager().switchToLastScene();
    }


}
