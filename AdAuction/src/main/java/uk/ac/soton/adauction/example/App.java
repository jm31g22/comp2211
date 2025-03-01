package uk.ac.soton.adauction.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/uk/ac/soton/adauction/example/loginPage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("AdGuru - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}