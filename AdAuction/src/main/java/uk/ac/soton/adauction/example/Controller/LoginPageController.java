package uk.ac.soton.adauction.example.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPageController {
    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;

    public void loginButtonOnAction(ActionEvent event) throws IOException {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();

        if (username.isBlank() || password.isBlank()) {
            loginMessageLabel.setText("Please enter username and password!");
            return;
        }

        if (!userValidation(username, password)) {
            loginMessageLabel.setText("Invalid Login. Please try again.");
            return;
        }

        String userRole = getUserRole(username, password);
        if (userRole == null) {
            loginMessageLabel.setText("Error retrieving user role.");
            return;
        }

        loginMessageLabel.setText("Login success. user:" + username + " role:" + userRole);
    }

    private boolean userValidation(String username, String password) {
        String query = "SELECT COUNT(1) FROM users WHERE username = ? AND password = ?";
        try (Connection connectDB = DatabaseConnection.getConnection();
            PreparedStatement statement = connectDB.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet queryResult = statement.executeQuery();
            return queryResult.next() && queryResult.getInt(1) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getUserRole(String username, String password) {
        String query = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement statement = connectDB.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getString("role") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showInformationAlert(String userRole) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ad Auction Dashboard - " + userRole + " Page");
        alert.setHeaderText(null);
        alert.setContentText("Welcome!\nThis is the " + userRole.toLowerCase() + " page.");
        alert.showAndWait();
    }



    private static void changeScene(String fxmlFileName, String windowTitle, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(LoginPageController.class.getResource("/uk/ac/soton/adauction/example/loginPage-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(windowTitle);
        stage.show();
    }
}