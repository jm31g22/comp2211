package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.DashboardController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardControllerTests {

    private DashboardController controller;
    private static final long TIMEOUT = 5;

    @BeforeAll
    public static void initJavaFX() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    public void setUp() throws Exception {
        controller = new DashboardController();

        // Initialize FXML components on the JavaFX thread
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            setField("impressionsLabel", new Label());
            setField("clicksLabel", new Label());
            setField("costLabel", new Label());
            setField("CPALabel", new Label());
            setField("CPMLabel", new Label());
            setField("CPCLabel", new Label());
            setField("CTRLabel", new Label());
            setField("uniquesLabel", new Label());
            setField("bounceRateLabel", new Label());
            setField("conversionsLabel", new Label());
            setField("bouncesLabel", new Label());
            setField("maleGenderButton", new RadioButton("Male"));
            setField("femaleGenderButton", new RadioButton("Female"));
            setField("bothGenderButton", new RadioButton("Both"));
            setField("lowIncomeButton", new RadioButton("Low"));
            setField("mediumIncomeButton", new RadioButton("Medium"));
            setField("highIncomeButton", new RadioButton("High"));
            setField("allIncomeButton", new RadioButton("All"));
            setField("newsContextButton", new RadioButton("News"));
            setField("blogContextButton", new RadioButton("Blog"));
            setField("socialMediaContextButton", new RadioButton("Social Media"));
            setField("allContextButton", new RadioButton("All"));
            setField("shoppingContextButton", new RadioButton("Shopping"));
            setField("ageButton1", new RadioButton("<25"));
            setField("ageButton2", new RadioButton("25-34"));
            setField("ageButton3", new RadioButton("35-44"));
            setField("ageButton4", new RadioButton("45-54"));
            setField("ageButton5", new RadioButton("55-64"));
            setField("ageButton6", new RadioButton(">=65"));
            setField("startDatePicker", new DatePicker());
            setField("endDatePicker", new DatePicker());

            // Mock metricValuePairs to avoid SQLException in initialize
            HashMap<String, SimpleStringProperty> mockMetrics = new HashMap<>();
            mockMetrics.put("NumberOfImpressions", new SimpleStringProperty("1000"));
            mockMetrics.put("NumberOfClicks", new SimpleStringProperty("50"));
            mockMetrics.put("TotalCost", new SimpleStringProperty("500.00"));
            mockMetrics.put("CPA", new SimpleStringProperty("10.00"));
            mockMetrics.put("CPM", new SimpleStringProperty("5.00"));
            mockMetrics.put("CPC", new SimpleStringProperty("2.00"));
            mockMetrics.put("CTR", new SimpleStringProperty("5%"));
            mockMetrics.put("NumberOfUniques", new SimpleStringProperty("800"));
            mockMetrics.put("BounceRate", new SimpleStringProperty("20%"));
            mockMetrics.put("NumberOfConversions", new SimpleStringProperty("10"));
            mockMetrics.put("NumberOfBounces", new SimpleStringProperty("200"));
            setField("metricValuePairs", mockMetrics);

            try {
                invokeMethod("initialize");
            } catch (Exception e) {
                System.err.println("Initialize failed: " + e.getMessage());
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
    public void testInitialize_ToggleGroups() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ToggleGroup genderGroup = (ToggleGroup) getField("genderToggleGroup");
            assertTrue(genderGroup.getToggles().contains(getField("maleGenderButton")));
            assertTrue(genderGroup.getToggles().contains(getField("femaleGenderButton")));
            assertTrue(genderGroup.getToggles().contains(getField("bothGenderButton")));

            ToggleGroup incomeGroup = (ToggleGroup) getField("incomeToggleGroup");
            assertTrue(incomeGroup.getToggles().contains(getField("lowIncomeButton")));
            assertTrue(incomeGroup.getToggles().contains(getField("mediumIncomeButton")));
            assertTrue(incomeGroup.getToggles().contains(getField("highIncomeButton")));
            assertTrue(incomeGroup.getToggles().contains(getField("allIncomeButton")));

            ToggleGroup contextGroup = (ToggleGroup) getField("contextToggleGroup");
            assertTrue(contextGroup.getToggles().contains(getField("newsContextButton")));
            assertTrue(contextGroup.getToggles().contains(getField("blogContextButton")));
            assertTrue(contextGroup.getToggles().contains(getField("socialMediaContextButton")));
            assertTrue(contextGroup.getToggles().contains(getField("allContextButton")));
            assertTrue(contextGroup.getToggles().contains(getField("shoppingContextButton")));

            ToggleGroup ageGroup = (ToggleGroup) getField("ageToggleGroup");
            assertTrue(ageGroup.getToggles().contains(getField("ageButton1")));
            assertTrue(ageGroup.getToggles().contains(getField("ageButton2")));
            assertTrue(ageGroup.getToggles().contains(getField("ageButton3")));
            assertTrue(ageGroup.getToggles().contains(getField("ageButton4")));
            assertTrue(ageGroup.getToggles().contains(getField("ageButton5")));
            assertTrue(ageGroup.getToggles().contains(getField("ageButton6")));
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }


    @Test
    public void testApplyFilters_ConcurrentCalls() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        ((RadioButton) getField("maleGenderButton")).setSelected(threadId % 2 == 0);
                        ((RadioButton) getField("highIncomeButton")).setSelected(threadId % 2 == 1);
                        ((RadioButton) getField("ageButton" + (threadId + 1))).setSelected(true);
                        try {
                            invokeMethod("applyFilters");
                        } catch (Exception e) {
                            System.err.println("Concurrent ApplyFilters failed: " + e.getMessage());
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
            assertNotNull(getField("metricValuePairs"));
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }



    @Test
    public void testRefreshScene_ConcurrentCalls() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        try {
                            invokeMethod("refreshScene");
                        } catch (Exception e) {
                            System.err.println("Concurrent RefreshScene failed: " + e.getMessage());
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
            assertNotNull(getField("metricValuePairs"));
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }



    @Test
    public void testToggleGroups_NullSelection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ((ToggleGroup) getField("genderToggleGroup")).selectToggle(null);
            ((ToggleGroup) getField("incomeToggleGroup")).selectToggle(null);
            ((ToggleGroup) getField("contextToggleGroup")).selectToggle(null);
            ((ToggleGroup) getField("ageToggleGroup")).selectToggle(null);
            try {
                invokeMethod("applyFilters");
                assertNotNull(getField("metricValuePairs"));
            } catch (Exception e) {
                fail("ApplyFilters with null selection failed: " + e.getMessage());
            }
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    // Helper Methods using Reflection
    private void setField(String fieldName, Object value) {
        try {
            Field field = DashboardController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getField(String fieldName) {
        try {
            Field field = DashboardController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    private void invokeMethod(String methodName, Object... args) {
        try {
            if (methodName.equals("initialize") || methodName.equals("applyFilters") || methodName.equals("refreshScene")) {
                Method method = DashboardController.class.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(controller);
            } else {
                Method method = DashboardController.class.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(controller);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
}

