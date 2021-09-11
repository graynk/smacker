package space.graynk.sie.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;

public class Layer {
    private ReadOnlyObjectWrapper<Image> preview = new ReadOnlyObjectWrapper<>();
    public ReadOnlyObjectProperty<Image> previewProperty;
    private String text;
    private final Canvas canvas;
    private final GraphicsContext context;

    public Layer(String text, Canvas canvas) {
        this.previewProperty = preview;
        this.text = text;
        this.previewProperty = preview.getReadOnlyProperty();
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
        this.updatePreview();
    }

    public Layer(String text) {
        this.previewProperty = preview;
        this.text = text;
        this.previewProperty = preview.getReadOnlyProperty();
        this.canvas = new Canvas(500, 500);
        canvas.setMouseTransparent(true);
        this.context = canvas.getGraphicsContext2D();
        this.updatePreview();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void drawImage(Image image) {
        Runnable runnable = () -> {
            canvas.setWidth(image.getWidth());
            canvas.setHeight(image.getHeight());
            context.drawImage(image, 0, 0);
            updatePreview();
        };

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    public void updatePreview() {
        var previewSize = 50;
        var width = canvas.getWidth();
        var height = canvas.getHeight();
        final SnapshotParameters spa = new SnapshotParameters();
        var biggestSide = Math.max(width, height);
        var scale = previewSize / biggestSide;
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage(previewSize, previewSize);
        canvas.snapshot(spa, image);
        preview.setValue(image);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getContext() {
        return context;
    }
}
