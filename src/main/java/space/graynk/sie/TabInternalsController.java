package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import space.graynk.sie.gui.LayerCell;
import space.graynk.sie.gui.Layer;
import space.graynk.sie.gui.ToolButton;
import space.graynk.sie.gui.ZoomableScrollPane;
import space.graynk.sie.tools.Tool;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    @FXML
    private ToggleGroup tools;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ListView<Layer> layers;
    @FXML
    private ZoomableScrollPane scrollPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private Canvas backgroundCanvas;

    private SelectionModel<Layer> selectionModel;
    private ReadOnlyObjectProperty<Layer> activeLayer;
    private ObjectProperty<Color> activeColor;
    private ObjectProperty<Tool> activeTool;
    public ReadOnlyIntegerProperty activeLayerIndexProperty;
    private final IntegerProperty layersCount = new SimpleIntegerProperty(0);
    public ReadOnlyIntegerProperty layersCountProperty;

    @FXML
    private void initialize() {
        var layerCountWrapper = new ReadOnlyIntegerWrapper(0);
        layerCountWrapper.bind(layersCount);
        layersCountProperty = layerCountWrapper.getReadOnlyProperty();
        selectionModel = layers.getSelectionModel();
        activeLayerIndexProperty = selectionModel.selectedIndexProperty();
        activeLayer = selectionModel.selectedItemProperty();
        activeColor = colorPicker.valueProperty();

        layers.getItems().addListener((ListChangeListener<Layer>) c -> layersCount.set(layers.getItems().size()));
        layers.getItems().add(new Layer("Background", backgroundCanvas));
        selectionModel.selectFirst();
        layers.setCellFactory(listView -> new LayerCell());
    }

    public void newImage() {
        this.newImage(500, 500);
    }

    public void newImage(int width, int height) {
        backgroundCanvas.setWidth(width);
        backgroundCanvas.setHeight(height);
        var context = backgroundCanvas.getGraphicsContext2D();
//        context.setImageSmoothing(false);
        context.setFill(Color.WHITE);
        context.fillRect(0, 0, width, height);
        this.activeLayer.getValue().updatePreview();
    }

    public void drawImage(Image image) {
        activeLayer.getValue().drawImage(image);
        Platform.runLater(() -> {
            layers.getItems().clear();
            layers.getItems().add(new Layer("Background", backgroundCanvas));
            selectionModel.selectFirst();
        });
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        // TODO do it with property binding and invalidation listeners
        var tool = ((ToolButton)tools.getSelectedToggle()).getTool();
        var canvas = activeLayer.getValue().getCanvas();
        canvas.getGraphicsContext2D().setFill(activeColor.getValue());
        canvas.getGraphicsContext2D().setStroke(activeColor.getValue());
        tool.handleDragStart(event, canvas);
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        var tool = ((ToolButton)tools.getSelectedToggle()).getTool();
        tool.handleDrag(event);
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        var tool = ((ToolButton)tools.getSelectedToggle()).getTool();
        tool.handleDragEnd(event);
        activeLayer.getValue().updatePreview();
    }

    public BufferedImage getImageForSaving() {
        final SnapshotParameters spa = new SnapshotParameters();
        var scale = 1 / scrollPane.scaleValue;
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage((int) stackPane.getWidth(), (int) stackPane.getHeight());
        stackPane.snapshot(spa, image);
        return SwingFXUtils.fromFXImage(image, null);
    }

    public void addLayer() {
        var layer = new Layer(String.format("Layer %d", layers.getItems().size()));
        layers.getItems().add(0, layer);
        selectionModel.selectFirst();
        stackPane.getChildren().add(layer.getCanvas());
    }

    public void deleteActiveLayer() {
        var selectedIndex = layers.getSelectionModel().getSelectedIndex();
        if (selectedIndex == layers.getItems().size() - 1) return;
        if (selectedIndex == 0) selectionModel.selectNext();
        else selectionModel.selectPrevious();
        layers.getItems().remove(selectedIndex);
        var canvases = stackPane.getChildren();
        canvases.remove(canvases.size() - selectedIndex - 1);
    }

    public void mergeDownActiveLayer() {
        var selectedIndex = selectionModel.getSelectedIndex();
        if (selectedIndex == layers.getItems().size() - 1) return;
        var image = activeLayer.getValue().getImage();
        var prevLayer = layers.getItems().get(selectedIndex+1);
        prevLayer.drawImage(image);
        this.deleteActiveLayer();
    }
}
