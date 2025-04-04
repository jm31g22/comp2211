package uk.ac.soton.adauction.example.Controller;

import com.itextpdf.text.DocumentException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.Utils.Export;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DashboardController extends SceneController {
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
    @FXML
    private RadioButton ageButton1;
    @FXML
    private RadioButton ageButton2;
    @FXML
    private RadioButton ageButton3;
    @FXML
    private RadioButton ageButton4;
    @FXML
    private RadioButton ageButton5;
    @FXML
    private RadioButton ageButton6;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private VBox metricsPane;
    @FXML
    private ImageView iconMetrics;
    @FXML
    private Label labelMetrics;
    @FXML
    private Label definitionMetrics;
    @FXML
    private Label exportLabel;

    private HashMap<String, SimpleStringProperty> metricValuePairs;
    private final ToggleGroup genderToggleGroup = new ToggleGroup();
    private final ToggleGroup incomeToggleGroup = new ToggleGroup();
    private final ToggleGroup ageToggleGroup = new ToggleGroup();
    private final ToggleGroup contextToggleGroup = new ToggleGroup();
    private static final HashMap<String, List<String>> metricsMap = new HashMap<>() {{
        put("Number of Impression", Arrays.asList("""
                An impression occurs whenever an ad is\s
                shown to a user, regardless of whether they\s
                click on it""", "icons/eye.png"));
        put("Number of Clicks", Arrays.asList("A click occurs when a user clicks on \n" +
                "an ad that is shown to them",
                "icons/click.png"));
        put("Number of Conversions", Arrays.asList("A conversion, or acquisition, occurs \n" +
                "when a user clicks and then acts on an ad", "icons/conversion.png"));
        put("Number of Uniques", Arrays.asList("The number of unique users that click on an ad \n" +
                "during the course of a campaign",
                "icons/click.png"));
        put("Number of Bounces", Arrays.asList("""
                A user clicks on an ad, but then fails to \s
                interact with the website (The definition \s
                of a bounce can be changed in the \s
                settings page)""", "icons/bounce.png"));
        put("CTR", Arrays.asList("The average number of clicks per impression", "icons/CPA.png"));
        put("CPA", Arrays.asList("The average amount of money spent on \n" +
                "an advertising campaign for each conversion", "icons/CPA.png"));
        put("CPC", Arrays.asList("The average amount of money spent on an \n" +
                "advertising campaign for each click",
                "icons/CPA.png"));
        put("CPM", Arrays.asList("The average amount of money spent on an advertising \n" +
                "campaign for every one thousand impressions", "icons/CPM.png"));
        put("Total Cost", Arrays.asList("The total cost for all clicks and impression ", "icons/cost.png"));
        put("Bounce Rate", Arrays.asList("""
                The average number of bounces per click\s
                (The definition of a bounce can be changed\s
                in the settings page)""", "icons/bounce.png"));
    }};




    /**
     * Include things that needs to be done on the first launch of this scene
     */
    public void initialize() {

        assignButtonGroups();

        super.initialize();
        try {
            metricValuePairs = App.getMetricsLoader().loadAllMetrics("All", "All", "All", "All", "Start", "End");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initializeLabels();
        metricsPane.opacityProperty().setValue(0);


    }

    /**
     * Assign the button groups for the filters
     */
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

        ageButton1.setToggleGroup(ageToggleGroup);
        ageButton2.setToggleGroup(ageToggleGroup);
        ageButton3.setToggleGroup(ageToggleGroup);
        ageButton4.setToggleGroup(ageToggleGroup);
        ageButton5.setToggleGroup(ageToggleGroup);
        ageButton6.setToggleGroup(ageToggleGroup);
    }

    /**
     * Initialize all key metrics label shown on dashboard page
     */
    private void initializeLabels() {
        loadNoOfImpression();
        loadNoOfClick();
        loadNoOfConversion();
        loadNoOfUniques();
        loadTotalCost();
        loadCPA();
        loadCPC();
        loadCPM();
        loadCTR();
        loadNoOfBounces();
        loadBounceRate();
    }

    /**
     * Load the number of impression label and assign event after clicking on the label
     */
    private void loadNoOfImpression(){
        impressionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfImpressions").get(),
                metricValuePairs.get("NumberOfImpressions")
        ));
        impressionsLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Number of Impression", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the number of click label and assign event after clicking on the label
     */
    private void loadNoOfClick(){
        clicksLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfClicks").get(),
                metricValuePairs.get("NumberOfClicks")
        ));
        clicksLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Number of Clicks", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the number of conversion label and assign event after clicking on the label
     */
    private void loadNoOfConversion(){
        conversionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfConversions").get(),
                metricValuePairs.get("NumberOfConversions")
        ));
        conversionsLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Number of Conversions", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the number of uniques label and assign event after clicking on the label
     */
    private void loadNoOfUniques(){
        uniquesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfUniques").get(),
                metricValuePairs.get("NumberOfUniques")
        ));
        uniquesLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Number of Uniques", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the number of bounces label and assign event after clicking on the label
     */
    private void loadNoOfBounces(){
        bouncesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfBounces").get(),
                metricValuePairs.get("NumberOfBounces")
        ));
        bouncesLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Number of Bounces", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the CPA label and assign event after clicking on the label
     */
    private void loadCPA(){
        CPALabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("CPA").get(),
                metricValuePairs.get("CPA")
        ));
        CPALabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("CPA", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the CPC label and assign event after clicking on the label
     */
    private void loadCPC(){
        CPCLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("CPC").get(),
                metricValuePairs.get("CPC")
        ));
        CPCLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("CPC", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the CPM label and assign event after clicking on the label
     */
    private void loadCPM(){
        CPMLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("CPM").get(),
                metricValuePairs.get("CPM")
        ));
        CPMLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("CPM", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the CTR label and assign event after clicking on the label
     */
    private void loadCTR(){
        CTRLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("CTR").get(),
                metricValuePairs.get("CTR")
        ));
        CTRLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("CTR", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the bounce rate label and assign event after clicking on the label
     */
    private void loadBounceRate(){
        bounceRateLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("BounceRate").get(),
                metricValuePairs.get("BounceRate")
        ));
        bounceRateLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Bounce Rate", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Load the total cost label and assign event after clicking on the label
     */
    private void loadTotalCost(){
        costLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "$" + metricValuePairs.get("TotalCost").get(),
                metricValuePairs.get("TotalCost")
        ));
        costLabel.setOnMouseClicked(mouseEvent -> {
            try {
                setMetricsPane("Total Cost", mouseEvent.getSceneX(),
                        mouseEvent.getSceneY());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Set the metrics pane to be visible at coordX and coordY and assign values in the metrics pane
     * @param metrics name of the metrics
     * @param coordX x-coordinate of the popup
     * @param coordY y-coordinate of the popup
     * @throws FileNotFoundException exception thrown when image path not found
     */
    private void setMetricsPane(String metrics, double coordX, double coordY) throws FileNotFoundException {
        metricsPane.setLayoutX(coordX-100);
        metricsPane.setLayoutY(coordY-100);
        metricsPane.opacityProperty().setValue(1);
        URL url = getClass().getResource(metricsMap.get(metrics).get(1));
        iconMetrics.setImage(new Image(url.toExternalForm()));
        labelMetrics.setText(metrics);
        definitionMetrics.setText(metricsMap.get(metrics).get(0));
        metricsPane.setOnMouseClicked(mouseEvent -> {
            closeMetricsPane();
        });
    }

    /**
     * function to reset metrics pane location (so it would not affect other touchable objects) and hide the component
     */
    private void closeMetricsPane(){
        metricsPane.setLayoutX(73.0);
        metricsPane.setLayoutY(255.0);
        metricsPane.opacityProperty().setValue(0);
    }

    /**
     * Include anything that needs to be done EACH time the scene is opened
     * @throws SQLException exception thrown when have error when fetching accessing database
     */
    @Override
    public void refreshScene() throws SQLException {

        metricValuePairs = App.getMetricsLoader().loadAllMetrics("All", "All", "All", "All", "Start", "End");
    }

    /**
     * Method allowing filters to be applied to the key metrics
     * @throws SQLException exception thrown when have error when fetching accessing database
     */
    @FXML
    public void applyFilters() throws SQLException {
        RadioButton selectedGenderRadio = (RadioButton) genderToggleGroup.getSelectedToggle();
        String gender = (selectedGenderRadio != null) ? selectedGenderRadio.getText() : "All"; // Default to "All" if nothing is selected

        RadioButton selectedIncomeRadio = (RadioButton) incomeToggleGroup.getSelectedToggle();
        String income = (selectedIncomeRadio != null) ? selectedIncomeRadio.getText() : "All";

        RadioButton selectedContextRadio = (RadioButton) contextToggleGroup.getSelectedToggle();
        String context = (selectedContextRadio != null) ? selectedContextRadio.getText() : "All";

        RadioButton selectedAgeRadio = (RadioButton) ageToggleGroup.getSelectedToggle();
        String age = (selectedAgeRadio != null) ? selectedAgeRadio.getText() : "All";

        String startDate = (startDatePicker.getValue() != null) ? startDatePicker.getValue().toString() : "Start";
        System.out.println("Start: " + startDate);
        String endDate = (endDatePicker.getValue() != null) ? endDatePicker.getValue().toString() : "End";
        System.out.println("End: " + endDate);

        metricValuePairs = App.getMetricsLoader().loadAllMetrics(age, gender, income, context, startDate, endDate);
    }

    @FXML
    private void exportCSV() {
        try {
            Export.export(metricValuePairs, "csv", "Downloads");

            exportLabel.setText("CSV saved to Downloads!");
        } catch (IOException | DocumentException e) {
            exportLabel.setText("Something went wrong!");
            throw new RuntimeException(e);
        }

        exportLabel.setVisible(true);
    }

    @FXML
    private void exportPDF() {
        try {
            Export.export(metricValuePairs, "pdf", "Downloads");

            exportLabel.setText("PDF saved to Downloads!");
        } catch (IOException | DocumentException e) {
            exportLabel.setText("Something went wrong!");
            throw new RuntimeException(e);
        }

        exportLabel.setVisible(true);

    }
}
