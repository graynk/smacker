package space.graynk.sie;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import space.graynk.sie.gui.LayerCell;
import space.graynk.sie.gui.LayerItem;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    @FXML
    private ToggleGroup tools;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ListView<LayerItem> layers;
    @FXML
    private CanvasController canvasController;

    @FXML
    private void initialize() {
        this.canvasController.bindColorProperty(colorPicker.valueProperty());
        layers.setCellFactory(listView -> new LayerCell());
    }

    public void drawImage(Image image) {
        canvasController.drawImage(image);
        Platform.runLater(() -> {
            layers.getItems().clear();
            layers.getItems().add(new LayerItem(canvasController.layerPreviewProperty, "Layer 1"));
        });
    }

    public BufferedImage getImage() {
        return canvasController.getImageForSaving();
    }
}
