package uk.ac.soton.adauction.example.Controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.AppState;

public class SettingsController extends SceneController{
    @FXML
    CheckBox timeSpent;
    @FXML
    Label bounceMessage;
    @FXML
    CheckBox numberOfPages;
    @FXML
    TextField bounceEntry1;
    @FXML
    TextField bounceEntry2;
    @FXML
    Label currentDefinition;

    private String bounceDefinition;

    @FXML
    public void initialize() {
        super.initialize();
        setupBindings();
    }



    @FXML
    private void numberOfPagesCheck(ActionEvent event) {
        bounceDefinition = "Pages";
        if (!numberOfPages.isSelected()) {
            numberOfPages.setSelected(true);
        }
        timeSpent.setSelected(false);
        bounceEntry2.setVisible(false);
        bounceEntry1.setVisible(true);
        bounceEntry2.clear();
    }
    @FXML
    private void timeSpentCheck(ActionEvent event) {
        bounceDefinition = "Time Spent";

        if (!timeSpent.isSelected()) {
            timeSpent.setSelected(true);
        }
        numberOfPages.setSelected(false);
        bounceEntry2.setVisible(true);
        bounceEntry1.setVisible(false);
        bounceEntry1.clear();
    }


    @FXML
    private void applyBounceDefinition(ActionEvent event) {
        // Ensure that at least one checkbox is selected
        if (!timeSpent.isSelected() && !numberOfPages.isSelected()) {
            showBounceMessage("Error! Please select an option above!");
            return;
        }

        // Retrieve and validate the number input
        String input = getNonEmptyText(bounceEntry1, bounceEntry2);
        if (!isInteger(input)) {
            showBounceMessage("Error! Please enter a valid integer!");
            return;
        }

        int number = Integer.parseInt(input);
        if (number <= 0) {
            showBounceMessage("Error! Please enter a valid integer!");
            return;
        }

        // Apply settings if all validations pass
        App.getAppState().setBounceDefinition(bounceDefinition);
        App.getAppState().setBounceDefinitionNumber(number);
        showBounceMessage("Success!");
    }

    // Helper method to show  messages
    private void showBounceMessage(String message) {
        bounceMessage.setText(message);
        bounceMessage.setVisible(true);
    }




    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * Gets the text from nonempty TextField
     * @param tf1 text field 1
     * @param tf2 text field 2
     * @return Non-empty contents of Textfields
     */
    private String getNonEmptyText(TextField tf1, TextField tf2) {
        if (!tf1.getText().trim().isEmpty()) {
            return tf1.getText().trim();
        } else {
            return tf2.getText().trim();
        }
    }


    /**
     * Cleanup of scene before being displayed
     */
    public void cleanScene() {

        bounceMessage.setVisible(false);
        bounceEntry2.setVisible(false);
        bounceEntry2.clear();
        bounceEntry1.setVisible(false);
        bounceEntry1.clear();
        timeSpent.setSelected(false);
        numberOfPages.setSelected(false);
    }

    private void setupBindings() {
        //Binding current definition of a bounce to the variables in SettingsState
        currentDefinition.textProperty().bind(Bindings.createStringBinding(
                () -> App.getAppState().getBounceDefinitionBinding().get() + " (" + App.getAppState().getBounceDefinitionNumberBinding().get() + ")", // Formatting
                App.getAppState().getBounceDefinitionBinding(), App.getAppState().getBounceDefinitionNumberBinding() // Observables
        ));
    }

    @Override
    public void refreshScene() {
        cleanScene();
    }
}
