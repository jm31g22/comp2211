package uk.ac.soton.adauction.example.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

public class DashBoardController {
    @FXML
    private PieChart impressionPie;
    @FXML
    private LineChart metricsLine;
    @FXML
    private ChoiceBox<String> chartSelection = new ChoiceBox<>();;
    @FXML
    private ChoiceBox<String> metricSelection = new ChoiceBox<>();
    @FXML
    private ChoiceBox<String> timeSelection;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private StackPane stackPaneGraph;
    //variable to save what is the current page
    private int currentPage;

    //assume pie chart is shown after entering the scene
    public void initialize(){
        //load chart options into list
        chartSelection.getItems().add("Metrics by time");
        chartSelection.getItems().add("Impression Chart");
        chartSelection.getItems().add("Histogram of click costs");
        chartSelection.setOnAction((event) -> {
            int selectedIndex = chartSelection.getSelectionModel().getSelectedIndex();
            Object selectedItem = chartSelection.getSelectionModel().getSelectedItem();
            if (selectedIndex == 0){
                currentPage = 0;
                loadLineChartPage();
            }else if (selectedIndex == 1){
                currentPage = 1;
                loadPieChartPage();
            }else if (selectedIndex == 2){
                currentPage = 2;

            }
            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
            System.out.println("   ChoiceBox.getValue(): " + chartSelection.getValue());
        });
    }

    /**
     * Load data for the pie chart and set metric selection
     */
    public void loadPieChartPage(){
        metricSelection.getItems().removeAll();
        stackPaneGraph.getChildren().clear();
        stackPaneGraph.getChildren().add(impressionPie);
        //load chart options into list
        metricSelection.getItems().add("Gender");
        metricSelection.getItems().add("Age");
        metricSelection.getItems().add("Income");
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Female", 60),
                        new PieChart.Data("Male", 40));
        impressionPie.setData(pieChartData);
    }

    /**
     * Load data for the line chart by time and set metric selection
     */
    public void loadLineChartPage(){
        metricSelection.getItems().removeAll();
        stackPaneGraph.getChildren().clear();
        stackPaneGraph.getChildren().add(metricsLine);
        //load chart options into list
        metricSelection.getItems().add("CPA");
        metricSelection.getItems().add("CTR");
        metricSelection.getItems().add("CPC");
        metricSelection.getItems().add("CPM");
        metricSelection.getItems().add("Bounce Rate");
        ArrayList<String> dateData = new ArrayList<>();
        dateData.add("1/1");
        dateData.add("14/1");
        //Defining X axis
        xAxis = new CategoryAxis();
        xAxis.setCategories(FXCollections.observableArrayList(dateData));
        xAxis.setLabel("Date");
        //Defining Y axis
        yAxis = new NumberAxis(0, 350, 50);
        yAxis.setLabel("CTR");
        metricsLine = new LineChart<>(xAxis,yAxis);
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("CTR by time");
        series.getData().add(new XYChart.Data<String, Integer>("1/1", 15));
        series.getData().add(new XYChart.Data<String, Integer>("3/1", 30));
        series.getData().add(new XYChart.Data<String, Integer>("6/1", 60));
        series.getData().add(new XYChart.Data<String, Integer>("8/1", 120));
        series.getData().add(new XYChart.Data<String, Integer>("10/1", 240));
        series.getData().add(new XYChart.Data<String, Integer>("14/1", 300));
        metricsLine.getData().add(series);
    }


}