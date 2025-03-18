package uk.ac.soton.adauction.example.Controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import uk.ac.soton.adauction.example.App;

import java.sql.SQLException;
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
    @FXML
    private RadioButton maleGenderButton;
    @FXML
    private RadioButton femaleGenderButton;
    @FXML
    private RadioButton bothGenderButton;
    @FXML
    private RadioButton lowIncomeButton;
    @FXML
    private RadioButton mediumIncomeButton;
    @FXML
    private RadioButton highIncomeButton;
    @FXML
    private RadioButton newsContextButton;
    @FXML
    private RadioButton blogContextButton;
    @FXML
    private RadioButton socialMediaContextButton;
    @FXML
    private RadioButton allContextButton;
    @FXML
    private RadioButton shoppingContextButton;
    @FXML
    private RadioButton allIncomeButton;


    private HashMap<String, SimpleStringProperty> metricValuePairs;
    private final ToggleGroup genderToggleGroup = new ToggleGroup();
    private final ToggleGroup incomeToggleGroup = new ToggleGroup();
    private final ToggleGroup ageToggleGroup = new ToggleGroup();
    private final ToggleGroup contextToggleGroup = new ToggleGroup();




    /**
     * Include things that needs to be done on the first launch of this scene
     */
    public void initialize() {

        assignButtonGroups();

        super.initialize();
        try {
            metricValuePairs = App.getMetricsLoader().loadAllMetrics("All", "All", "All", "All");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initializeLabels();

    }

    private void assignButtonGroups() {
        maleGenderButton.setToggleGroup(genderToggleGroup);
        femaleGenderButton.setToggleGroup(genderToggleGroup);
        bothGenderButton.setToggleGroup(genderToggleGroup);

        lowIncomeButton.setToggleGroup(incomeToggleGroup);
        mediumIncomeButton.setToggleGroup(incomeToggleGroup);
        highIncomeButton.setToggleGroup(incomeToggleGroup);
        allIncomeButton.setToggleGroup(incomeToggleGroup);


        shoppingContextButton.setToggleGroup(contextToggleGroup);
        newsContextButton.setToggleGroup(contextToggleGroup);
        blogContextButton.setToggleGroup(contextToggleGroup);
        socialMediaContextButton.setToggleGroup(contextToggleGroup);
        allContextButton.setToggleGroup(contextToggleGroup);

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

    @FXML
    public void applyFilters() throws SQLException {
        RadioButton selectedGenderRadio = (RadioButton) genderToggleGroup.getSelectedToggle();
        String gender = (selectedGenderRadio != null) ? selectedGenderRadio.getText() : "All"; // Default to "All" if nothing is selected

        RadioButton selectedIncomeRadio = (RadioButton) incomeToggleGroup.getSelectedToggle();
        String income = (selectedIncomeRadio != null) ? selectedIncomeRadio.getText() : "All";

        RadioButton selectedContextRadio = (RadioButton) contextToggleGroup.getSelectedToggle();
        String context = (selectedContextRadio != null) ? selectedContextRadio.getText() : "All";

        System.out.println(gender);
        metricValuePairs = App.getMetricsLoader().loadAllMetrics("All", gender, income, context);
    }
}
