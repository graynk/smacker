package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import space.graynk.sie.gui.Layer;
import space.graynk.sie.gui.LayerCell;
import space.graynk.sie.gui.ZoomableScrollPane;
import space.graynk.sie.tools.Tool;
import space.graynk.sie.tools.manipulation.Select;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    private final ListView<Layer> layers = new ListView<>();
    @FXML
    private ZoomableScrollPane scrollPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private Canvas backgroundCanvas;
    @FXML
    private Canvas toolCanvas;
    @FXML
    private Spinner<Integer> textHeightSpinner;
    @FXML
    private Spinner<Integer> rowsSpinner;
    @FXML
    private ScrollPane stickerPane;
    @FXML
    private StackPane stackStickerPane;
    @FXML
    private Canvas backgroundStickerCanvas;
    @FXML
    private Canvas textCanvas;

    private SelectionModel<Layer> selectionModel;
    private ReadOnlyObjectProperty<Layer> activeLayer;
    private ObjectProperty<Tool> activeTool;
    public ReadOnlyBooleanProperty backgroundSelected;
    private final IntegerProperty layersCount = new SimpleIntegerProperty(0);
    public ReadOnlyIntegerProperty layersCountProperty;
    private double offsetX;
    private double offsetY;
    private double scale = 0.5;

    @FXML
    private void initialize() {
        this.activeTool = new ReadOnlyObjectWrapper<>(new Select());
        textHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 55, 16));
        rowsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));
        var layerCountWrapper = new ReadOnlyIntegerWrapper(0);
        layerCountWrapper.bind(layersCount);
        layersCountProperty = layerCountWrapper.getReadOnlyProperty();
        selectionModel = layers.getSelectionModel();
        var backgroundSelectedWrapper = new ReadOnlyBooleanWrapper();
        backgroundSelectedWrapper.bind(
                selectionModel.selectedIndexProperty()
                        .isEqualTo(
                                layersCountProperty.subtract(1
                                )
                        )
        );
        backgroundSelected = backgroundSelectedWrapper.getReadOnlyProperty();
        activeLayer = selectionModel.selectedItemProperty();

        layers.getItems().addListener((ListChangeListener<Layer>) c -> layersCount.set(layers.getItems().size()));
        layers.getItems().add(new Layer("Background", backgroundCanvas));
        selectionModel.selectFirst();
        layers.setCellFactory(listView -> new LayerCell());
        this.backgroundStickerCanvas.setHeight(512);
        this.backgroundStickerCanvas.setWidth(512);
    }

    public void newImage(boolean transparent) {
        this.newImage(500, 500, transparent);
    }

    public void newImage(int width, int height, boolean transparent) {
        backgroundCanvas.setWidth(width);
        backgroundCanvas.setHeight(height);
        var context = backgroundCanvas.getGraphicsContext2D();
//        context.setImageSmoothing(false);
        if (!transparent) {
            context.setFill(Color.WHITE);
            context.fillRect(0, 0, width, height);
        }
        this.activeLayer.getValue().updatePreview();
    }

    public void drawImage(Image image) {
        activeLayer.getValue().drawImage(image);
        mirrorCanvas();
        Platform.runLater(() -> {
            layers.getItems().clear();
            layers.getItems().add(new Layer("Background", backgroundCanvas));
            selectionModel.selectFirst();
        });
    }

    private void mirrorCanvas() {
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(scale, scale));
        WritableImage image = backgroundCanvas.snapshot(params, null);
        offsetX = image.getWidth() / 6;
        offsetY = image.getHeight() / 8;
        backgroundStickerCanvas.getGraphicsContext2D().drawImage(image, offsetX, offsetY, 512, 512, 0, 0, 512, 512);
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        // TODO do it with property binding and invalidation listeners
        var tool = activeTool.getValue();
        var canvas = activeLayer.getValue().getCanvas();
        tool.handleDragStart(event, canvas);
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        var tool = activeTool.getValue();
        tool.handleDrag(event);
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        var tool = activeTool.getValue();
        tool.handleDragEnd(event);
        activeLayer.getValue().updatePreview();
    }

    @FXML
    private void onToolEntered(MouseEvent event) {
        var tool = activeTool.getValue();
        tool.handleToolEnter(event, toolCanvas);
    }

    @FXML
    private void onToolExited(MouseEvent event) {
        var tool = activeTool.getValue();
        tool.handleToolLeave(event, toolCanvas);
    }

    public BufferedImage getImageForSaving() {
        final SnapshotParameters spa = new SnapshotParameters();
        var scale = 1 / scrollPane.scaleValue;
        spa.setTransform(Transform.scale(scale, scale));
        spa.setFill(Color.TRANSPARENT);
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
