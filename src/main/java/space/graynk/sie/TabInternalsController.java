package space.graynk.sie;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    @FXML
    private ToggleGroup tools;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private CanvasController canvasController;

    public void drawImage(Image image) {
        canvasController.drawImage(image);
    }

    public BufferedImage getImage() {
        return canvasController.getImage();
    }

    @FXML
    private void colorChanged(ActionEvent event) {
        canvasController.setCurrentColor(colorPicker.getValue());
    }
}
