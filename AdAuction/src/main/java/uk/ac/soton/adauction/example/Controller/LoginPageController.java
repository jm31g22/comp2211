package uk.ac.soton.adauction.example.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import uk.ac.soton.adauction.example.App;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;
import uk.ac.soton.adauction.example.Utils.EmailUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class LoginPageController extends SceneController{
    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private TextField verificationCodeField;
    @FXML
    private Button sendEmailButton;

    private String dynamicVerificationCode;
    private volatile boolean isCountingDown = false;
    private final Connection conn = DatabaseConnection.getRemoteUsersConnection();

    public LoginPageController() throws SQLException {
    }


    @FXML
    public void initialize() {
        dynamicVerificationCode = null;
    }


    public void sendMailButtonOnAction(ActionEvent event) {
        String username = usernameTextField.getText();

        if (username.isBlank()) {
            loginMessageLabel.setText("Please enter username!");
            return;
        }

        String email = fetchUserEmail(username);
        if (email == null || email.isEmpty()) {
            loginMessageLabel.setText("Users whose email does not exist or is invalid!");
            return;
        }

        dynamicVerificationCode = generateVerificationCode();

        sendEmailButton.setDisable(true);
        String originalText = sendEmailButton.getText();

        new Thread(() -> {
            try {
                EmailUtils.Email(email, "comp2211cw course verification login system", "Hello " + username + ", Please use the following verification code to log in to the system: " + dynamicVerificationCode);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showErrorMessage("Send failed: " + e.getMessage());
                });
            }
        }).start();

        new Thread(() -> {
            try {
                isCountingDown = true;
                for (int i = 30; i >= 0 && isCountingDown; i--) {
                    final int remaining = i;
                    Platform.runLater(() -> {
                        sendEmailButton.setText(remaining > 0 ? remaining + " s" : originalText);
                        sendEmailButton.setDisable(remaining > 0);
                    });
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showErrorMessage("Send failed: " + e.getMessage());
                    sendEmailButton.setDisable(false);
                    sendEmailButton.setText(originalText);
                });
            } finally {
                isCountingDown = false;
            }
        }).start();
    }



    public void loginButtonOnAction() {
        String username = usernameTextField.getText();
        String password = passwordPasswordField.getText();
        String code = verificationCodeField.getText();

        if (username.isBlank() || password.isBlank()) {
            showErrorMessage("Please enter username and password!");
            return;
        }

        if (code.isBlank()) {
            showErrorMessage("Please enter email verification code!");
            return;
        }

        if (dynamicVerificationCode == null) {
            showErrorMessage("You did not send the verification code!");
            return;
        }

        if (!code.equals(dynamicVerificationCode)) {
            showErrorMessage("Login failed. Incorrect email verification code!");
            return;
        }
        dynamicVerificationCode = null;

        if (!userValidation(username, password)) {
            showErrorMessage("Invalid Login. Please try again.");
            return;
        }

        String userRole = getUserRole(username, password);
        if (userRole == null) {
            showErrorMessage("Error retrieving user role.");
            return;
        }

        App.getSceneManager().switchTo("dashboard");

    }

    private void showErrorMessage(String error) {
        loginMessageLabel.setText(error);
        loginMessageLabel.setVisible(true);
    }

    private String fetchUserEmail(String username) {
        String email = null;
        String query = "SELECT * FROM users WHERE username = ?";
        try (
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet queryResult = statement.executeQuery();
            if (queryResult.next()) {
                email = queryResult.getString("email");
            }
            return email;
        } catch (Exception e) {
            e.printStackTrace();
            return email;
        }
    }

    private boolean userValidation(String username, String password) {
        String query = "SELECT COUNT(1) FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
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
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getString("role") : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    private void showInformationAlert(String userRole) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Ad Auction Dashboard - " + userRole + " Page");
//        alert.setHeaderText(null);
//        alert.setContentText("Welcome!\nThis is the " + userRole.toLowerCase() + " page.");
//        alert.showAndWait();
//    }



    private static void changeScene(String fxmlFileName, String windowTitle, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(LoginPageController.class.getResource("/uk/ac/soton/adauction/example/Controller/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(windowTitle);
        stage.show();
    }

    public static String generateVerificationCode() {
        Random random = new Random();
        StringBuilder verificationCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            verificationCode.append(random.nextInt(10));
        }
        return verificationCode.toString();
    }
    @Override
    public void refreshScene() {
        loginMessageLabel.setVisible(false);
        usernameTextField.clear();
        passwordPasswordField.clear();
        verificationCodeField.clear();
        usernameTextField.requestFocus();
    }
}