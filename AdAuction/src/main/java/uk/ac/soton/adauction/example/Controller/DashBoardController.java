package uk.ac.soton.adauction.example.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import uk.ac.soton.adauction.example.FetchData.ImpressionLog;

import java.util.*;

public class DashBoardController {
    @FXML
    private PieChart impressionPie;
    @FXML
    private LineChart<String, Number> metricsLine;
    @FXML
    private ChoiceBox<String> chartSelection = new ChoiceBox<>();
    ;
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
    //variable to save what is the current page
    private int currentPage;
    private ImpressionLog impressionLog;

    public DashBoardController() {
        impressionLog = new ImpressionLog();
    }

    //assume pie chart is shown after entering the scene
    public void initialize() {
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
            } else if (selectedIndex == 1) {
                currentPage = 1;
                loadPieChartPage();
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
                System.out.println("Age Pie selected");
            } else if (selectedIndex == 2) {
                System.out.println("Income Pie selected");
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
        metricSelection.getItems().add("Number of Conversion"); //need bar chart do later
        metricSelection.getItems().add("CPA");
        metricSelection.getItems().add("Bounce Rate");
        metricSelection.setOnAction(null);
        metricSelection.setOnAction((event) -> {
            int selectedIndex = metricSelection.getSelectionModel().getSelectedIndex();
            if (selectedIndex == 0) {
                System.out.println("Number of Impression selected");
                //default showing by day
                loadImpressionCountGraph(1);
            } else if (selectedIndex == 1) {
                System.out.println("Number of Click selected");
            } else if (selectedIndex == 2) {
                System.out.println("Number of Unique selected");
            } else if (selectedIndex == 3){
                System.out.println("Number of Conversion selected");
            } else if (selectedIndex == 4){
                System.out.println("CPA selected");
            } else if (selectedIndex == 5){
                System.out.println("Bounce Rate");
            }
        });
        timeSelection.getItems().add("By Hour");
        timeSelection.getItems().add("By Day");
        timeSelection.getItems().add("By Week");
        timeSelection.getItems().add("By Month");
        timeSelection.setValue("By Day");
        timeSelection.setOnAction(null);
        timeSelection.setOnAction((event) -> {
            int selectedIndex = timeSelection.getSelectionModel().getSelectedIndex();
            loadImpressionCountGraph(selectedIndex);
        });
    }

    public void loadImpressionCountGraph(int index){
        stackPaneGraph.getChildren().clear();
        HashMap<String,Integer> count = new HashMap<>();
        if (index == 0){
            System.out.println("Impression Count By Hour selected");
            count = impressionLog.fetchImpressionHourCount();
        }else if (index == 1){
            System.out.println("Impression Count By Day selected");
            count = impressionLog.fetchImpressionDateCount();
        }else if (index == 2){
            System.out.println("Impression Count By Week selected");
        }else if (index == 3){
            System.out.println("Impression Count By Month selected");
        }
        //Defining X axis
        xAxis = new CategoryAxis();
        LinkedHashSet<String> dates = new LinkedHashSet<>(count.keySet());
        ObservableList<String> observableList = FXCollections.observableArrayList(dates);
        Collections.sort(observableList);
        xAxis.setCategories(observableList);
        xAxis.setLabel("Date");
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
        for (String date: count.keySet()){
            series.getData().add(new XYChart.Data<String, Number>(date, count.getOrDefault(date, 0)));
        }
        metricsLine.getData().add(series);
        metricsLine.setLegendVisible(false);
        metricsLine.setPrefWidth(570.0);
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




}