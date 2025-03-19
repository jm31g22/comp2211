package uk.ac.soton.adauction.example.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.adauction.example.FetchData.ClickLog;
import uk.ac.soton.adauction.example.FetchData.ServerLog;
import uk.ac.soton.adauction.example.FetchData.ImpressionLog;

import java.util.*;
import java.util.function.BiConsumer;

public class ChartsSceneController extends SceneController {
    @FXML
    private PieChart impressionPie;
    @FXML
    private LineChart<String, Number> metricsLine;
    @FXML
    private ChoiceBox<String> chartSelection = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> metricSelection = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> timeSelection = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> granSelection = new ChoiceBox<>();
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private StackPane stackPaneGraph;
    @FXML
    private Button panLeftButton = new Button();
    @FXML
    private Button panRightButton = new Button();
    private int offset = 0;  // Global offset for navigation.
    @FXML
    private VBox hoverPane;
    @FXML
    private Label timeLabel;
    @FXML
    private Label valueLabel;
    @FXML
    private Label timeOrCatLabel;
    //variable to save what is the current page
    private int currentPage;
    private final ImpressionLog impressionLog;
    private final ClickLog clickLog;
    private final ServerLog serverLog;


    public ChartsSceneController() {
        impressionLog = new ImpressionLog();
        clickLog = new ClickLog();
        serverLog = new ServerLog();
    }

    //assume pie chart is shown after entering the scene
    public void initialize() {
        super.initialize();
        granSelection.opacityProperty().setValue(1);
        panLeftButton.setVisible(false);
        panRightButton.setVisible(false);
        stackPaneGraph.getChildren().clear();
        //load chart options into list
        chartSelection.getItems().setAll(
                "Metrics by time",
                "Impression Chart",
                "Histogram of click costs",
                "Bounces vs Clicks"
        );
        chartSelection.setOnAction((event) -> {
            int selectedIndex = chartSelection.getSelectionModel().getSelectedIndex();
            Object selectedItem = chartSelection.getSelectionModel().getSelectedItem();
            hoverPane.opacityProperty().setValue(0);
            if (selectedIndex == 0) {
                currentPage = 0;
                loadLineChartPage();
                panLeftButton.setVisible(true);
                panRightButton.setVisible(true);
                timeSelection.opacityProperty().setValue(1);
                granSelection.opacityProperty().setValue(1);
            } else if (selectedIndex == 1) {
                currentPage = 1;
                loadPieChartPage();
                panLeftButton.setVisible(false);
                panRightButton.setVisible(false);
                granSelection.opacityProperty().setValue(0);
                timeSelection.opacityProperty().setValue(0);
            } else if (selectedIndex == 2) {
                currentPage = 2;
                panLeftButton.setVisible(false);
                panRightButton.setVisible(false);
                granSelection.opacityProperty().setValue(0);
            }
            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
        });
    }

    /**
     * Function to modify the pie chart page and set metric selection
     */
    public void loadPieChartPage() {
        metricSelection.getItems().clear();
        stackPaneGraph.getChildren().clear();
        impressionPie.getData().clear();
        stackPaneGraph.getChildren().add(impressionPie);
        //load chart options into list
        metricSelection.getItems().add("Gender");
        metricSelection.getItems().add("Age");
        metricSelection.getItems().add("Income");
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
            hoverPane.opacityProperty().setValue(0);
            if (selectedIndex == 0) {
                loadGenderPieData();
            } else if (selectedIndex == 1) {
                loadAgePieData();
            } else if (selectedIndex == 2) {
                loadIncomePieData();
            }
        });
        timeSelection.hide();
    }

    private void hoverImpressionPane(String cat){
        impressionPie.getData().stream().forEach(data ->{
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            double coordX = e.getX();
                            double coordY = e.getY();
                            addChartHoverPane(coordX, coordY, cat, data.getName(), (int) data.getPieValue());
                        }
                    });
        });
    }

    /**
     * Function to load data into impression pie chart by gender
     */
    public void loadGenderPieData() {
        HashMap<String, Integer> counts = impressionLog.fetchImpressionGenderCount();
        double femaleCount = Math.round((float) counts.get("female") / counts.get("total") * 100);
        double maleCount = Math.round((float) counts.get("male") / counts.get("total") * 100);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Female", femaleCount),
                        new PieChart.Data("Male", maleCount));
        impressionPie.setData(pieChartData);
        hoverImpressionPane("Gender :");
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #b81370;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #2d58d6;");
        impressionPie.setPrefWidth(770.0);
        impressionPie.setPrefHeight(500.0);
        impressionPie.setLegendVisible(false);
    }
    /**
     * Function to load impression group by age pie chart
     */
    public void loadAgePieData() {
        HashMap<String, Integer> counts = impressionLog.fetchImpressionAgeCount();
        double age1Count = Math.round((float) counts.get("<25") / counts.get("total") * 100);
        double age2Count = Math.round((float) counts.get("25-34") / counts.get("total") * 100);
        double age3Count = Math.round((float) counts.get("35-44") / counts.get("total") * 100);
        double age4Count = Math.round((float) counts.get("45-54") / counts.get("total") * 100);
        double age5Count = Math.round((float) counts.get(">54") / counts.get("total") * 100);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("<25", age1Count),
                        new PieChart.Data("25-34", age2Count),
                        new PieChart.Data("35-44", age3Count),
                        new PieChart.Data("45-54", age4Count),
                        new PieChart.Data(">54", age5Count));
        impressionPie.setData(pieChartData);
        hoverImpressionPane("Age :");
        impressionPie.setPrefWidth(770.0);
        impressionPie.setPrefHeight(500.0);
        impressionPie.setLegendVisible(false);
    }

    /**
     * Function to load impression group by income pie chart
     */
    public void loadIncomePieData() {
        HashMap<String, Integer> counts = impressionLog.fetchImpressionIncomeCount();
        double income1Count = Math.round((float) counts.get("low") / counts.get("total") * 100);
        double income2Count = Math.round((float) counts.get("medium") / counts.get("total") * 100);
        double income3Count = Math.round((float) counts.get("high") / counts.get("total") * 100);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Low", income1Count),
                        new PieChart.Data("Medium", income2Count),
                        new PieChart.Data("High", income3Count));
        impressionPie.setData(pieChartData);
        hoverImpressionPane("Income :");
        impressionPie.setPrefWidth(770.0);
        impressionPie.setPrefHeight(500.0);
        impressionPie.setLegendVisible(false);
    }

    /**
     * Load data for the line chart by time and set metric selection
     */
    public void loadLineChartPage() {
        metricSelection.getItems().clear();
        stackPaneGraph.getChildren().clear();
        //load chart options into list
        metricSelection.getItems().addAll(
                "Number of Impression",
                "Number of Click",
                "Number of Unique",
                "Number of Conversion",
                "Number of Bounces"
        );
        metricSelection.setOnAction(null);
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
            if (selectedIndex < 0) {
                return; // invalid
            }
            System.out.println("Metric selected index: " + selectedIndex);
            addTimeSelection(selectedIndex);
        });

    }

    private void addTimeSelection(int graphIndex) {
        // clear old choices & selection
        timeSelection.getItems().clear();
        timeSelection.getSelectionModel().clearSelection();

        granSelection.getItems().clear();
        granSelection.getSelectionModel().clearSelection();

        // add menu items
        timeSelection.getItems().addAll("By Hour", "By Day", "By Week", "By Month");
        granSelection.getItems().addAll("Hourly", "Daily", "Weekly", "Monthly");

        // attempt graph load on selection change
        timeSelection.setOnAction(e -> attemptGraphLoad(graphIndex));
        granSelection.setOnAction(e -> attemptGraphLoad(graphIndex));
    }

    private void attemptGraphLoad(int graphIndex) {
        int timeIndex = timeSelection.getSelectionModel().getSelectedIndex();  // 0..3 or -1
        String granChoice = granSelection.getSelectionModel().getSelectedItem(); // or null

        if (timeIndex < 0 || granChoice == null) {
            // invalid choices
            return;
        }

        switch (graphIndex) {
            case 0:
                loadImpressionCountGraph(timeIndex, granChoice);
                break;
            case 1:
                loadClickCountGraph(timeIndex, granChoice);
                break;
            case 2:
                loadUniqueCountGraph(timeIndex, granChoice);
                break;
            case 3:
                loadConversionCountGraph(timeIndex, granChoice);
                break;
            case 4:
                loadBounceCountGraph(timeIndex, granChoice);
                break;
            default:
                System.err.println("Unknown metric selection index: " + graphIndex);
        }
    }


    @FunctionalInterface
    private interface DataFetcher {
        HashMap<String, Integer> fetch(String groupingGranularity, int tickIndex, int offset);
    }

    private void loadCountGraph(
            int tickIndex,
            String groupingGranularity,
            DataFetcher fetcher,                    // get the data
            String yAxisLabel,
            String seriesName,
            BiConsumer<Integer, String> reloadFunc  // reload when panning the data using arrows
    ) {
        stackPaneGraph.getChildren().clear();

        // determine x-axis label
        CategoryAxis xAxis = new CategoryAxis();
        switch (tickIndex) {
            case 0:
                System.out.println("Tick increment: Hour selected");
                xAxis.setLabel("Hour");
                break;
            case 1:
                System.out.println("Tick increment: Day selected");
                xAxis.setLabel("Date");
                break;
            case 2:
                System.out.println("Tick increment: Week selected");
                xAxis.setLabel("Week");
                break;
            case 3:
                System.out.println("Tick increment: Month selected");
                xAxis.setLabel("Month");
                break;
            default:
                throw new IllegalArgumentException("Invalid tick increment index: " + tickIndex);
        }

        // fetch data
        HashMap<String, Integer> count = fetcher.fetch(groupingGranularity, tickIndex, offset);

        // sort time buckets and add to x axis
        List<String> sortedBuckets = new ArrayList<>(count.keySet());
        Collections.sort(sortedBuckets);
        xAxis.setCategories(FXCollections.observableArrayList(sortedBuckets));

        // get y-axis range
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo - minNo) / 50;
        if (diff <= 0) {
            diff = 1;
        }
        NumberAxis yAxis = new NumberAxis(Math.max(minNo - diff, 0), maxNo + diff, diff);
        yAxis.setLabel(yAxisLabel);

        // create chart
        LineChart<String, Number> metricsLine = new LineChart<>(xAxis, yAxis);
        metricsLine.setLegendVisible(false);
        metricsLine.setPrefWidth(770.0);
        metricsLine.setPrefHeight(500.0);
        metricsLine.setHorizontalGridLinesVisible(false);
        metricsLine.setVerticalGridLinesVisible(false);
        metricsLine.setCreateSymbols(false);

        // build the data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        for (String bucket : sortedBuckets) {
            series.getData().add(new XYChart.Data<>(bucket, count.getOrDefault(bucket, 0)));
        }
        metricsLine.getData().add(series);

        // style
        Node line = series.getNode() != null
                ? series.getNode().lookup(".chart-series-line")
                : null;
        if (line != null) {
            line.setStyle("-fx-stroke: #6677b2;");
        }


        metricsLine.setAnimated(false);
        metricsLine.setCreateSymbols(true);
        line = series.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: #6677b2;");
        for (XYChart.Data<String, Number> data: series.getData()){
            Platform.runLater(()->{
                Node symbol = data.getNode().lookup(".chart-line-symbol");
                symbol.setStyle("-fx-background-color:  #1F263E, #FFFFFF");
            });
        }
        stackPaneGraph.getChildren().clear();
        stackPaneGraph.getChildren().add(metricsLine);

        // panning button functionality
        panLeftButton.setOnAction(e -> {
            offset--;
            reloadFunc.accept(tickIndex, groupingGranularity);
        });

        panRightButton.setOnAction(e -> {
            offset++;
            reloadFunc.accept(tickIndex, groupingGranularity);
        });

        for (XYChart.Data<String, Number> data: series.getData()){
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            double coordX = e.getX();
                            double coordY = e.getY();
                            addChartHoverPane(coordX, coordY, "Time: ", data.getXValue(), (Integer) data.getYValue());
                        }
                    });
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            hoverPane.opacityProperty().setValue(0);
                        }
                    });
        }

        System.out.println(seriesName + " loaded. Current offset = " + offset);
    }

    public void loadImpressionCountGraph(int tickIndex, String groupingGranularity) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                impressionLog::fetchImpressionCounts,
                "No of Impression",
                "No of Impression over time",
                this::loadImpressionCountGraph
        );
    }

    public void loadClickCountGraph(int tickIndex, String groupingGranularity) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                clickLog::fetchClickCounts,
                "No of Click",
                "No of Click over time",
                this::loadClickCountGraph
        );
    }

    public void loadUniqueCountGraph(int tickIndex, String groupingGranularity) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                clickLog::fetchUniqueCounts,
                "No of Unique",
                "No of Unique over time",
                this::loadUniqueCountGraph
        );
    }

    public void loadConversionCountGraph(int tickIndex, String groupingGranularity) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                serverLog::fetchConversionCounts,
                "No of Conversion",
                "No of Conversion over time",
                this::loadConversionCountGraph
        );
    }

    public void loadBounceCountGraph(int tickIndex, String groupingGranularity) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                serverLog::fetchBounceCounts,
                "No of Bounce",
                "No of Bounce over time",
                this::loadBounceCountGraph
        );
    }

    public long getMaxCount(HashMap<String,Integer> count){
        long max = 0;
        for (String date: count.keySet()){
            max = Math.max(max, count.get(date));
        }
        return max;
    }

    public long getMinCount(HashMap<String,Integer> count){
        long min = Integer.MAX_VALUE;
        for (String date: count.keySet()){
            min = Math.min(min, count.get(date));
        }
        return min;
    }

    /**
     * Function to add chart hovering pane
     * @param coordX x-coordinate of the hovering pane
     * @param coordY y-coordinate of the hovering pane
     * @param cat category of the data
     * @param key key of the data
     * @param value value of the data
     */

    private void addChartHoverPane(double coordX, double coordY, String cat, String key, Integer value){
        hoverPane.setLayoutX(coordX+200);
        hoverPane.setLayoutY(coordY+200);
        hoverPane.opacityProperty().setValue(1);
        timeOrCatLabel.setText(cat);
        timeLabel.setText(key);
        valueLabel.setText(String.valueOf(value));
    }


    /**
     * Include anything that needs to be done EACH time the scene is opened
     */
    @Override
    public void refreshScene() {
        hoverPane.opacityProperty().setValue(0);
    }
}