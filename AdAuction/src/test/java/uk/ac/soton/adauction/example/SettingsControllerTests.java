package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.SettingsController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsControllerTests {

    private SettingsController controller;
    private static final long TIMEOUT = 5;

    @BeforeAll
    public static void setupSpec() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    private File createTempCSV(String name) throws IOException {
        File tempFile = File.createTempFile(name, ".csv");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), "id,value\n1,abc".getBytes());  // dummy CSV content
        return tempFile;
    }


    @BeforeEach
    public void setup() throws Exception {
        controller = new SettingsController();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setField("bounceEntry1", new TextField());
                setField("bounceEntry2", new TextField());
                setField("bounceMessage", new Label());
                setField("timeSpent", new CheckBox("Time Spent"));
                setField("numberOfPages", new CheckBox("Number of Pages"));
                setField("currentDefinition", new Label());

                setField("influencerModeRadio", new RadioButton("Influencer mode"));
                setField("entrepreneurModeRadio", new RadioButton("Entrepreneur mode"));
                setField("analystModeRadio", new RadioButton("Analyst mode"));
                setField("customizedModeRadio", new RadioButton("Customized mode"));

                // These are needed for toggle and component selection logic
                setField("noOfImpression", new CheckBox());
                setField("noOfClicks", new CheckBox());
                setField("noOfConversion", new CheckBox());
                setField("noOfUniques", new CheckBox());
                setField("noOfBounces", new CheckBox());
                setField("CPM", new CheckBox());
                setField("CPA", new CheckBox());
                setField("CTR", new CheckBox());
                setField("CPC", new CheckBox());
                setField("bounceRate", new CheckBox());
                setField("totalCost", new CheckBox());

                invokeMethod("initialize");
            } catch (Exception e) {
                e.printStackTrace();
            }
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @AfterEach
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    public void testNumberOfPagesCheck_SetsCorrectDefinition() throws Exception {
        Platform.runLater(() -> {
            ((CheckBox) getField("numberOfPages")).setSelected(false);
            ((CheckBox) getField("timeSpent")).setSelected(true);
            invokeSafe("numberOfPagesCheck", new ActionEvent());

            assertTrue(((CheckBox) getField("numberOfPages")).isSelected());
            assertFalse(((CheckBox) getField("timeSpent")).isSelected());
            assertTrue(((TextField) getField("bounceEntry1")).isVisible());
            assertFalse(((TextField) getField("bounceEntry2")).isVisible());
        });
        Thread.sleep(500);
    }

    @Test
    public void testTimeSpentCheck_SetsCorrectDefinition() throws Exception {
        Platform.runLater(() -> {
            ((CheckBox) getField("timeSpent")).setSelected(false);
            ((CheckBox) getField("numberOfPages")).setSelected(true);
            invokeSafe("timeSpentCheck", new ActionEvent());

            assertTrue(((CheckBox) getField("timeSpent")).isSelected());
            assertFalse(((CheckBox) getField("numberOfPages")).isSelected());
            assertTrue(((TextField) getField("bounceEntry2")).isVisible());
            assertFalse(((TextField) getField("bounceEntry1")).isVisible());
        });
        Thread.sleep(500);
    }

    @Test
    public void testApplyBounceDefinition_Invalid_NoSelection() throws Exception {
        Platform.runLater(() -> {
            invokeSafe("applyBounceDefinition", new ActionEvent());
            Label msg = (Label) getField("bounceMessage");
            assertEquals("Error! Please select an option above!", msg.getText());
            assertTrue(msg.isVisible());
        });
        Thread.sleep(500);
    }

    @Test
    public void testApplyBounceDefinition_InvalidInput() throws Exception {
        Platform.runLater(() -> {
            ((CheckBox) getField("timeSpent")).setSelected(true);
            ((TextField) getField("bounceEntry2")).setText("abc");
            invokeSafe("applyBounceDefinition", new ActionEvent());

            Label msg = (Label) getField("bounceMessage");
            assertEquals("Error! Please enter a valid integer!", msg.getText());
        });
        Thread.sleep(500);
    }

    @Test
    public void testApplyBounceDefinition_Success() throws Exception {
        Platform.runLater(() -> {
            ((CheckBox) getField("timeSpent")).setSelected(true);
            ((TextField) getField("bounceEntry2")).setText("5");
            invokeSafe("applyBounceDefinition", new ActionEvent());

            Label msg = (Label) getField("bounceMessage");
            assertEquals("Success", msg.getText());
        });
        Thread.sleep(500);
    }

    @Test
    public void testSetModeToggleGroup_Customized_EnablesCheckboxes() throws Exception {
        Platform.runLater(() -> {
            ((RadioButton) getField("customizedModeRadio")).setSelected(true);
            invokeSafe("setModeToggleGroup");

            CheckBox cb = (CheckBox) getField("noOfImpression");
            assertFalse(cb.isDisabled());
        });
        Thread.sleep(500);
    }

    @Test
    public void testSetModeToggleGroup_Analyst_DisablesCheckboxes() throws Exception {
        Platform.runLater(() -> {
            ((RadioButton) getField("analystModeRadio")).setSelected(true);
            invokeSafe("setModeToggleGroup");

            CheckBox cb = (CheckBox) getField("noOfImpression");
            assertTrue(cb.isDisabled());
        });
        Thread.sleep(500);
    }

    @Test
    public void testCleanScene_ClearsBounceFields() throws Exception {
        Platform.runLater(() -> {
            ((TextField) getField("bounceEntry1")).setText("100");
            ((TextField) getField("bounceEntry2")).setText("200");
            invokeSafe("cleanScene");

            assertTrue(((TextField) getField("bounceEntry1")).getText().isEmpty());
            assertTrue(((TextField) getField("bounceEntry2")).getText().isEmpty());
        });
        Thread.sleep(500);
    }

    // Utility methods
    private void setField(String name, Object value) {
        try {
            Field field = SettingsController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + name, e);
        }
    }

    private Object getField(String name) {
        try {
            Field field = SettingsController.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + name, e);
        }
    }

    private void invokeSafe(String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
            }
            var method = SettingsController.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            method.invoke(controller, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private void invokeMethod(String name) throws Exception {
        var method = SettingsController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(controller);
    }

    @Test
    public void testSetErrorMessage_setsTextAndVisible() throws Exception {
        Platform.runLater(() -> {
            invokeSafe("setErrorMessage", "Test error");
            Label msg = (Label) getField("importMessage");
            assertEquals("Test error", msg.getText());
            assertTrue(msg.isVisible());
        });
        Thread.sleep(300);
    }

    @Test
    public void testUploadFiles_allFilesPresent_noErrorMessage() throws Exception {
        File click = createTempCSV("click");
        File impression = createTempCSV("impression");
        File server = createTempCSV("server");

        Platform.runLater(() -> {
            setField("file1", click);
            ((Label) getField("clickLabel")).setText(click.getName());

            setField("file2", impression);
            ((Label) getField("impressionLabel")).setText(impression.getName());

            setField("file3", server);
            ((Label) getField("serverLabel")).setText(server.getName());

            invokeSafe("uploadFiles");

            Label importMessage = (Label) getField("importMessage");
            // It may still be visible briefly but shouldn't contain error
            assertNotEquals("Click Log Missing", importMessage.getText());
            assertNotEquals("Impression Log Missing", importMessage.getText());
            assertNotEquals("Server Log Missing", importMessage.getText());
        });
        Thread.sleep(500);
    }



}
