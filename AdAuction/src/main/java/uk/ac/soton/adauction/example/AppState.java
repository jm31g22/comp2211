package uk.ac.soton.adauction.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class AppState {

    private final StringProperty bounceDefinition = new SimpleStringProperty("Pages");
    private final IntegerProperty bounceDefinitionNumber = new SimpleIntegerProperty(5);
    private static int dashboardMode = 2;
    private static ArrayList<Integer> components = new ArrayList<>();
    private String role = "admin";


    public void setBounceDefinition(String newDefinition) {
        bounceDefinition.set(newDefinition);
    }
    public void setBounceDefinitionNumber(int i) {
        bounceDefinitionNumber.set(i);
    }

    public StringProperty getBounceDefinitionBinding() {
        return bounceDefinition;
    }
    public IntegerProperty getBounceDefinitionNumberBinding() {
        return bounceDefinitionNumber;
    }

    public void setRole(String newRole) {
        role = newRole;
    }
    public String getRole() {
        return role;
    }
    public AppState() {

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
