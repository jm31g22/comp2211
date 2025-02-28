package uk.ac.soton.adauction.example.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;

public class DashBoardController {
    @FXML
    private PieChart impressionPie;
    //assume pie chart is shown after entering the scene
    public void initialize(){
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Grapefruit", 13),
                        new PieChart.Data("Oranges", 25),
                        new PieChart.Data("Plums", 10),
                        new PieChart.Data("Pears", 22),
                        new PieChart.Data("Apples", 30));
        impressionPie.setData(pieChartData);
    }
}