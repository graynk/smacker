package space.graynk.sie;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import space.graynk.sie.gui.Layer;
import space.graynk.sie.gui.LayerCell;
import space.graynk.sie.tools.Tool;
import space.graynk.sie.tools.manipulation.Select;

import java.awt.image.BufferedImage;

public class TabInternalsController {
    @FXML
    private ListView<Layer> layers;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane imagePane;
    @FXML
    private StackPane imageStackPane;
    @FXML
    private ImageView imageView;
    @FXML
    private Canvas toolCanvas;
    @FXML
    private Spinner<Integer> textHeightSpinner;
    @FXML
    private Spinner<Integer> paddingSpinner;
    @FXML
    private StackPane fullStickerPane;
    @FXML
    private ImageView stickerImageView;

    private SelectionModel<Layer> selectionModel;
    private ReadOnlyObjectProperty<Layer> activeLayer;
    private ObjectProperty<Tool> activeTool;
    public ReadOnlyBooleanProperty backgroundSelected;
    private final IntegerProperty layersCount = new SimpleIntegerProperty(0);
    public ReadOnlyIntegerProperty layersCountProperty;
    private final DoubleProperty scaleProperty = new SimpleDoubleProperty(0.7);
    private Image openedImage;
    private final ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

    private static final int MIN_PIXELS = 10;

    @FXML
    private void initialize() {
        textHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 55, 42));
        paddingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 200, 80));
        this.activeTool = new ReadOnlyObjectWrapper<>(new Select(paddingSpinner.valueProperty()));
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
        layers.getItems().addListener((ListChangeListener<Layer>) c -> {
            var count = layers.getItems().size();
            layersCount.set(count);
        });
        layersCount.addListener((observable, oldValue, newValue) -> {
            var count = layers.getItems().size();
            // -1 skips the background
            for (int i = 0; i < count-1; i++) {
                var layer = layers.getItems().get(i);
                repositionText(layer, layer.getImage(), oldValue.intValue()-newValue.intValue());
            }
        });
        layers.setCellFactory(listView -> new LayerCell());
        toolCanvas.widthProperty().bind(imageStackPane.widthProperty());
        toolCanvas.heightProperty().bind(imageStackPane.heightProperty());
        scaleProperty.addListener((observable, oldValue, newValue) -> drawStickerBackground(newValue.doubleValue()));
        ((Select) activeTool.get()).setSelectionHeight(textHeightSpinner.getValue());
        textHeightSpinner.valueProperty().addListener((observable, oldValue, newValue) -> ((Select) activeTool.get()).setSelectionHeight(newValue));
    }

    public void drawImage(Image image) {
        openedImage = image;
        imageView.setImage(image);
        stickerImageView.setImage(image);
        var viewport = new Rectangle2D(image.getWidth()/4, image.getHeight()/4, 512, 512);
        stickerImageView.setViewport(viewport);
        imagePane.setVvalue(imagePane.getVmax());
        imagePane.setHvalue(imagePane.getHmax()/4);
        Platform.runLater(() -> {
            layers.getItems().clear();
            layers.getItems().add(new Layer("Background", stickerImageView));
            addLayer();
            selectionModel.selectFirst();
        });
    }

    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        var width = imageView.getImage().getWidth() ;
        var height = imageView.getImage().getHeight() ;

        var maxX = width - viewport.getWidth();
        var maxY = height - viewport.getHeight();

        var minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        var minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    private void drawStickerBackground(double scale) {
        final SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage((int) openedImage.getWidth(), (int) openedImage.getHeight());
        imageView.snapshot(spa, image);
        stickerImageView.setImage(image);
    }

    @FXML
    private void onStickerPressed(MouseEvent event) {
        Point2D mousePress = imageViewToImage(stickerImageView, new Point2D(event.getX(), event.getY()));
        mouseDown.set(mousePress);
    }

    @FXML
    private void onStickerDragged(MouseEvent event) {
        fullStickerPane.getScene().setCursor(Cursor.MOVE);
        Point2D dragPoint = imageViewToImage(stickerImageView, new Point2D(event.getX(), event.getY()));
        shift(stickerImageView, dragPoint.subtract(mouseDown.get()));
        mouseDown.set(imageViewToImage(stickerImageView, new Point2D(event.getX(), event.getY())));
    }

    @FXML
    private void onStickerReleased(MouseEvent event) {
        fullStickerPane.getScene().setCursor(Cursor.DEFAULT);
        Point2D mousePress = imageViewToImage(stickerImageView, new Point2D(event.getX(), event.getY()));
        mouseDown.set(mousePress);
    }

    @FXML
    private void onStickerScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        double width = openedImage.getWidth();
        double height = openedImage.getHeight();
        Rectangle2D viewport = stickerImageView.getViewport();


        double scale = clamp(scaleProperty.get() + 0.1*sign(delta),
                // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

                // don't scale so that we're bigger than image dimensions:
                Math.max(width / viewport.getWidth(), height / viewport.getHeight())

        );
        scaleProperty.set(scale);
    }

    @FXML
    private void onStickerPaneScroll(ScrollEvent event) {
        event.consume();
        var delta = event.getDeltaY();
        if (delta == 0) return;
        var scaleDelta = delta > 0 ? 0.1 : -0.1;
        scaleProperty.set(scaleProperty.get() + scaleDelta);
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        var tool = activeTool.getValue();
        tool.handleDragStart(event, toolCanvas);
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
        var select = (Select)tool;
        var selection = select.getSelection();
        var wim = new WritableImage((int)selection.getWidth(), (int)selection.getHeight());
        var parameter = new SnapshotParameters();
        var scale = select.getScale();
        if (Math.abs(1 - scale) > 0.00001) {
            parameter.setTransform(new Scale(scale, scale));
        }
        parameter.setViewport(selection);
        imageView.snapshot(parameter, wim);
        var textCanvasContext = activeLayer.get().getContext();
        textCanvasContext.clearRect(0, 0, 512, 512);
        var height = textHeightSpinner.getValue().doubleValue() * scale;
        var lines = layers.getItems().size()-1;
        var line = lines-selectionModel.getSelectedIndex()-1;
        textCanvasContext.drawImage(wim,
                (512-selection.getWidth()) / 2,
                3*512.0/4-height*lines/2+height*line+height/2
        );
        activeLayer.getValue().updatePreview();
    }

    // TODO: broken, but whatever
    private void repositionText(Layer layer, Image image, int sign) {
        var textCanvasContext = layer.getContext();
        textCanvasContext.clearRect(0, 0, 512, 512);
        var height = textHeightSpinner.getValue().doubleValue();
        var lines = layers.getItems().size()-1;
        textCanvasContext.drawImage(image,
                0,
                sign*height*lines/2+height/2
        );
        layer.updatePreview();
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
        spa.setViewport(new Rectangle2D(
                (fullStickerPane.getWidth()-512)/2,
                (fullStickerPane.getHeight()-512)/2,
                512, 512));
        var image = new WritableImage(512, 512);
        fullStickerPane.snapshot(spa, image);
        return SwingFXUtils.fromFXImage(image, null);
    }

    public void addLayer() {
        var layer = new Layer(String.format("Layer %d", layers.getItems().size()));
        layers.getItems().add(0, layer);
//        selectionModel.selectFirst();
        fullStickerPane.getChildren().add(layer.getCanvas());
    }

    public void deleteActiveLayer() {
        var selectedIndex = layers.getSelectionModel().getSelectedIndex();
        if (selectedIndex == layers.getItems().size() - 1) return;
        if (selectedIndex == 0) selectionModel.selectNext();
        else selectionModel.selectPrevious();
        layers.getItems().remove(selectedIndex);
        var canvases = fullStickerPane.getChildren();
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

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private int sign(double delta) {
        if (delta > 0) {
            return 1;
        }
        if (delta < 0) {
            return -1;
        }
        return 0;
    }
}
