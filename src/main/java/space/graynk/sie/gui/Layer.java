package space.graynk.sie.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

public class Layer {
    private ReadOnlyObjectWrapper<Image> preview = new ReadOnlyObjectWrapper<>();
    public ReadOnlyObjectProperty<Image> previewProperty;
    private String text;
    private Canvas canvas;
    private GraphicsContext context;

    public Layer(String text, ImageView imageView) {
        this.previewProperty = preview;
        this.text = text;
        this.previewProperty = preview.getReadOnlyProperty();
        this.updatePreview(imageView);
    }

    public Layer(String text, Canvas canvas) {
        this.previewProperty = preview;
        this.text = text;
        this.previewProperty = preview.getReadOnlyProperty();
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
        this.updatePreview(canvas);
    }

    public Layer(String text) {
        this.previewProperty = preview;
        this.text = text;
        this.previewProperty = preview.getReadOnlyProperty();
        this.canvas = new Canvas(500, 500);
        canvas.setMouseTransparent(true);
        this.context = canvas.getGraphicsContext2D();
        this.updatePreview(canvas);
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
            updatePreview(canvas);
        };

        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        Platform.runLater(runnable);
    }

    public void updatePreview() {
        this.updatePreview(canvas);
    }

    private void updatePreview(Node node) {
        var previewSize = 50;
        var width = node.getBoundsInLocal().getWidth();
        var height = node.getBoundsInLocal().getHeight();
        final SnapshotParameters spa = new SnapshotParameters();
        var biggestSide = Math.max(width, height);
        var scale = previewSize / biggestSide;
        spa.setTransform(Transform.scale(scale, scale));
        var image = new WritableImage((int)(width*scale), (int)(height*scale));
        node.snapshot(spa, image);
        preview.setValue(image);
    }

    public Image getImage() {
        var width = canvas.getWidth();
        var height = canvas.getHeight();
        var image = new WritableImage((int) width, (int) height);
        var parameter = new SnapshotParameters();
        parameter.setFill(Color.TRANSPARENT);
        canvas.snapshot(parameter, image);
        return image;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public GraphicsContext getContext() {
        return context;
    }
}
