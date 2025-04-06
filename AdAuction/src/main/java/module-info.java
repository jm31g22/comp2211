module uk.ac.soton.adauction {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires itextpdf;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    //requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires java.mail;
    requires it.unimi.dsi.fastutil;

    opens uk.ac.soton.adauction.example.Controller to javafx.fxml;
    opens uk.ac.soton.adauction.example to javafx.fxml;
    exports uk.ac.soton.adauction.example;
    exports uk.ac.soton.adauction.example.Controller;
    exports uk.ac.soton.adauction.example.Utils;
    opens uk.ac.soton.adauction.example.Utils to javafx.fxml;
}