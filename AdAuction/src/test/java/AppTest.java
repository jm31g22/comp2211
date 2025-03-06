import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import uk.ac.soton.adauction.example.App;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest extends ApplicationTest {
    Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        new App().start(stage);
        primaryStage = stage;
    }

    @Test
    void sceneNotNull() {
        assertNotNull(primaryStage.getScene());

        assertNotNull(primaryStage.getScene().getRoot());
    }
    @Test
    void testAppOpensDashboard() throws IOException {
        // Load the expected root from the 'dashboard.fxml' manually
        FXMLLoader loader = new FXMLLoader(getClass().getResource("uk.ac.soton.adauction.example/Controller/dashboard.fxml"));
        Parent expectedRoot = loader.load();

        // Assert that the root of the scene matches the expected root
        assertEquals(expectedRoot.getClass(), primaryStage.getScene().getRoot().getClass(), "The scene should be loaded from 'dashboard.fxml'");
    }
}

