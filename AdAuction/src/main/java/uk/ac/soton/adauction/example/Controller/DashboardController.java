package uk.ac.soton.adauction.example.Controller;

import com.itextpdf.text.DocumentException;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.Utils.Export;
import uk.ac.soton.adauction.example.AppState;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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
    @FXML
    private StackPane impressionIcon;
    @FXML
    private StackPane clickIcon;
    @FXML
    private StackPane conversionIcon;
    @FXML
    private StackPane uniquesIcon;
    @FXML
    private StackPane bouncesIcon;
    @FXML
    private StackPane CPMIcon;
    @FXML
    private StackPane CTRIcon;
    @FXML
    private StackPane costIcon;
    @FXML
    private StackPane CPCIcon;
    @FXML
    private StackPane CPAIcon;
    @FXML
    private StackPane bounceRateIcon;
    @FXML
    private HBox impressionBlock;
    @FXML
    private HBox clickBlock;
    @FXML
    private HBox conversionBlock;
    @FXML
    private HBox uniqueBlock;
    @FXML
    private HBox bounceBlock;
    @FXML
    private HBox CPMBlock;
    @FXML
    private HBox CTRBlock;
    @FXML
    private HBox costBlock;
    @FXML
    private HBox CPCBlock;
    @FXML
    private HBox CPABlock;
    @FXML
    private HBox bounceRateBlock;
    @FXML
    private HBox dashboardBlock1;
    @FXML
    private HBox dashboardBlock2;
    @FXML
    private HBox dashboardBlock3;
    @FXML
    private HBox dashboardBlock4;

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
                "icons/unique.png"));
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
                "icons/CPM.png"));
        put("CPM", Arrays.asList("The average amount of money spent on an advertising \n" +
                "campaign for every one thousand impressions", "icons/CPM.png"));
        put("Total Cost", Arrays.asList("The total cost for all clicks and impression ", "icons/cost.png"));
        put("Bounce Rate", Arrays.asList("""
                The average number of bounces per click\s
                (The definition of a bounce can be changed\s
                in the settings page)""", "icons/bounce.png"));
    }};
    private static final String[] colors = new String[]{"#d3ffe7","#caf1ff","#ffa3cf"};


    /**
     * Include things that needs to be done on the first launch of this scene
     */
    public void initialize() {
        Date startDate = App.getMetricsLoader().getStartDate();
        Date endDate = App.getMetricsLoader().getEndDate();
        if (startDate != null && endDate != null){
            LocalDate startDateLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDateLocal = App.getMetricsLoader().getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            startDatePicker.setDayCellFactory(p -> new DateCell(){
                public void updateItem(LocalDate date, boolean empty){
                    super.updateItem(date, empty);
                    if (empty || date.isBefore(startDateLocal) || date.isAfter(endDateLocal)){
                        setDisable(true);
                        System.out.println("Dates are disabled before: " + startDateLocal + " and after: " + endDateLocal);
                    }
                }
            });
        }
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
        int mode = AppState.getDashboardMode();
        dashboardBlock1.getChildren().clear();
        dashboardBlock1.opacityProperty().setValue(0);
        dashboardBlock2.getChildren().clear();
        dashboardBlock2.opacityProperty().setValue(0);
        dashboardBlock3.getChildren().clear();
        dashboardBlock3.opacityProperty().setValue(0);
        dashboardBlock4.getChildren().clear();
        dashboardBlock4.opacityProperty().setValue(0);
        switch (mode) {
            case 0 -> loadInfluencerMode();
            case 1 -> loadEntrepreneurMode();
            case 3 -> loadCustomizedMode();
            default -> loadAnalystMode();
        }
    }

    /**
     * Load analyst mode
     */
    private void loadAnalystMode(){
        loadNoOfImpression(1);
        loadNoOfClick(1);
        loadNoOfConversion(1);
        loadNoOfUniques(1);
        loadNoOfBounces(2);
        loadCPM(2);
        loadCTR(2);
        loadTotalCost(2);
        loadCPC(3);
        loadCPA(3);
        loadBounceRate(3);
        dashboardBlock1.getChildren().addAll(impressionBlock, bounceBlock, CPCBlock);
        dashboardBlock1.opacityProperty().setValue(1);
        dashboardBlock2.getChildren().addAll(clickBlock, CPMBlock, CPABlock);
        dashboardBlock2.opacityProperty().setValue(1);
        dashboardBlock3.getChildren().addAll(conversionBlock, CTRBlock, bounceRateBlock);
        dashboardBlock3.opacityProperty().setValue(1);
        dashboardBlock4.getChildren().addAll(uniqueBlock, costBlock);
        dashboardBlock4.opacityProperty().setValue(1);
    }

    /**
     * load influencer mode
     */
    private void loadInfluencerMode(){
        loadNoOfImpression(1);
        loadNoOfClick(2);
        loadNoOfConversion(3);
        loadNoOfUniques(1);
        loadCTR(2);
        dashboardBlock1.getChildren().addAll(impressionBlock, clickBlock, conversionBlock);
        dashboardBlock1.opacityProperty().setValue(1);
        dashboardBlock2.getChildren().addAll(uniqueBlock, CTRBlock);
        dashboardBlock2.opacityProperty().setValue(1);
    }

    /**
     * Load entrepreneur mode
     */
    private void loadEntrepreneurMode(){
        loadNoOfClick(1);
        loadNoOfConversion(2);
        loadNoOfUniques(3);
        loadTotalCost(1);
        dashboardBlock1.getChildren().addAll(clickBlock, conversionBlock, uniqueBlock);
        dashboardBlock1.opacityProperty().setValue(1);
        dashboardBlock2.getChildren().addAll(costBlock);
        dashboardBlock2.opacityProperty().setValue(1);
    }

    /**
     * load customised mode
     */
    private void loadCustomizedMode(){
        ArrayList<Integer> components = AppState.getComponents();
        HBox[] blockList = new HBox[]{dashboardBlock1, dashboardBlock2, dashboardBlock3, dashboardBlock4};
        int blockIndex = 0;
        HBox currentBlock = blockList[blockIndex];
        int offset = 1;
        for (Integer component : components) {
            currentBlock.opacityProperty().setValue(1);
            switch (component) {
                case 0:
                    loadNoOfImpression(offset);
                    currentBlock.getChildren().add(impressionBlock);
                    break;
                case 1:
                    loadNoOfClick(offset);
                    currentBlock.getChildren().add(clickBlock);
                    break;
                case 2:
                    loadNoOfConversion(offset);
                    currentBlock.getChildren().add(conversionBlock);
                    break;
                case 3:
                    loadNoOfUniques(offset);
                    currentBlock.getChildren().add(uniqueBlock);
                    break;
                case 4:
                    loadNoOfBounces(offset);
                    currentBlock.getChildren().add(bounceBlock);
                    break;
                case 5:
                    loadCPM(offset);
                    currentBlock.getChildren().add(CPMBlock);
                    break;
                case 6:
                    loadCTR(offset);
                    currentBlock.getChildren().add(CTRBlock);
                    break;
                case 7:
                    loadTotalCost(offset);
                    currentBlock.getChildren().add(costBlock);
                    break;
                case 8:
                    loadCPC(offset);
                    currentBlock.getChildren().add(CPCBlock);
                    break;
                case 9:
                    loadCPA(offset);
                    currentBlock.getChildren().add(CPABlock);
                    break;
                case 10:
                    loadBounceRate(offset);
                    currentBlock.getChildren().add(bounceRateBlock);
                    break;
            }
            offset++;
            if (offset > 3) {
                blockIndex++;
                currentBlock = blockList[blockIndex];
                offset = 1;
            }
        }
    }

    /**
     * Load icon given:
     * @param i
     * @param imagePath - path to icon
     * @param stackPane - parent pane
     */
    private void loadIcon(int i, String imagePath, StackPane stackPane){
        Circle circle = new Circle(24);
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 1.0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(1, Color.web(colors[i-1]))
        );
        circle.setFill(gradient);
        URL url = getClass().getResource(imagePath);
        ImageView imageView = new ImageView(new Image(url.toExternalForm()));
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        stackPane.getChildren().addAll(circle, imageView);
    }

    /**
     * Load the number of impression label and assign event after clicking on the label
     */
    private void loadNoOfImpression(int i){
        String imagePath = "icons/impression" + i + ".png";
        loadIcon(i, imagePath, impressionIcon);
        impressionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfImpressions").get(),
                metricValuePairs.get("NumberOfImpressions")
        ));
        impressionBlock.setOnMouseClicked(mouseEvent -> {
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
    private void loadNoOfClick(int i){
        String imagePath = "icons/clicks" + i + ".png";
        loadIcon(i, imagePath, clickIcon);
        clicksLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfClicks").get(),
                metricValuePairs.get("NumberOfClicks")
        ));
        clickBlock.setOnMouseClicked(mouseEvent -> {
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
    private void loadNoOfConversion(int i){
        String imagePath = "icons/conversion" + i + ".png";
        loadIcon(i, imagePath, conversionIcon);
        conversionsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("NumberOfConversions").get(),
                metricValuePairs.get("NumberOfConversions")
        ));
        conversionBlock.setOnMouseClicked(mouseEvent -> {
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
    private void loadNoOfUniques(int i){
        String imagePath = "icons/uniques" + i + ".png";
        loadIcon(i, imagePath, uniquesIcon);
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
    private void loadNoOfBounces(int i){
        String imagePath = "icons/bounce" + i + ".png";
        loadIcon(i, imagePath, bouncesIcon);
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
    private void loadCPA(int i){
        String imagePath = "icons/CTR" + i + ".png";
        loadIcon(i, imagePath, CPAIcon);
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
    private void loadCPC(int i){
        String imagePath = "icons/CPM" + i + ".png";
        loadIcon(i, imagePath, CPCIcon);
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
    private void loadCPM(int i){
        String imagePath = "icons/CPM" + i + ".png";
        loadIcon(i, imagePath, CPMIcon);
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
    private void loadCTR(int i){
        String imagePath = "icons/CTR" + i + ".png";
        loadIcon(i, imagePath, CTRIcon);
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
    private void loadBounceRate(int i){
        String imagePath = "icons/bounce" + i + ".png";
        loadIcon(i, imagePath, bounceRateIcon);
        bounceRateLabel.textProperty().bind(Bindings.createStringBinding(
                () -> metricValuePairs.get("BounceRate").get(),
                metricValuePairs.get("BounceRate")
        ));
        bounceRateBlock.setOnMouseClicked(mouseEvent -> {
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
    private void loadTotalCost(int i){
        String imagePath = "icons/cost" + i + ".png";
        loadIcon(i, imagePath, costIcon);
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
        metricValuePairs = App.getMetricsLoader().loadBounceMetrics();
        initializeLabels();
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

    /**
     * Export data as csv
     */
    @FXML
    private void exportCSV() {
        try {
            Export.exportMetrics(metricValuePairs, "csv", "Downloads");
            exportLabel.setText("CSV saved to Downloads!");
        } catch (IOException | DocumentException e) {
            exportLabel.setText("Something went wrong!");
            throw new RuntimeException(e);
        }
        exportLabel.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> exportLabel.setVisible(false));
        pause.play();
    }

    /**
     * Export data as pdf
     */
    @FXML
    private void exportPDF() {
        try {
            Export.exportMetrics(metricValuePairs, "pdf", "Downloads");
            exportLabel.setText("PDF saved to Downloads!");
        } catch (IOException | DocumentException e) {
            exportLabel.setText("Something went wrong!");
            throw new RuntimeException(e);
        }
        exportLabel.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> exportLabel.setVisible(false));
        pause.play();


    }
}
