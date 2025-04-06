package uk.ac.soton.adauction.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class AppState {

    private static final StringProperty bounceDefinition = new SimpleStringProperty("Pages");
    private static final IntegerProperty bounceDefinitionNumber = new SimpleIntegerProperty(5);
    private static int dashboardMode = 2;
    private static ArrayList<Integer> components = new ArrayList<>();


    public static void setBounceDefinition(String newDefinition) {
        bounceDefinition.set(newDefinition);
    }
    public static void setBounceDefinitionNumber(int i) {
        bounceDefinitionNumber.set(i);
    }

    public static StringProperty getBounceDefinitionBinding() {
        return bounceDefinition;
    }
    public static IntegerProperty getBounceDefinitionNumberBinding() {
        return bounceDefinitionNumber;
    }

    public static void setDashboardMode(int i){
        dashboardMode = i;
        System.out.println("Current mode is: " + i);
    }
    public static int getDashboardMode(){return dashboardMode;}
    public static void addComponents(int i){
        components.add(i);
        System.out.println("Added to components: " + i);
    }
    public static void removeComponents(int i){
        if (components.contains(i)){
            components.remove(components.indexOf(i));
            System.out.println("Remove from components: " + i);
        }
    }
    public static ArrayList<Integer> getComponents(){return components;}


}
