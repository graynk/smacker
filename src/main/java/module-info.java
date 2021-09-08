module space.graynk.sie {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.coreui;
    requires org.kordamp.ikonli.fluentui;

    opens space.graynk.sie to javafx.fxml;
    exports space.graynk.sie;
    exports space.graynk.sie.gui;
    opens space.graynk.sie.gui to javafx.fxml;
}