package uk.ac.soton.adauction.example.Controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.FetchData.MetricsLoader;
import uk.ac.soton.adauction.example.SettingsState;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;

import static javafx.beans.binding.Bindings.createStringBinding;

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

    private HashMap<String, SimpleStringProperty> metricValuePairs;

    public DashboardController() {

    }

    /**
     * Include things that needs to be done on the first launch of this scene
     * @throws SQLException
     */
    public void initialize() throws SQLException {

        metricValuePairs = App.getMetricsLoader().loadAllMetrics();
        initializeLabels();

    }

    private void initializeLabels() {

        impressionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Number of Impressions: " + metricValuePairs.get("NumberOfImpressions").get(),
                metricValuePairs.get("NumberOfImpressions")
        ));
        conversionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Number of Conversions: " + metricValuePairs.get("NumberOfConversions").get(),
                metricValuePairs.get("NumberOfConversions")
        ));
        clicksLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Number of Clicks: " + metricValuePairs.get("NumberOfClicks").get(),
                metricValuePairs.get("NumberOfClicks")
        ));
        uniquesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Number of Uniques: " + metricValuePairs.get("NumberOfUniques").get(),
                metricValuePairs.get("NumberOfUniques")
        ));
        costLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Total Cost: $" + metricValuePairs.get("TotalCost").get(),
                metricValuePairs.get("TotalCost")
        ));
        CPALabel.textProperty().bind(Bindings.createStringBinding(
                () -> "CPA: " + metricValuePairs.get("CPA").get(),
                metricValuePairs.get("CPA")
        ));
        CPCLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "CPC " + metricValuePairs.get("CPC").get(),
                metricValuePairs.get("CPC")
        ));
        CPMLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "CPM: " + metricValuePairs.get("CPM").get(),
                metricValuePairs.get("CPM")
        ));
        CTRLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "CTR: " + metricValuePairs.get("CTR").get(),
                metricValuePairs.get("CTR")
        ));
        bouncesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Number of Bounces: " + metricValuePairs.get("NumberOfBounces").get(),
                metricValuePairs.get("NumberOfBounces")
        ));
        bounceRateLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "Bounce Rate: " + metricValuePairs.get("BounceRate").get(),
                metricValuePairs.get("BounceRate")
        ));
    }

    @Override
    public void refreshScene() throws SQLException {
        metricValuePairs = App.getMetricsLoader().loadBounceMetrics();
    }
}
