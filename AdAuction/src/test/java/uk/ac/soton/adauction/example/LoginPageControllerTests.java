package uk.ac.soton.adauction.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import uk.ac.soton.adauction.example.Controller.LoginPageController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginPageControllerTests {

    private LoginPageController controller;
    private static final long TIMEOUT = 5;

    @BeforeClass
    public static void initJavaFX() throws Exception {

        FxToolkit.registerPrimaryStage();
    }

    @Before
    public void setUp() throws Exception {
        controller = new LoginPageController();


        setField("conn", null);


        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            setField("loginMessageLabel", new Label());
            setField("usernameTextField", new TextField());
            setField("passwordPasswordField", new PasswordField());
            setField("verificationCodeField", new TextField());
            setField("sendEmailButton", new Button("Send Email"));
            invokeMethod("initialize");
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Test
    public void testSendMailButton_EmptyUsername() throws Exception {
        assertMessageAfterAction("sendMailButtonOnAction",
                () -> setField("usernameTextField", "text", ""),
                "Please enter username!");
    }

    @Test
    public void testSendMailButton_NoEmailFound() throws Exception {
        assertMessageAfterAction("sendMailButtonOnAction",
                () -> setField("usernameTextField", "text", "testUser"),
                "Users whose email does not exist or is invalid!");
    }


    @Test
    public void testSendMailButton_ConcurrentRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        setField("usernameTextField", "text", "testUser" + threadId);
                        invokeMethod("sendMailButtonOnAction", new ActionEvent());
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

        // Check that the button is disabled (indicating email attempt)
        CountDownLatch checkLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            assertTrue(((Button) getField("sendEmailButton")).isDisable());
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testLogin_EmptyFields() throws Exception {
        assertMessageAfterAction("loginButtonOnAction",
                () -> {
                    setField("usernameTextField", "text", "");
                    setField("passwordPasswordField", "text", "");
                    setField("verificationCodeField", "text", "");
                },
                "Please enter username and password!");
    }



    @Test
    public void testLogin_NoCodeSent() throws Exception {
        assertMessageAfterAction("loginButtonOnAction",
                () -> {
                    setField("usernameTextField", "text", "user");
                    setField("passwordPasswordField", "text", "pass");
                    setField("verificationCodeField", "text", "123456");
                },
                "You did not send the verification code!");
    }

    @Test
    public void testLogin_WrongCode() throws Exception {
        setField("dynamicVerificationCode", "123456");
        assertMessageAfterAction("loginButtonOnAction",
                () -> {
                    setField("usernameTextField", "text", "user");
                    setField("passwordPasswordField", "text", "pass");
                    setField("verificationCodeField", "text", "654321");
                },
                "Login failed. Incorrect email verification code!");
    }

    @Test
    public void testLogin_CorrectCode_NoDB() throws Exception {
        setField("dynamicVerificationCode", "123456");
        assertMessageAfterAction("loginButtonOnAction",
                () -> {
                    setField("usernameTextField", "text", "user");
                    setField("passwordPasswordField", "text", "pass");
                    setField("verificationCodeField", "text", "123456");
                },
                "Invalid Login. Please try again.");
    }

    @Test
    public void testLogin_ExcessiveInputLength() throws Exception {
        String longString = "a".repeat(1000);
        assertMessageAfterAction("loginButtonOnAction",
                () -> {
                    setField("usernameTextField", "text", longString);
                    setField("passwordPasswordField", "text", longString);
                    setField("verificationCodeField", "text", longString);
                },
                "You did not send the verification code!");
    }

    @Test
    public void testLogin_ConcurrentAttempts() throws Exception {
        setField("dynamicVerificationCode", "123456");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    CountDownLatch innerLatch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        setField("usernameTextField", "text", "user" + Thread.currentThread().getId());
                        setField("passwordPasswordField", "text", "pass");
                        setField("verificationCodeField", "text", "123456");
                        invokeMethod("loginButtonOnAction");
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
            assertEquals("You did not send the verification code!", ((Label) getField("loginMessageLabel")).getText());
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testGenerateVerificationCode_Uniqueness() {
        String[] codes = new String[1000];
        for (int i = 0; i < 1000; i++) {
            codes[i] = LoginPageController.generateVerificationCode();
            assertEquals(6, codes[i].length());
            assertTrue(codes[i].matches("\\d{6}"));
        }
        long uniqueCount = java.util.Arrays.stream(codes).distinct().count();
        assertTrue("Too many duplicate codes: " + (1000 - uniqueCount), uniqueCount > 950);
    }

    @Test
    public void testRefreshScene_EdgeCases() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Label label = (Label) getField("loginMessageLabel");
            label.setText(null); // Null text edge case
            label.setVisible(true);
            setField("usernameTextField", "text", null);
            setField("passwordPasswordField", "text", null);
            setField("verificationCodeField", "text", null);

            invokeMethod("refreshScene");

            assertFalse(label.isVisible());
            assertEquals("", ((TextField) getField("usernameTextField")).getText());
            assertEquals("", ((PasswordField) getField("passwordPasswordField")).getText());
            assertEquals("", ((TextField) getField("verificationCodeField")).getText());
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
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
                        Label label = (Label) getField("loginMessageLabel");
                        label.setText("Error" + Thread.currentThread().getId());
                        label.setVisible(true);
                        setField("usernameTextField", "text", "user" + Thread.currentThread().getId());
                        setField("passwordPasswordField", "text", "pass");
                        setField("verificationCodeField", "text", "123");

                        invokeMethod("refreshScene");
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
            assertFalse(((Label) getField("loginMessageLabel")).isVisible());
            assertEquals("", ((TextField) getField("usernameTextField")).getText());
            assertEquals("", ((PasswordField) getField("passwordPasswordField")).getText());
            assertEquals("", ((TextField) getField("verificationCodeField")).getText());
            checkLatch.countDown();
        });
        checkLatch.await(TIMEOUT, TimeUnit.SECONDS);
    }

    // Helper Methods using Reflection
    private void setField(String fieldName, Object value) {
        try {
            Field field = LoginPageController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private void setField(String fieldName, String property, Object value) {
        try {
            Object target = getField(fieldName);
            if (target == null) return; // Handle null field case
            Method setter = target.getClass().getMethod("set" + capitalize(property), value == null ? String.class : value.getClass());
            setter.invoke(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set property: " + property, e);
        }
    }

    private Object getField(String fieldName) {
        try {
            Field field = LoginPageController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    private void invokeMethod(String methodName, Object... args) {
        try {
            if (methodName.equals("initialize") || methodName.equals("loginButtonOnAction")) {
                Method method = LoginPageController.class.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(controller);
            } else if (methodName.equals("sendMailButtonOnAction")) {
                Method method = LoginPageController.class.getDeclaredMethod(methodName, ActionEvent.class);
                method.setAccessible(true);
                method.invoke(controller, args.length > 0 ? args[0] : null);
            } else {
                Method method = LoginPageController.class.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(controller);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    private void assertMessageAfterAction(String methodName, Runnable setup, String expectedMessage) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            setup.run();
            if (methodName.equals("sendMailButtonOnAction")) {
                invokeMethod(methodName, new ActionEvent());
            } else {
                invokeMethod(methodName);
            }
            latch.countDown();
        });
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(expectedMessage, ((Label) getField("loginMessageLabel")).getText());
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
