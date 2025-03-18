package uk.ac.soton.adauction.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppState {

    private static final StringProperty bounceDefinition = new SimpleStringProperty("Pages");
    private static final IntegerProperty bounceDefinitionNumber = new SimpleIntegerProperty(5);


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

}
