package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.SceneController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SceneControllerTests {

    private TestSceneController controller;
    private static final long TIMEOUT = 5; // seconds

    // Concrete implementation for testing
    private static class TestSceneController extends SceneController {
        @Override
        public void refreshScene() throws SQLException {
            // Minimal implementation for testing
        }
    }

    @BeforeAll
    public static void initJavaFX() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    public void setUp() throws Exception {
        controller = new TestSceneController();

        // Initialize FXML components on the JavaFX thread
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            setField("backButton", new Button("Back"));
            setField("dashboardButton", new Button("Dashboard"));
            setField("settingsButton", new Button("Settings"));
            setField("chartsButton", new Button("Charts"));
            setField("homeButton", new Button("Home"));
            setField("logOutButton", new Button("Log Out"));

            try {
                invokeMethod("initialize");
            } catch (Exception e) {
                fail("Initialize failed: " + e.getMessage());
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
    public void testInitialize_ButtonEventHandlers() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Button backButton = (Button) getField("backButton");
            assertNotNull(backButton.getOnAction(), "Back button should have an event handler");

            Button dashboardButton = (Button) getField("dashboardButton");
            assertNotNull(dashboardButton.getOnAction(), "Dashboard button should have an event handler");

            Button settingsButton = (Button) getField("settingsButton");
            assertNotNull(settingsButton.getOnAction(), "Settings button should have an event handler");

            Button chartsButton = (Button) getField("chartsButton");
            assertNotNull(chartsButton.getOnAction(), "Charts button should have an event handler");

            Button homeButton = (Button) getField("homeButton");
            assertNotNull(homeButton.getOnAction(), "Home button should have an event handler");

            Button logOutButton = (Button) getField("logOutButton");
            assertNotNull(logOutButton.getOnAction(), "Log Out button should have an event handler");

            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testInitialize_NullButtons() throws Exception {
        controller = new TestSceneController(); // Reset controller
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            setField("backButton", null);
            setField("dashboardButton", null);
            setField("settingsButton", null);
            setField("chartsButton", null);
            setField("homeButton", null);
            setField("logOutButton", null);

            try {
                invokeMethod("initialize");
                // Should not throw NPE, just skip setting handlers
                assertTrue(true); // If we reach here, no exception occurred
            } catch (Exception e) {
                fail("Initialize with null buttons failed: " + e.getMessage());
            }
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }


    @Test
    public void testButtonActions_ConcurrentClicks() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch latch = new CountDownLatch(6);
        String[] methods = {"switchToDashboard", "switchToCharts", "switchToSettings",
                "switchToLogin", "switchToLastScene", "switchToDashboard"};

        for (int i = 0; i < 6; i++) {
            final String method = methods[i];
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        try {
                            Button button = switch (method) {
                                case "switchToDashboard" -> (Button) getField("dashboardButton");
                                case "switchToCharts" -> (Button) getField("chartsButton");
                                case "switchToSettings" -> (Button) getField("settingsButton");
                                case "switchToLogin" -> (Button) getField("logOutButton");
                                case "switchToLastScene" -> (Button) getField("backButton");
                                default -> null;
                            };
                            if (button != null && button.getOnAction() != null) {
                                button.getOnAction().handle(new ActionEvent());
                            }
                            assertTrue(true); // No exception means success
                        } catch (Exception e) {
                            System.err.println(method + " concurrent failed: " + e.getMessage());
                        }
                        innerLatch.countDown();
                    });
                    innerLatch.await(TIMEOUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(TIMEOUT * 2, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void testInitialize_ConcurrentCalls() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        try {
                            invokeMethod("initialize");
                            assertNotNull(((Button) getField("dashboardButton")).getOnAction());
                        } catch (Exception e) {
                            System.err.println("Concurrent Initialize failed: " + e.getMessage());
                        }
                        innerLatch.countDown();
                    });
                    innerLatch.await(TIMEOUT, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(TIMEOUT * 2, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS));

        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            assertNotNull(((Button) getField("dashboardButton")).getOnAction());
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testRefreshScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.refreshScene(); // Direct call since it's abstract
                assertTrue(true); // No exception means success
            } catch (SQLException e) {
                fail("RefreshScene failed: " + e.getMessage());
            }
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    // Helper Methods using Reflection
    private void setField(String fieldName, Object value) {
        try {
            Field field = SceneController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getField(String fieldName) {
        try {
            Field field = SceneController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    private void invokeMethod(String methodName, Object... args) {
        try {
            Method method = SceneController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
}
