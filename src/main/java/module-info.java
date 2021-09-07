module space.graynk.sie {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens space.graynk.sie to javafx.fxml;
    exports space.graynk.sie;
}