package space.graynk.sie;

import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import space.graynk.sie.gui.ZoomableScrollPane;

import java.awt.image.BufferedImage;

public class CanvasController {
    @FXML
    private ZoomableScrollPane scrollPane;
    @FXML
    private Canvas mainCanvas;
    public final ReadOnlyObjectProperty<Image> layerPreviewProperty;
    private final ObjectProperty<Image> layerPreview = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Color> currentColor = new ReadOnlyObjectWrapper<>(Color.BLACK);

    private GraphicsContext context;

    public CanvasController() {
        var previewWrapper = new ReadOnlyObjectWrapper<Image>();
        previewWrapper.bind(this.layerPreview);
        this.layerPreviewProperty = previewWrapper.getReadOnlyProperty();
    }

    @FXML
    private void initialize() {
        this.mainCanvas.setWidth(500);
        this.mainCanvas.setHeight(500);
        context = mainCanvas.getGraphicsContext2D();
        context.setImageSmoothing(false);
        context.setFill(Color.WHITE);
        context.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    }

    public void drawImage(Image image) {
        Runnable runnable = () -> {
            mainCanvas.setWidth(image.getWidth());
            mainCanvas.setHeight(image.getHeight());
            context.drawImage(image, 0, 0);
            layerPreview.setValue(getPreview());
            scrollPane.resetScale();
        };

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    public Image getPreview() {
        var previewSize = 50;
        var width = mainCanvas.getWidth();
        var height = mainCanvas.getHeight();
        final SnapshotParameters spa = new SnapshotParameters();
        var biggestSide = Math.max(width, height);
        var scale = previewSize / biggestSide;
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage(previewSize, previewSize);
        mainCanvas.snapshot(spa, image);
        return image;
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
        if (event.isMiddleButtonDown()) return;
        event.consume();
        if (event.isSecondaryButtonDown()) {
            var angle = Math.atan2(
                    event.getY() - mainCanvas.getHeight(),
                    event.getX() - mainCanvas.getWidth()
            );
//            WritableImage writableImage = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
            var writableImage = mainCanvas.snapshot(null, null);
//            context.save();
            context.setFill(Color.WHITE);
            context.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
//            context.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
            Rotate r = new Rotate(angle, mainCanvas.getWidth() / 2, mainCanvas.getHeight() / 2);
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
        layerPreview.setValue(getPreview());
    }

    public void bindColorProperty(ObjectProperty<Color> color) {
        this.currentColor.bind(color);
    }
}
