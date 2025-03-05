package uk.ac.soton.adauction.example.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;

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

        metricValuePairs = App.getMetricsLoader().getMetricValuePairs();

        initializeLabels();
    }

    private void initializeLabels() {
        impressionsLabel.setText("Number of Impressions: " + metricValuePairs.get("NumberOfImpressions"));
        conversionsLabel.setText("Number of Conversions: " + metricValuePairs.get("NumberOfConversions"));
        clicksLabel.setText("Number of Clicks: " + metricValuePairs.get("NumberOfClicks"));
        uniquesLabel.setText("Number of Uniques: " + metricValuePairs.get("NumberOfUniques"));
        costLabel.setText("Total Cost: " + "$" + metricValuePairs.get("TotalCost"));
        CPALabel.setText("CPA: " + metricValuePairs.get("CPA"));
        CPCLabel.setText("CPC: " + metricValuePairs.get("CPC"));
        CTRLabel.setText("CTR: " + metricValuePairs.get("CTR"));
        CPMLabel.setText("CPM: " + metricValuePairs.get("CPM"));

    }
}
