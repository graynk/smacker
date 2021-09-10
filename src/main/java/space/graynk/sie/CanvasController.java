package space.graynk.sie;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import space.graynk.sie.gui.LayerItem;
import space.graynk.sie.gui.ZoomableScrollPane;

import java.awt.image.BufferedImage;

public class CanvasController {
    @FXML
    private ZoomableScrollPane scrollPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private Canvas mainCanvas;
    public final ReadOnlyObjectProperty<Image> layerPreviewProperty;
    private final ObjectProperty<Image> layerPreview = new SimpleObjectProperty<>();
    private ReadOnlyObjectProperty<Color> currentColor;
    private ReadOnlyObjectProperty<LayerItem> activeLayer;

    public CanvasController() {
        var previewWrapper = new ReadOnlyObjectWrapper<Image>();
        previewWrapper.bind(this.layerPreview);
        this.layerPreviewProperty = previewWrapper.getReadOnlyProperty();
    }

    public Canvas getMainCanvas() {
        return this.mainCanvas;
    }

    @FXML
    private void initialize() {
        this.mainCanvas.setWidth(500);
        this.mainCanvas.setHeight(500);
    }

    public void drawImage(Image image) {
        activeLayer.getValue().drawImage(image);
    }

    public BufferedImage getImageForSaving() {
        final SnapshotParameters spa = new SnapshotParameters();
        var scale = 1 / scrollPane.scaleValue;
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
        mainCanvas.snapshot(spa, image);
        return SwingFXUtils.fromFXImage(image, null);
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        var canvas = activeLayer.getValue().getCanvas();
        var context = activeLayer.getValue().getContext();
        if (event.isMiddleButtonDown()) return;
        event.consume();
        if (event.isSecondaryButtonDown()) {
            var angle = Math.atan2(
                    event.getY() - canvas.getHeight(),
                    event.getX() - canvas.getWidth()
            );
//            WritableImage writableImage = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
            var writableImage = canvas.snapshot(null, null);
//            context.save();
            context.setFill(Color.WHITE);
            context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//            context.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
            Rotate r = new Rotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
            context.setImageSmoothing(false);

            context.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
//            context.rotate(1);
//            context.translate(mainCanvas.getWidth() / 2, 0);
            context.drawImage(writableImage, 0, 0);
//            context.restore();
            return;
        }
        context.setFill(currentColor.getValue());
        var radius = 4;
        context.fillOval(event.getX() - radius, event.getY() - radius, radius * 2, radius * 2);
//        context.getPixelWriter().setColor((int)event.getX(), (int)event.getY(), Color.RED);
    }

    @FXML
    private void onMouseDragEnd(MouseEvent event) {
        activeLayer.getValue().updatePreview();
    }

    public void bindColorProperty(ReadOnlyObjectProperty<Color> color) {
        this.currentColor = color;
    }

    public void bindActiveLayer(ReadOnlyObjectProperty<LayerItem> layer) {
        this.activeLayer = layer;
        var canvas = this.activeLayer.getValue().getCanvas();
        var context = canvas.getGraphicsContext2D();
        context.setImageSmoothing(false);
        context.setFill(Color.WHITE);
        context.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        this.activeLayer.getValue().updatePreview();
    }

    public void addCanvas(Canvas canvas) {
        stackPane.getChildren().add(canvas);
    }
}
