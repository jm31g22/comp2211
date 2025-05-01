package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.ChartsSceneController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ChartsSceneControllerTests {

    private ChartsSceneController controller;
    private static final long TIMEOUT = 5;

    @BeforeAll
    public static void initJavaFX() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    public void setUp() throws Exception {
        controller = new ChartsSceneController();

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setField("impressionPie", new PieChart());
                setField("chartSelection", new ChoiceBox<>());
                setField("metricSelection", new ChoiceBox<>());
                setField("timeSelection", new ChoiceBox<>());
                setField("granSelection", new ChoiceBox<>());
                setField("lowerDatePicker", new DatePicker());
                setField("upperDatePicker", new DatePicker());
                setField("dateRangeToggle", new Button());
                setField("panLeftButton", new Button());
                setField("panRightButton", new Button());
                setField("hoverPane", new VBox());
                setField("stackPaneGraph", new StackPane());
                setField("applyFilters", new Button());
                setField("timeLabel", new Label());
                setField("valueLabel", new Label());
                setField("timeOrCatLabel", new Label());
                setField("fromLabel", new Text());
                setField("toLabel", new Text());

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
    public void testInitialize_SetsChartSelectionOptions() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ChoiceBox<String> chartBox = (ChoiceBox<String>) getField("chartSelection");
            assertTrue(chartBox.getItems().contains("Metrics by time"));
            assertTrue(chartBox.getItems().contains("Impression Chart"));
            assertTrue(chartBox.getItems().contains("Histogram of click costs"));
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testDateToggleVisibilitySwitch() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            invokeMethod("handleDateRangeToggle");

            assertTrue(((DatePicker) getField("lowerDatePicker")).isVisible());
            assertTrue(((DatePicker) getField("upperDatePicker")).isVisible());
            assertTrue(((Text) getField("fromLabel")).isVisible());
            assertTrue(((Text) getField("toLabel")).isVisible());

            invokeMethod("handleDateRangeToggle");

            assertFalse(((DatePicker) getField("lowerDatePicker")).isVisible());
            assertFalse(((DatePicker) getField("upperDatePicker")).isVisible());
            assertFalse(((Text) getField("fromLabel")).isVisible());
            assertFalse(((Text) getField("toLabel")).isVisible());

            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testRefreshScene_HidesHoverPane() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VBox hoverPane = (VBox) getField("hoverPane");
            hoverPane.setOpacity(1);
            invokeMethod("refreshScene");
            assertEquals(0, hoverPane.getOpacity());
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    // ----------------- Helper Methods --------------------
    private void setField(String fieldName, Object value) {
        try {
            Field field = ChartsSceneController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getField(String fieldName) {
        try {
            Field field = ChartsSceneController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    private void invokeMethod(String methodName, Object... args) {
        try {
            Method method = ChartsSceneController.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
}
