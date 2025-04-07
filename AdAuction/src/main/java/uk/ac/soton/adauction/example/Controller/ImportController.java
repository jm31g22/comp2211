package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.Utils.CampaignImporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ImportController {
    private File file1, file2, file3;

    @FXML
    private Label clickLabel;

    @FXML
    private Label impressionLabel;

    @FXML
    private Label serverLabel;

   /**
     * Select first CSV
     * @param event
     */
    @FXML
    private void selectClickLog(ActionEvent event) {
        file1 = chooseFile(event);
        clickLabel.setText(file1.getName());
    }

    /**
     * Select second CSV
     * @param event
     */
    @FXML
    private void selectImpressionLog(ActionEvent event) {
        file2 = chooseFile(event);
        impressionLabel.setText(file2.getName());

    }

    /**
     * Select third CSV
     * @param event
     */
    @FXML
    private void selectServerLog(ActionEvent event) {
        file3 = chooseFile(event);
        serverLabel.setText(file3.getName());

    }

    /**
     * FileChooser utility function
     * @param event
     * @return
     */
    private File chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Handle upload button click
     */
    @FXML
    private void uploadFiles() {
        if (file1 == null && file2 == null && file3 == null) {
            System.out.println("No files selected.");
            return;
        }

        System.out.println("Uploading files...");

        // Get the application's working directory
        String directory = System.getProperty("user.dir") + "/AdAuction/src/main/java/uk/ac/soton/adauction/example";
        System.out.println("Directory: " + directory);
        // Save selected files
        saveFile(file1, directory);
        saveFile(file2, directory);
        saveFile(file3, directory);

        directory += "/";
        CampaignImporter.importCampaign(directory + file1.getName(), directory + file2.getName(), directory + file3.getName());
        try {
            App.getSceneManager().getControllerCache().get("dashboard").refreshScene();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Stage stage = (Stage) clickLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Save a file to the working directory
     * @param file
     * @param directory
     */
    private void saveFile(File file, String directory) {
        if (file == null) return;

        File destination = new File(directory, file.getName()); // Save with the same name
        try {
            Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved: " + destination.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save file: " + file.getName());
            e.printStackTrace();
        }
    }
}
