package uk.ac.soton.adauction.example;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Ensures a single instance for all tests
public class BootupTests extends ApplicationTest {
    private Stage primaryStage;


    @Override
    public void start(Stage stage) throws Exception {
        new App().start(stage);
        primaryStage = stage;
    }

    @Test
    void sceneNotNull() {
        assertNotNull(primaryStage.getScene(), "Scene should not be null");
        assertNotNull(primaryStage.getScene().getRoot(), "Scene root should not be null");
    }

    @Test
    void testAppOpensDashboard() throws IOException {
        // Load the expected root from 'login.fxml'
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uk/ac/soton/adauction/example/Controller/login.fxml"));
        Parent expectedRoot = loader.load();

        // Get the actual root from the primary stage
        Parent actualRoot = primaryStage.getScene().getRoot();

        // Compare classes instead of instances (avoids memory reference mismatch)
        assertEquals(expectedRoot.getClass(), actualRoot.getClass(), "The scene should be loaded from 'login.fxml'");
    }
}
