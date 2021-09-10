package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
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
        var wrapper = new ReadOnlyObjectWrapper<Color>();
        wrapper.bind(colorPicker.valueProperty());
        this.canvasController.bindColorProperty(wrapper.getReadOnlyProperty());
        layers.getItems().add(new LayerItem("Background", canvasController.getMainCanvas()));
        layers.getSelectionModel().selectFirst();
        this.canvasController.bindActiveLayer(layers.getSelectionModel().selectedItemProperty());
        layers.setCellFactory(listView -> new LayerCell());
    }

    public void drawImage(Image image) {
        canvasController.drawImage(image);
        Platform.runLater(() -> {
            layers.getItems().clear();
            layers.getItems().add(new LayerItem("Background", canvasController.getMainCanvas()));
            layers.getSelectionModel().selectFirst();
        });
    }

    public BufferedImage getImage() {
        return canvasController.getImageForSaving();
    }

    public void addLayer() {
        var layer = new LayerItem(String.format("Layer %d", layers.getItems().size()));
        layers.getItems().add(0, layer);
        layers.getSelectionModel().selectFirst();
        canvasController.addCanvas(layer.getCanvas());
    }

    public void deleteActiveLayer() {
        layers.getItems().remove(layers.getSelectionModel().getSelectedIndex());
    }

    public void mergeDownActiveLayer() {

    }
}
