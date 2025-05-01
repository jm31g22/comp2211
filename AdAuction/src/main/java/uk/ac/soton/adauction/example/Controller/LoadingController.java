package uk.ac.soton.adauction.example.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class LoadingController extends SceneController {

    @FXML private Label loadingLabel;
    @FXML private ProgressIndicator progressIndicator;

    private Timeline messageTimeline;
    private final List<String> loadingMessages = List.of(
            "Loading campaign data...",
            "Parsing impression logs...",
            "Parsing server logs...",
            "Parsing click logs...",
            "Crunching numbers...",
            "Verifying data integrity...",
            "Almost there..."
    );

    private final Random random = new Random();

    @Override
    public void refreshScene() throws SQLException {
        startRotatingMessages();
    }

    private void startRotatingMessages() {
        messageTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            String randomMessage = loadingMessages.get(random.nextInt(loadingMessages.size()));
            loadingLabel.setText(randomMessage);
        }));
        messageTimeline.setCycleCount(Timeline.INDEFINITE);
        messageTimeline.play();
    }


}
