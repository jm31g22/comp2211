package uk.ac.soton.adauction.example.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import uk.ac.soton.adauction.example.FetchData.ClickLog;
import uk.ac.soton.adauction.example.FetchData.ImpressionLog;
import uk.ac.soton.adauction.example.FetchData.ServerLog;
import uk.ac.soton.adauction.example.Utils.GraphFilters;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class ComparisonController extends SceneController {
    private int offset1 = 0;
    private int offset2 = 0;
    @FXML
    AnchorPane graph1Pane;
    @FXML
    private ChoiceBox<String> chartSelection1 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> metricSelection1 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> timeSelection1 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> granSelection1 = new ChoiceBox<>();
    @FXML
    private Button panLeftButton1 = new Button();
    @FXML
    private Button panRightButton1 = new Button();
    @FXML
    private PieChart piechart1;
    @FXML
    AnchorPane graph2Pane;
    @FXML
    private ChoiceBox<String> chartSelection2 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> metricSelection2 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> timeSelection2 = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> granSelection2 = new ChoiceBox<>();
    @FXML
    private Button panLeftButton2 = new Button();
    @FXML
    private Button panRightButton2 = new Button();
    @FXML
    private PieChart piechart2;
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
    private GraphFilters graph1Filters;
    private GraphFilters graph2Filters;



    public ComparisonController() {
        impressionLog = new ImpressionLog();
        clickLog = new ClickLog();
        serverLog = new ServerLog();
    }

    /**
     * Initialise scene - assume pie chart is shown after entering the scene
     */
    public void initialize() {
        super.initialize();
        granSelection1.opacityProperty().setValue(1);
        panLeftButton1.setVisible(false);
        panRightButton1.setVisible(false);
        graph1Pane.getChildren().clear();
        hoverPane.opacityProperty().setValue(0);
        //load chart options into list
        chartSelection1.getItems().setAll(
                "Metrics by time",
                "Impression Chart",
                "Histogram of click costs"
        );
        chartSelection1.setOnAction((event) -> {
            int selectedIndex = chartSelection1.getSelectionModel().getSelectedIndex();
            Object selectedItem = chartSelection1.getSelectionModel().getSelectedItem();
            hoverPane.opacityProperty().setValue(0);
            if (selectedIndex == 0) {
                currentPage = 0;
                loadLineChartPage(metricSelection1,timeSelection1,granSelection1,graph1Pane);
                panLeftButton1.setVisible(true);
                panRightButton1.setVisible(true);
                metricSelection1.opacityProperty().setValue(1);
                timeSelection1.opacityProperty().setValue(1);
                granSelection1.opacityProperty().setValue(1);
            } else if (selectedIndex == 1) {
                currentPage = 1;
                loadPieChartPage(metricSelection1, timeSelection1, piechart1, graph1Pane);
                panLeftButton1.setVisible(false);
                panRightButton1.setVisible(false);
                metricSelection1.opacityProperty().setValue(1);
                granSelection1.opacityProperty().setValue(0);
                timeSelection1.opacityProperty().setValue(0);
            } else if (selectedIndex == 2) {
                currentPage = 2;
                loadHistogramPage(graph1Pane);
                panLeftButton1.setVisible(false);
                panRightButton1.setVisible(false);
                granSelection1.opacityProperty().setValue(0);
                timeSelection1.opacityProperty().setValue(0);
                metricSelection1.opacityProperty().setValue(0);
            }
            System.out.println("Selection made for graph 1: [" + selectedIndex + "] " + selectedItem);
        });
        granSelection2.opacityProperty().setValue(1);
        panLeftButton2.setVisible(false);
        panRightButton2.setVisible(false);
        graph2Pane.getChildren().clear();
        //load chart options into list
        chartSelection2.getItems().setAll(
                "Metrics by time",
                "Impression Chart",
                "Histogram of click costs"
        );
        chartSelection2.setOnAction((event) -> {
            int selectedIndex = chartSelection2.getSelectionModel().getSelectedIndex();
            Object selectedItem = chartSelection2.getSelectionModel().getSelectedItem();
            hoverPane.opacityProperty().setValue(0);
            if (selectedIndex == 0) {
                currentPage = 0;
                loadLineChartPage(metricSelection2, timeSelection2, granSelection2, graph2Pane);
                panLeftButton2.setVisible(true);
                panRightButton2.setVisible(true);
                metricSelection2.opacityProperty().setValue(1);
                timeSelection2.opacityProperty().setValue(1);
                granSelection2.opacityProperty().setValue(1);
            } else if (selectedIndex == 1) {
                currentPage = 1;
                loadPieChartPage(metricSelection2, timeSelection2, piechart2, graph2Pane);
                panLeftButton2.setVisible(false);
                panRightButton2.setVisible(false);
                metricSelection2.opacityProperty().setValue(1);
                granSelection2.opacityProperty().setValue(0);
                timeSelection2.opacityProperty().setValue(0);
            } else if (selectedIndex == 2) {
                currentPage = 2;
                loadHistogramPage(graph2Pane);
                panLeftButton2.setVisible(false);
                panRightButton2.setVisible(false);
                granSelection2.opacityProperty().setValue(0);
                timeSelection2.opacityProperty().setValue(0);
                metricSelection2.opacityProperty().setValue(0);
            }
            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
        });
    }

    /**
     * Function to load the histogram of distributed click cost
     */
    public void loadHistogramPage(AnchorPane pane){
        pane.getChildren().clear();
        ChartViewer viewer = new ChartViewer(createHistogram());
        viewer.setPrefWidth(623.0);
        viewer.setPrefHeight(242.0);
        pane.getChildren().add(viewer);
        viewer.setStyle("-fx-border-width: 0");
    }


    /**
     * Function to modify the pie chart page and set metric selection
     */
    public void loadPieChartPage(ChoiceBox<String> metricSelection, ChoiceBox<String> timeSelection, PieChart impressionPie, AnchorPane pane) {
        metricSelection.getItems().clear();
        pane.getChildren().clear();
        impressionPie.getData().clear();
        pane.getChildren().add(impressionPie);
        impressionPie.setStyle("-fx-border-width: 0");
        //load chart options into list
        metricSelection.getItems().add("Gender");
        metricSelection.getItems().add("Age");
        metricSelection.getItems().add("Income");
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
            hoverPane.opacityProperty().setValue(0);
            if (selectedIndex == 0) {
                loadGenderPieData(impressionPie);
            } else if (selectedIndex == 1) {
                loadAgePieData(impressionPie);
            } else if (selectedIndex == 2) {
                loadIncomePieData(impressionPie);
            }
        });
        timeSelection.hide();
    }

    private void hoverImpressionPane(String cat, PieChart impressionPie){
        impressionPie.getData().stream().forEach(data ->{
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<>() {
                        @Override
                        public void handle(MouseEvent e) {
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
    public void loadGenderPieData(PieChart impressionPie) {
        HashMap<String, Integer> counts = impressionLog.fetchImpressionGenderCount();
        double femaleCount = Math.round((float) counts.get("female") / counts.get("total") * 100);
        double maleCount = Math.round((float) counts.get("male") / counts.get("total") * 100);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Female", femaleCount),
                        new PieChart.Data("Male", maleCount));
        impressionPie.setData(pieChartData);
        hoverImpressionPane("Gender :", impressionPie);
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #a85775;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #5f8df7;");
        impressionPie.setPrefWidth(623.0);
        impressionPie.setPrefHeight(242.0);
        impressionPie.setLegendVisible(false);
    }
    /**
     * Function to load impression group by age pie chart
     */
    public void loadAgePieData(PieChart impressionPie) {
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
        hoverImpressionPane("Age :", impressionPie);
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #5f3f65;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #a85775;");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #e47c6f;");
        pieChartData.get(3).getNode().setStyle("-fx-pie-color: #ffb563;");
        pieChartData.get(4).getNode().setStyle("-fx-pie-color: #f9f871;");
        impressionPie.setPrefWidth(623.0);
        impressionPie.setPrefHeight(242.0);
        impressionPie.setLegendVisible(false);
    }

    /**
     * Function to load impression group by income pie chart
     */
    public void loadIncomePieData(PieChart impressionPie) {
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
        hoverImpressionPane("Income :", impressionPie);
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #1f263e;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #d1eeec;");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #208a86;");
        impressionPie.setPrefWidth(623.0);
        impressionPie.setPrefHeight(242.0);
        impressionPie.setLegendVisible(false);
    }

    /**
     * Load data for the line chart by time and set metric selection
     */
    public void loadLineChartPage(ChoiceBox<String> metricSelection, ChoiceBox<String> timeSelection, ChoiceBox<String> granSelection, AnchorPane pane) {
        metricSelection.getItems().clear();
        pane.getChildren().clear();
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
            addTimeSelection(selectedIndex, timeSelection, granSelection, pane);
        });

    }

    /**
     * Add menu items for timeframe and granularity selection - and attempt to refresh graph upon changes
     * @param graphIndex - which graph
     */
    private void addTimeSelection(int graphIndex, ChoiceBox<String> timeSelection, ChoiceBox<String> granSelection, AnchorPane pane) {
        // clear old choices & selection
        timeSelection.getItems().clear();
        timeSelection.getSelectionModel().clearSelection();

        granSelection.getItems().clear();
        granSelection.getSelectionModel().clearSelection();

        // add menu items
        timeSelection.getItems().addAll("By Hour", "By Day", "By Week", "By Month");
        granSelection.getItems().addAll("Hourly", "Daily", "Weekly", "Monthly");

        // attempt graph load on selection change
        if (pane == graph1Pane){
            timeSelection.setOnAction(e -> attemptGraphLoad(graphIndex, timeSelection, granSelection, pane, panLeftButton1, panRightButton1));
            granSelection.setOnAction(e -> attemptGraphLoad(graphIndex, timeSelection, granSelection, pane, panLeftButton1, panRightButton1));
        }else{
            timeSelection.setOnAction(e -> attemptGraphLoad(graphIndex, timeSelection, granSelection, pane, panLeftButton2, panRightButton2));
            granSelection.setOnAction(e -> attemptGraphLoad(graphIndex, timeSelection, granSelection, pane, panLeftButton2, panRightButton2));
        }

    }

    /**
     * Attempt to load graph over time
     * @param graphIndex
     */
    private void attemptGraphLoad(int graphIndex, ChoiceBox<String> timeSelection, ChoiceBox<String> granSelection, AnchorPane pane, Button panLeftButton, Button panRightButton) {
        int timeIndex = timeSelection.getSelectionModel().getSelectedIndex();  // 0..3 or -1
        String granChoice = granSelection.getSelectionModel().getSelectedItem(); // or null

        if (timeIndex < 0 || granChoice == null) {
            // invalid choices
            return;
        }

        switch (graphIndex) {
            case 0:
                loadImpressionCountGraph(timeIndex, granChoice, pane, panLeftButton, panRightButton);
                break;
            case 1:
                loadClickCountGraph(timeIndex, granChoice, pane, panLeftButton, panRightButton);
                break;
            case 2:
                loadUniqueCountGraph(timeIndex, granChoice, pane, panLeftButton, panRightButton);
                break;
            case 3:
                loadConversionCountGraph(timeIndex, granChoice, pane, panLeftButton, panRightButton);
                break;
            case 4:
                loadBounceCountGraph(timeIndex, granChoice, pane, panLeftButton, panRightButton);
                break;
            default:
                System.err.println("Unknown metric selection index: " + graphIndex);
        }
    }

    @FunctionalInterface
    public interface Consumer<T,U,V,W,X>{
        void accept(T t, U u, V v, W w, X x);
    }

    @FunctionalInterface
    private interface DataFetcher {
        HashMap<String, Integer> fetch(String groupingGranularity, int tickIndex, int offset);
    }

    /**
     * Generic function to load a count graph over time
     * @param tickIndex -
     * @param groupingGranularity - granularity of data
     * @param fetcher - generic data fetcher interface
     * @param yAxisLabel - name of the y axis
     * @param seriesName - name of the graph
     * @param reloadFunc - function to reload when panning data
     */
    private void loadCountGraph(
            int tickIndex,
            String groupingGranularity,
            ComparisonController.DataFetcher fetcher,                    // get the data
            String yAxisLabel,
            String seriesName,
            Consumer<Integer, String, AnchorPane, Button, Button> reloadFunc,  // reload when panning the data using arrows
            AnchorPane pane,
            Button panLeftButton,
            Button panRightButton
    ) {
        pane.getChildren().clear();

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
        HashMap<String, Integer> count = new HashMap<>();
        if (pane == graph1Pane){
            count = fetcher.fetch(groupingGranularity, tickIndex, offset1);
        }else{
            count = fetcher.fetch(groupingGranularity, tickIndex, offset2);
        }
        // fetch data

        // sort time buckets and add to x-axis
        java.util.List<String> sortedBuckets = new ArrayList<>(count.keySet());
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
        metricsLine.setPrefWidth(623.0);
        metricsLine.setPrefHeight(242.0);
        metricsLine.setHorizontalGridLinesVisible(false);
        metricsLine.setVerticalGridLinesVisible(false);
        metricsLine.setCreateSymbols(false);
        metricsLine.setStyle("-fx-border-width: 0");

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
        pane.getChildren().clear();
        pane.getChildren().add(metricsLine);
        if (pane == graph1Pane){
            // panning button functionality
            panLeftButton.setOnAction(e -> {
                offset1--;
                reloadFunc.accept(tickIndex, groupingGranularity, pane, panLeftButton, panRightButton);
            });

            panRightButton.setOnAction(e -> {
                offset1++;
                reloadFunc.accept(tickIndex, groupingGranularity, pane, panLeftButton, panRightButton);
            });
        }else{
            // panning button functionality
            panLeftButton.setOnAction(e -> {
                offset2--;
                reloadFunc.accept(tickIndex, groupingGranularity, pane, panLeftButton, panRightButton);
            });

            panRightButton.setOnAction(e -> {
                offset2++;
                reloadFunc.accept(tickIndex, groupingGranularity, pane, panLeftButton, panRightButton);
            });
        }


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
        if (pane == graph1Pane){
            System.out.println(seriesName + " loaded. Current offset = " + offset1);
        }else{
            System.out.println(seriesName + " loaded. Current offset = " + offset2);
        }

    }

    /**
     * Load impression count graph
     * @param tickIndex
     * @param groupingGranularity
     */
    public void loadImpressionCountGraph(int tickIndex,
                                         String groupingGranularity,
                                         AnchorPane pane,
                                         Button panLeftButton,
                                         Button panRightButton) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                impressionLog::fetchImpressionCounts,
                "No of Impression",
                "No of Impression over time",
                this::loadImpressionCountGraph,
                pane,
                panLeftButton,
                panRightButton
        );
    }

    /**
     * Load click count graph
     * @param tickIndex
     * @param groupingGranularity
     */
    public void loadClickCountGraph(int tickIndex,
                                    String groupingGranularity,
                                    AnchorPane pane,
                                    Button panLeftButton,
                                    Button panRightButton) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                clickLog::fetchClickCounts,
                "No of Click",
                "No of Click over time",
                this::loadClickCountGraph,
                pane,
                panLeftButton,
                panRightButton
        );
    }

    /**
     * Load unique count graph
     * @param tickIndex
     * @param groupingGranularity
     */
    public void loadUniqueCountGraph(int tickIndex,
                                     String groupingGranularity,
                                     AnchorPane pane,
                                     Button panLeftButton,
                                     Button panRightButton) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                clickLog::fetchUniqueCounts,
                "No of Unique",
                "No of Unique over time",
                this::loadUniqueCountGraph,
                pane,
                panLeftButton,
                panRightButton
        );
    }

    /**
     * Load conversion count graph
     * @param tickIndex
     * @param groupingGranularity
     */
    public void loadConversionCountGraph(int tickIndex,
                                         String groupingGranularity,
                                         AnchorPane pane,
                                         Button panLeftButton,
                                         Button panRightButton) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                serverLog::fetchConversionCounts,
                "No of Conversion",
                "No of Conversion over time",
                this::loadConversionCountGraph,
                pane,
                panLeftButton,
                panRightButton
        );
    }

    /**
     * Load bounce count graph
     * @param tickIndex
     * @param groupingGranularity
     */
    public void loadBounceCountGraph(int tickIndex,
                                     String groupingGranularity,
                                     AnchorPane pane,
                                     Button panLeftButton,
                                     Button panRightButton) {
        loadCountGraph(
                tickIndex,
                groupingGranularity,
                serverLog::fetchBounceCounts,
                "No of Bounce",
                "No of Bounce over time",
                this::loadBounceCountGraph,
                pane,
                panLeftButton,
                panRightButton
        );
    }
    /**
     * Get maximum count from dataset
     * @param count
     * @return maximum
     */
    private long getMaxCount(HashMap<String,Integer> count){
        long max = 0;
        for (String date: count.keySet()){
            max = Math.max(max, count.get(date));
        }
        return max;
    }

    /**
     * Get minimum count from dataset
     * @param count
     * @return minimum
     */
    private long getMinCount(HashMap<String,Integer> count){
        long min = Integer.MAX_VALUE;
        for (String date: count.keySet()){
            min = Math.min(min, count.get(date));
        }
        return min;
    }

    private JFreeChart createHistogram(){
        double[] values = clickLog.getHistogramData();
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("click-cost", values, 10);
        JFreeChart histogram = ChartFactory.createHistogram(
                "Histogram Of The Click Costs",
                "Click Cost",
                "Frequency",
                dataset);
        XYPlot plot = (XYPlot) histogram.getPlot();
        Stroke gridLines = new BasicStroke(0);
        plot.setDomainGridlineStroke(gridLines);
        plot.setRangeGridlineStroke(gridLines);
        plot.setBackgroundPaint(null);
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new java.awt.Color(31, 38, 62));
        histogram.removeLegend();
        histogram.setBackgroundPaint(null);
        return histogram;
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

    @Override
    public void refreshScene() throws SQLException {

    }
}
