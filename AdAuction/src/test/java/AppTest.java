import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import uk.ac.soton.adauction.example.App;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        try {
            new App().start(stage); // Start the JavaFX App
        } catch (Exception e) {
            fail("Application failed to start: " + e.getMessage());
        }
    }

    @Test
    void testAppLaunchesSuccessfully() {
        assertDoesNotThrow(() -> {
            Stage primaryStage = new Stage();
            new App().start(primaryStage);
        });
    }
}
