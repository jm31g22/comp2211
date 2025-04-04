package uk.ac.soton.adauction.example.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import uk.ac.soton.adauction.example.Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AdminController extends SceneController{
    @FXML private TableView<Map<String, String>> usersTable;
    @FXML private TableColumn<Map<String, String>, String> usernameColumn;
    @FXML private TableColumn<Map<String, String>, String> passwordColumn;
    @FXML private TableColumn<Map<String, String>, String> emailColumn;
    @FXML private TableColumn<Map<String, String>, String> roleColumn;

    @FXML private TextField newUsername;
    @FXML private TextField newPassword;
    @FXML private TextField newEmail;
    @FXML private ComboBox<String> roleComboBox;

    private final Connection conn = DatabaseConnection.getRemoteUsersConnection();
    private final ObservableList<Map<String, String>> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        super.initialize();
        setupTable();
        loadUsers();
        setupRoleComboBox();
        setupPasswordTooltip();
        setupEmailTooltip();
    }

    /*
     * Initialize List Control
     */
    private void setupTable() {
        usersTable.setEditable(true);

        // username
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("username")));
        usernameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        usernameColumn.setOnEditCommit(event -> {
            // Map<String, String> user = event.getRowValue();
            // updateUserField(user.get("username"), "username", event.getNewValue());
            // user.put("username", event.getNewValue());
            String newUsername = event.getNewValue();
            Map<String, String> user = event.getRowValue();
            String originalUsername = user.get("username");
            if (newUsername.equals(originalUsername)) return;
            if (checkUserExists(newUsername)) {
                showErrorAlert("Input Error", "Username already exists");
                event.getTableView().refresh();
                return;
            }
            updateUserField(originalUsername, "username", newUsername);
        });

        // password
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("password")));
        passwordColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        passwordColumn.setOnEditCommit(event -> {
            // Map<String, String> user = event.getRowValue();
            // updateUserField(user.get("username"), "password", event.getNewValue());
            // user.put("password", event.getNewValue());
            String newPassword = event.getNewValue();
            Map<String, String> user = event.getRowValue();
            List<String> errors = validatePassword(newPassword);
            if (!errors.isEmpty()) {
                showPasswordErrorAlert(errors);
                event.getTableView().refresh();
                return;
            }
            updateUserField(user.get("username"), "password", newPassword);
        });

        // email
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("email")));
        emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        emailColumn.setOnEditCommit(event -> {
            // Map<String, String> user = event.getRowValue();
            // updateUserField(user.get("username"), "email", event.getNewValue());
            // user.put("email", event.getNewValue());
            String newEmail = event.getNewValue();
            Map<String, String> user = event.getRowValue();
            List<String> errors = validateEmail(newEmail);
            if (!errors.isEmpty()) {
                showEmailErrorAlert(errors);
                event.getTableView().refresh();
                return;
            }
            updateUserField(user.get("username"), "email", newEmail);
        });

        // role
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("role")));
        roleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        roleColumn.setOnEditCommit(event -> {
            Map<String, String> user = event.getRowValue();
            updateUserField(user.get("username"), "role", event.getNewValue());
            user.put("role", event.getNewValue());
        });

        // Actions - Delete
        TableColumn<Map<String, String>, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    Map<String, String> user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user.get("username"));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        usersTable.getColumns().add(actionsColumn);
    }

    /*
     * Role ComboxBox
     */
    private void setupRoleComboBox() {
        roleComboBox.getItems().addAll("viewer", "editor", "admin");
        roleComboBox.getSelectionModel().selectFirst();
    }

    /*
     * Password Tooltip
     */
    private void setupPasswordTooltip() {
        Tooltip tooltip = new Tooltip("Password must contain:\n" +
                "- A mix of lowercase and uppercase letters\n" +
                "- At least one number\n" +
                "- At least one special character (@$!%*?&)\n" +
                "- Minimum 8 characters");
        tooltip.setStyle("-fx-font-size: 13px; -fx-text-fill: #fff;");

        newPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Window window = newPassword.getScene().getWindow();
                Point2D point = newPassword.localToScreen(
                        newPassword.getWidth() / 2 + 5,  // X
                        newPassword.getHeight() / 2 + 20    // Y
                );
                tooltip.show(window, point.getX(), point.getY());
            } else {
                tooltip.hide();
            }
        });

        // newPassword.setTooltip(tooltip);
    }

    /*
     * Email Tooltip
     */
    private void setupEmailTooltip() {
        Tooltip tooltip = new Tooltip("Valid email requirements:\n" +
                "- Must contain @ symbol\n" +
                "- Proper domain format (e.g. example.com)\n" +
                "- Maximum length 254 characters");
        tooltip.setStyle("-fx-font-size: 13px; -fx-text-fill: #fff;");

        newEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Window window = newEmail.getScene().getWindow();
                Point2D point = newEmail.localToScreen(
                        newEmail.getWidth() / 2 + 5,    // X
                        newEmail.getHeight() / 2 + 20      // Y
                );
                tooltip.show(window, point.getX(), point.getY());
            } else {
                tooltip.hide();
            }
        });

        //newEmail.setTooltip(tooltip);
    }

    /*
     * Retrieve all user data from the database users table to the list
     */
    private void loadUsers() {
        users.clear();
        String query = "SELECT * FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("username", rs.getString("username"));
                user.put("password", rs.getString("password"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                users.add(user);
            }
            usersTable.setItems(users);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load users: " + e.getMessage());
        }
    }

    /*
     * Add a user to the users data table
     */
    @FXML
    private void handleAddUser() {
        String username = newUsername.getText();
        String password = newPassword.getText();
        String email = newEmail.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty()) {
            showErrorAlert("Input Error", "Username must be entered");
            return;
        }

        if (checkUserExists(username)) {
            showErrorAlert("Input Error", "Username already exists");
            return;
        }

        if (password.isEmpty()) {
            showErrorAlert("Input Error", "Password required");
            return;
        }

        List<String> passwordErrors = validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            showPasswordErrorAlert(passwordErrors);
            return;
        }

        if (email.isEmpty()) {
            showErrorAlert("Input Error", "Email must be entered");
            return;
        }

        List<String> emailErrors = validateEmail(email);
        if (!emailErrors.isEmpty()) {
            showEmailErrorAlert(emailErrors);
            return;
        }

        if (role == null) {
            showErrorAlert("Input Error", "User role must be entered");
            return;
        }

        String query = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, role);
            stmt.executeUpdate();

            logAction("CREATE", username);
            loadUsers();
            clearInputFields();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add user: " + e.getMessage());
        }
    }

    // Check if the username exists
    private boolean checkUserExists(String username) {
        String query = "SELECT username FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error checking username: " + e.getMessage());
            return true;
        }
    }

    // Verify password
    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return errors;
        }

        if (password.length() < 8) {
            errors.add("Minimum 8 characters");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.add("At least one lowercase letter");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.add("At least one uppercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            errors.add("At least one number");
        }
        if (!password.matches(".*[@$!%*?&].*")) {
            errors.add("At least one special character (@$!%*?&)");
        }

        return errors;
    }

    // Verify email
    private List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add("Email cannot be empty");
            return errors;
        }

        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        if (!pattern.matcher(email).matches()) {
            errors.add("Invalid email format");
        } else if (!email.contains(".")) {
            errors.add("Missing domain extension (e.g .com/.org)");
        } else if (email.length() > 254) {
            errors.add("Email too long (max 254 chars)");
        }

        return errors;
    }

    /*
     * Delete a specified user
     */
    private void handleDeleteUser(String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the user: " + username + "?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                String query = "DELETE FROM users WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.executeUpdate();

                    logAction("DELETE", username);
                    loadUsers();
                } catch (SQLException e) {
                    showErrorAlert("Database Error", "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }

    /*
     * After the user directly modifies the user field in the list and presses enter,
     * it is used here to submit and update the user to the database
     */
    private void updateUserField(String originalUsername, String field, String newValue) {
        String query = String.format("UPDATE users SET %s = ? WHERE username = ?", field);
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newValue);
            stmt.setString(2, originalUsername);
            stmt.executeUpdate();

            logAction("UPDATE", originalUsername + " - " + field + " changed to " + newValue);
            loadUsers();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update user: " + e.getMessage());
        }
    }

    /*
     * Record the executed operation in the database user_rogs table
     */
    private void logAction(String actionType, String details) {
        String query = "INSERT INTO user_logs (action_type, details, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, actionType);
            stmt.setString(2, details);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Clear the input fields for adding users
     */
    private void clearInputFields() {
        newUsername.clear();
        newPassword.clear();
        newEmail.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    /*
     * Pop up error prompt dialog box
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showPasswordErrorAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Password Requirements Not Met");
        alert.setHeaderText("Password does not meet the following requirements:");

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        for (String error : errors) {
            Label label = new Label("• " + error);
            label.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");
            content.getChildren().add(label);
        }

        Label help = new Label("Please correct the password and try again.");
        help.setStyle("-fx-text-fill: #616161; -fx-font-size: 12px;");
        content.getChildren().add(help);

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void showEmailErrorAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Email Address");
        alert.setHeaderText("Email validation failed:");

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        for (String error : errors) {
            Label label = new Label("• " + error);
            label.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");
            content.getChildren().add(label);
        }

        Label example = new Label("Example valid format:\nuser@example.com");
        example.setStyle("-fx-text-fill: #616161; -fx-font-size: 12px;");
        content.getChildren().add(example);

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @Override
    public void refreshScene() {
    }

    @FXML
    private void handleShowLogs() {
        String query = "SELECT * FROM user_logs ORDER BY timestamp DESC";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n===================== User Logs =====================");
            while (rs.next()) {
                System.out.printf("[%s] %s - %s%n",
                        rs.getTimestamp("timestamp"),
                        rs.getString("action_type"),
                        rs.getString("details"));
            }
            System.out.println("=====================================================");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load logs: " + e.getMessage());
        }
    }
}
