package space.graynk.sie;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    @FXML
    private ToggleGroup tools;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private CanvasController canvasController;

    @FXML
    private void initialize() {
        this.canvasController.bindColorProperty(colorPicker.valueProperty());
    }

    public void drawImage(Image image) {
        canvasController.drawImage(image);
    }

    public BufferedImage getImage() {
        return canvasController.getImage();
    }
}
