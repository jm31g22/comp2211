package uk.ac.soton.adauction.example.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.adauction.example.FetchData.ClickLog;
import uk.ac.soton.adauction.example.FetchData.ServerLog;
import uk.ac.soton.adauction.example.FetchData.ImpressionLog;

import java.util.*;

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
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private StackPane stackPaneGraph;
    @FXML
    private VBox hoverPane;
    @FXML
    private Label timeLabel;
    @FXML
    private Label valueLabel;
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
        stackPaneGraph.getChildren().clear();
        //load chart options into list
        chartSelection.getItems().add("Metrics by time");
        chartSelection.getItems().add("Impression Chart");
        chartSelection.getItems().add("Histogram of click costs");
        chartSelection.getItems().add("Bounces vs Clicks");
        chartSelection.setOnAction((event) -> {
            int selectedIndex = chartSelection.getSelectionModel().getSelectedIndex();
            Object selectedItem = chartSelection.getSelectionModel().getSelectedItem();
            if (selectedIndex == 0) {
                currentPage = 0;
                loadLineChartPage();
                timeSelection.opacityProperty().setValue(1);
            } else if (selectedIndex == 1) {
                currentPage = 1;
                loadPieChartPage();
                timeSelection.opacityProperty().setValue(0);
            } else if (selectedIndex == 2) {
                currentPage = 2;

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
        stackPaneGraph.getChildren().add(impressionPie);
        //load chart options into list
        metricSelection.getItems().add("Gender");
        metricSelection.getItems().add("Age");
        metricSelection.getItems().add("Income");
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
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
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #b81370;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #2d58d6;");
        impressionPie.setPrefWidth(770.0);
        impressionPie.setPrefHeight(500.0);
        impressionPie.setLegendVisible(false);
    }

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
        impressionPie.setPrefWidth(770.0);
        impressionPie.setPrefHeight(500.0);
        impressionPie.setLegendVisible(false);
    }

    public void loadIncomePieData() {
        HashMap<String, Integer> counts = impressionLog.fetchImpressionIncomeCount();
        double income1Count = Math.round((float) counts.get("low") / counts.get("total") * 100);
        double income2Count = Math.round((float) counts.get("medium") / counts.get("total") * 100);
        double income3Count = Math.round((float) counts.get("high") / counts.get("total") * 100);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("low", income1Count),
                        new PieChart.Data("medium", income2Count),
                        new PieChart.Data("high", income3Count));
        impressionPie.setData(pieChartData);
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
        metricSelection.getItems().add("Number of Impression");
        metricSelection.getItems().add("Number of Click");
        metricSelection.getItems().add("Number of Unique");
        metricSelection.getItems().add("Number of Conversion");
        metricSelection.getItems().add("Number of Bounces");
        metricSelection.setOnAction(null);
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                System.out.println("Number of Impression selected");
                addTimeSelection(0);
            } else if (selectedIndex == 1) {
                System.out.println("Number of Click selected");
                addTimeSelection(1);
            } else if (selectedIndex == 2) {
                System.out.println("Number of Unique selected");
                addTimeSelection(2);
            } else if (selectedIndex == 3){
                System.out.println("Number of Conversion selected");
                addTimeSelection(3);
            } else if (selectedIndex == 4){
                System.out.println("Number of Bounces selected");
                addTimeSelection(4);
            }
        });

    }

    private void addTimeSelection(int graph) {
        timeSelection.getItems().clear();
        timeSelection.getItems().add("By Hour");
        timeSelection.getItems().add("By Day");
        timeSelection.getItems().add("By Week");
        timeSelection.getItems().add("By Month");
        timeSelection.setOnAction(null);
        timeSelection.setOnAction((event2) -> {
            int selectedTime = timeSelection.getSelectionModel().getSelectedIndex();
            if (graph == 0){
                loadImpressionCountGraph(selectedTime);
            }else if (graph == 1){
                loadClickCountGraph(selectedTime);
            }else if (graph == 2){
                loadUniqueCountGraph(selectedTime);
            }else if (graph == 3){
                loadConversionCountGraph(selectedTime);
            }else if (graph == 4){
                loadBounceCountGraph(selectedTime);
            }
        });
    }


    public void loadImpressionCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        xAxis = new CategoryAxis();
        if (index == 0){
            System.out.println("Impression Count By Hour selected");
            count = impressionLog.fetchImpressionHourCount();
            xAxis.setLabel("Hour");
        }else if (index == 1){
            System.out.println("Impression Count By Day selected");
            count = impressionLog.fetchImpressionDateCount();
            xAxis.setLabel("Date");
        }else if (index == 2){
            System.out.println("Impression Count By Week selected");
            count = impressionLog.fetchImpressionWeekCount();
            xAxis.setLabel("Week");
        }else if (index == 3){
            System.out.println("Impression Count By Month selected");
            count = impressionLog.fetchImpressionMonthCount();
            xAxis.setLabel("Month");
        }
        //Defining X axis
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setAutoRanging(true);
        //Defining Y
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo-minNo)/50;
        yAxis = new NumberAxis(Math.max(minNo-diff,0), maxNo+diff, diff);
        yAxis.setLabel("No of Impression");
        metricsLine = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("No of Impression over time");
        addToLineGraph(count, series);
    }

    public void loadClickCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        xAxis = new CategoryAxis();
        if (index == 0){
            System.out.println("Click Count By Hour selected");
            count = clickLog.fetchClickHourCount();
            xAxis.setLabel("Hour");
        }else if (index == 1){
            System.out.println("Click Count By Day selected");
            count = clickLog.fetchClickDateCount();
            xAxis.setLabel("Date");
        }else if (index == 2){
            System.out.println("Click Count By Week selected");
            count = clickLog.fetchClickWeekCount();
            xAxis.setLabel("Week");
        }else if (index == 3){
            System.out.println("Click Count By Month selected");
            count = clickLog.fetchClickMonthCount();
            xAxis.setLabel("Month");
        }
        //Defining X axis
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setAutoRanging(true);
        //Defining Y
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo-minNo)/50;
        yAxis = new NumberAxis(Math.max(minNo-diff,0), maxNo+diff, diff);
        yAxis.setLabel("No of Click");
        metricsLine = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("No of Click over time");
        addToLineGraph(count, series);
    }

    public void loadUniqueCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        xAxis = new CategoryAxis();
        if (index == 0){
            System.out.println("Unique Count By Hour selected");
            count = clickLog.fetchUniqueHourCount();
            xAxis.setLabel("Hour");
        }else if (index == 1){
            System.out.println("Unique Count By Day selected");
            count = clickLog.fetchUniqueDateCount();
            xAxis.setLabel("Date");
        }else if (index == 2){
            System.out.println("Unique Count By Week selected");
            count = clickLog.fetchUniqueWeekCount();
            xAxis.setLabel("Week");
        }else if (index == 3){
            System.out.println("Unique Count By Month selected");
            count = clickLog.fetchUniqueMonthCount();
            xAxis.setLabel("Month");
        }
        //Defining X axis
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setAutoRanging(true);
        //Defining Y
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo-minNo)/50;
        yAxis = new NumberAxis(Math.max(minNo-diff,0), maxNo+diff, diff);
        yAxis.setLabel("No of Unique");
        metricsLine = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("No of Unique over time");
        addToLineGraph(count, series);
    }

    public void loadConversionCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        xAxis = new CategoryAxis();
        if (index == 0){
            System.out.println("Conversion Count By Hour selected");
            count = serverLog.fetchConversionHourCount();
            xAxis.setLabel("Hour");
        }else if (index == 1){
            System.out.println("Conversion Count By Day selected");
            count = serverLog.fetchConversionDateCount();
            xAxis.setLabel("Date");
        }else if (index == 2){
            System.out.println("Conversion Count By Week selected");
            count = serverLog.fetchConversionWeekCount();
            xAxis.setLabel("Week");
        }else if (index == 3){
            System.out.println("Conversion Count By Month selected");
            count = serverLog.fetchConversionMonthCount();
            xAxis.setLabel("Month");
        }
        //Defining X axis
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setAutoRanging(true);
        //Defining Y
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo-minNo)/50;
        yAxis = new NumberAxis(Math.max(minNo-diff,0), maxNo+diff, diff);
        yAxis.setLabel("No of Conversion");
        metricsLine = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("No of Conversion over time");
        addToLineGraph(count, series);
    }

    public void loadBounceCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        xAxis = new CategoryAxis();
        if (index == 0){
            System.out.println("Bounce Count By Hour selected");
            count = serverLog.fetchBounceHourCount();
            xAxis.setLabel("Hour");
        }else if (index == 1){
            System.out.println("Bounce Count By Day selected");
            count = serverLog.fetchBounceDateCount();
            xAxis.setLabel("Date");
        }else if (index == 2){
            System.out.println("Bounce Count By Week selected");
            count = serverLog.fetchBounceWeekCount();
            xAxis.setLabel("Week");
        }else if (index == 3){
            System.out.println("Bounce Count By Month selected");
            count = serverLog.fetchBounceMonthCount();
            xAxis.setLabel("Month");
        }
        //Defining X axis
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setAutoRanging(true);
        //Defining Y
        long minNo = getMinCount(count);
        long maxNo = getMaxCount(count);
        long diff = (maxNo-minNo)/50;
        yAxis = new NumberAxis(Math.max(minNo-diff,0), maxNo+diff, diff);
        yAxis.setLabel("No of Bounce");
        metricsLine = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("No of Bounce over time");
        addToLineGraph(count, series);
    }


    private void addToLineGraph(HashMap<String, Integer> count, XYChart.Series<String, Number> series) {
        for (String date: count.keySet()){
            XYChart.Data<String, Number> data = new XYChart.Data<>(date, count.getOrDefault(date, 0));
            series.getData().add(data);
        }
        metricsLine.getData().add(series);
        for (XYChart.Data<String, Number> data: series.getData()){
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            double coordX = e.getX();
                            double coordY = e.getY();
                            addChartHoverPane(coordX, coordY, data.getXValue(), (Integer) data.getYValue());
                        }
                    });
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED,
                    new EventHandler<MouseEvent>() {
                        @Override public void handle(MouseEvent e) {
                            hoverPane.opacityProperty().setValue(0);
                        }
                    });
        }
        metricsLine.setLegendVisible(false);
        metricsLine.setPrefWidth(770.0);
        metricsLine.setPrefHeight(500.0);
        metricsLine.setHorizontalGridLinesVisible(false);
        metricsLine.setVerticalGridLinesVisible(false);
        metricsLine.setAnimated(false);
        metricsLine.setCreateSymbols(true);
        Node line = series.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: #6677b2;");
        for (XYChart.Data<String, Number> data: series.getData()){
            Platform.runLater(()->{
                Node symbol = data.getNode().lookup(".chart-line-symbol");
                symbol.setStyle("-fx-background-color:  #1F263E, #FFFFFF");
            });
        }
        stackPaneGraph.getChildren().clear();
        stackPaneGraph.getChildren().add(metricsLine);
        System.out.println("Data Loaded");
        metricsLine.requestLayout();
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

    private void addChartHoverPane(double coordX, double coordY, String date, Integer value){
        hoverPane.setLayoutX(coordX+200);
        hoverPane.setLayoutY(coordY+200);
        hoverPane.opacityProperty().setValue(1);
        timeLabel.setText(date);
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