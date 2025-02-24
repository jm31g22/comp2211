module uk.ac.soton.adauction {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.adauction to javafx.fxml;
    exports uk.ac.soton.adauction.example;
    opens uk.ac.soton.adauction.example to javafx.fxml;
}