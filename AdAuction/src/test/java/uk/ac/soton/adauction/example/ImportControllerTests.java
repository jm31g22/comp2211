package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.ImportController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ImportControllerTests {

    private ImportController controller;
    private static final long TIMEOUT = 5;

    @BeforeAll
    public static void setupSpec() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    public void setup() throws Exception {
        controller = new ImportController();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setField("clickLabel", new Label());
                setField("impressionLabel", new Label());
                setField("serverLabel", new Label());
                setField("importMessage", new Label());
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

    // ========== TEST CASES ==========

    @Test
    public void testUploadFiles_noFilesSelected() throws Exception {
        Platform.runLater(() -> {
            invokeSafe("uploadFiles");
            Label importMessage = (Label) getField("importMessage");
            assertEquals("No files selected", importMessage.getText());
            assertTrue(importMessage.isVisible());
        });
        Thread.sleep(500);
    }

    @Test
    public void testUploadFiles_clickLogMissing() throws Exception {
        File impression = createTempCSV("impression");
        File server = createTempCSV("server");

        Platform.runLater(() -> {
            setField("file2", impression);
            ((Label) getField("impressionLabel")).setText(impression.getName());

            setField("file3", server);
            ((Label) getField("serverLabel")).setText(server.getName());

            invokeSafe("uploadFiles");

            Label importMessage = (Label) getField("importMessage");
            assertEquals("Click Log Missing", importMessage.getText());
        });
        Thread.sleep(500);
    }

    @Test
    public void testUploadFiles_impressionLogMissing() throws Exception {
        File click = createTempCSV("click");
        File server = createTempCSV("server");

        Platform.runLater(() -> {
            setField("file1", click);
            ((Label) getField("clickLabel")).setText(click.getName());

            setField("file3", server);
            ((Label) getField("serverLabel")).setText(server.getName());

            invokeSafe("uploadFiles");

            Label importMessage = (Label) getField("importMessage");
            assertEquals("Impression Log Missing", importMessage.getText());
        });
        Thread.sleep(500);
    }

    @Test
    public void testUploadFiles_serverLogMissing() throws Exception {
        File click = createTempCSV("click");
        File impression = createTempCSV("impression");

        Platform.runLater(() -> {
            setField("file1", click);
            ((Label) getField("clickLabel")).setText(click.getName());

            setField("file2", impression);
            ((Label) getField("impressionLabel")).setText(impression.getName());

            invokeSafe("uploadFiles");

            Label importMessage = (Label) getField("importMessage");
            assertEquals("Server Log Missing", importMessage.getText());
        });
        Thread.sleep(500);
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
            assertNotEquals("Click Log Missing", importMessage.getText());
            assertNotEquals("Impression Log Missing", importMessage.getText());
            assertNotEquals("Server Log Missing", importMessage.getText());
        });
        Thread.sleep(500);
    }

    @Test
    public void testSaveFile_copiesToDestination() throws IOException {
        File tempFile = createTempCSV("testfile");
        File targetDir = Files.createTempDirectory("target").toFile();
        controller.saveFile(tempFile, targetDir.getAbsolutePath());

        File copied = new File(targetDir, tempFile.getName());
        assertTrue(copied.exists());
    }

    @Test
    public void testSaveFile_overwritesExistingFile() throws IOException {
        File source = createTempCSV("conflict");
        File destDir = Files.createTempDirectory("destDir").toFile();
        File existing = new File(destDir, source.getName());
        Files.writeString(existing.toPath(), "OLD CONTENT");
        assertTrue(existing.exists());

        controller.saveFile(source, destDir.getAbsolutePath());

        String contents = Files.readString(existing.toPath());
        assertEquals("id,value\n1,abc", contents.trim());
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

    // ========== UTILITIES ==========

    private File createTempCSV(String name) throws IOException {
        File tempFile = File.createTempFile(name, ".csv");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), "id,value\n1,abc".getBytes());
        return tempFile;
    }

    private void setField(String name, Object value) {
        try {
            Field field = ImportController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + name, e);
        }
    }

    private Object getField(String name) {
        try {
            Field field = ImportController.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + name, e);
        }
    }

    private void invokeSafe(String methodName, Object... params) {
        try {
            Class<?>[] paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].getClass();
            }
            var method = ImportController.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            method.invoke(controller, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
}
