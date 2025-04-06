package uk.ac.soton.adauction.example.Controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import uk.ac.soton.adauction.example.App;
import net.synedra.validatorfx.Check;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.AppState;

import java.awt.*;
import java.util.ArrayList;

public class SettingsController extends SceneController {
    @FXML
    private CheckBox timeSpent;
    @FXML
    private Label bounceMessage;
    @FXML
    private CheckBox numberOfPages;
    @FXML
    private TextField bounceEntry1;
    @FXML
    private TextField bounceEntry2;
    @FXML
    private Label currentDefinition;
    @FXML
    private RadioButton influencerModeRadio;
    @FXML
    private RadioButton entrepreneurModeRadio;
    @FXML
    private RadioButton analystModeRadio;
    @FXML
    private RadioButton customizedModeRadio;
    @FXML
    private CheckBox noOfImpression;
    @FXML
    private CheckBox noOfClicks;
    @FXML
    private CheckBox noOfConversion;
    @FXML
    private CheckBox noOfUniques;
    @FXML
    private CheckBox noOfBounces;
    @FXML
    private CheckBox CPM;
    @FXML
    private CheckBox CTR;
    @FXML
    private CheckBox totalCost;
    @FXML
    private CheckBox CPC;
    @FXML
    private CheckBox CPA;
    @FXML
    private CheckBox bounceRate;
    private String bounceDefinition;
    private ToggleGroup toggleGroup = new ToggleGroup();



    @FXML
    public void initialize() {
        super.initialize();
        setupBindings();
        influencerModeRadio.setToggleGroup(toggleGroup);
        entrepreneurModeRadio.setToggleGroup(toggleGroup);
        analystModeRadio.setToggleGroup(toggleGroup);
        customizedModeRadio.setToggleGroup(toggleGroup);
        analystModeRadio.setSelected(true);
        CheckBox[] checkboxes = new CheckBox[]{noOfImpression, noOfClicks, noOfConversion, noOfUniques, noOfBounces,
                CPM, CPA, CPM, CTR, CPC, bounceRate, totalCost};
        for (CheckBox cb: checkboxes){
            cb.setDisable(true);
        }
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

    public void setModeToggleGroup() {
        RadioButton selectedMode = (RadioButton) toggleGroup.getSelectedToggle();
        String mode = (selectedMode != null) ? selectedMode.getText() : "";
        switch (mode) {
            case "Influencer mode" -> AppState.setDashboardMode(0);
            case "Entrepreneur mode" -> AppState.setDashboardMode(1);
            case "Customized mode" -> AppState.setDashboardMode(3);
            default -> AppState.setDashboardMode(2);
        }
        CheckBox[] checkboxes = new CheckBox[]{noOfImpression, noOfClicks, noOfConversion, noOfUniques, noOfBounces,
                CPM, CPA, CPM, CTR, CPC, bounceRate, totalCost};
        if (!customizedModeRadio.isSelected()) {
            for (CheckBox cb: checkboxes){
                cb.setDisable(true);
            }
        }else{
            for (CheckBox cb: checkboxes){
                cb.setDisable(false);
            }
        }
    }

    public void addNoOfImpression(){
        if (noOfImpression.isSelected()){
            AppState.addComponents(0);
        }else{
            AppState.removeComponents(0);
        }
    }

    public void addNoOfClicks(){
        if (noOfClicks.isSelected()){
            AppState.addComponents(1);
        }else{
            AppState.removeComponents(1);
        }
    }

    public void addNoOfConversion(){
        if (noOfConversion.isSelected()){
            AppState.addComponents(2);
        }else{
            AppState.removeComponents(2);
        }
    }

    public void addNoOfUniques(){
        if (noOfUniques.isSelected()){
            AppState.addComponents(3);
        }else{
            AppState.removeComponents(3);
        }
    }

    public void addNoOfBounce(){
        if (noOfBounces.isSelected()){
            AppState.addComponents(4);
        }else{
            AppState.removeComponents(4);
        }
    }

    public void addCPM(){
        if (CPM.isSelected()){
            AppState.addComponents(5);
        }else{
            AppState.removeComponents(5);
        }
    }

    public void addCTR(){
        if (CTR.isSelected()){
            AppState.addComponents(6);
        }else{
            AppState.removeComponents(6);
        }
    }

    public void addTotalCost(){
        if (totalCost.isSelected()){
            AppState.addComponents(7);
        }else{
            AppState.removeComponents(7);
        }
    }

    public void addCPC(){
        if (CPC.isSelected()){
            AppState.addComponents(8);
        }else{
            AppState.removeComponents(8);
        }
    }

    public void addCPA(){
        if (CPA.isSelected()){
            AppState.addComponents(9);
        }else{
            AppState.removeComponents(9);
        }
    }

    public void addBounceRate(){
        if (bounceRate.isSelected()){
            AppState.addComponents(10);
        }else{
            AppState.removeComponents(10);
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
                () -> "Current Bounce Definition: " + App.getAppState().getBounceDefinitionBinding().get() + " - " + App.getAppState().getBounceDefinitionNumberBinding().get() , // Formatting
                App.getAppState().getBounceDefinitionBinding(), App.getAppState().getBounceDefinitionNumberBinding() // Observables
        ));
    }

    @Override
    public void refreshScene() {
        cleanScene();
    }
}
