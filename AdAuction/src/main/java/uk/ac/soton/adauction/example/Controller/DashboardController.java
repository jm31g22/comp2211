package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.FetchData.KeyMetrics;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;

public class DashboardController extends SceneController{
    @FXML
    private Label impressionsLabel;
    @FXML
    private Label clicksLabel;
    @FXML
    private Label costLabel;
    @FXML
    private Label CPALabel;
    @FXML
    private Label CPMLabel;
    @FXML
    private Label CPCLabel;
    @FXML
    private Label CTRLabel;
    @FXML
    private Label uniquesLabel;
    @FXML
    private Label bounceRateLabel;
    @FXML
    private Label conversionsLabel;
    @FXML
    private Label bouncesLabel;

    private HashMap<String, String> metricValuePairs;

    public DashboardController() {
    }

    public void initialize() {
        KeyMetrics keyMetrics = new KeyMetrics();
        keyMetrics.calculateMetrics();

        metricValuePairs = keyMetrics.getMetricValuePairs();
//
//        impressionsLabel.setText("Number of Impressions: " + metricValuePairs.get("NumberOfImpressions"));
    }


//    private String getCost() {
//
//
//        return "lool";
//    }
}
