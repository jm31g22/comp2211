package uk.ac.soton.adauction.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppState {

    private final StringProperty bounceDefinition = new SimpleStringProperty("Pages");
    private final IntegerProperty bounceDefinitionNumber = new SimpleIntegerProperty(5);
    private String role;


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

}
