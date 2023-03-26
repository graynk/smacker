package space.graynk.sie.tools.manipulation;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import space.graynk.sie.tools.Tool;

public class Select extends Tool {
    private Point2D mouseStart;
    private Point2D mouseEnd;
    private Canvas canvas;
    private GraphicsContext context;
    private double scale = 1;
    private final ReadOnlyObjectProperty<Integer> scalePadding;
    private final ReadOnlyObjectProperty<Double> textHeight;

    public Select(ReadOnlyObjectProperty<Integer> scalePadding, ReadOnlyObjectProperty<Double> textHeight) {
        super();
        this.scalePadding = scalePadding;
        this.textHeight = textHeight;
    }

    @Override
    public void handleDragStart(MouseEvent event, Canvas canvas) {
        this.mouseStart = new Point2D(event.getX(), event.getY());
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
        this.context.setLineDashes(20, 10);
    }

    @Override
    public void handleDrag(MouseEvent event) {
        var height = textHeight.getValue();
        super.handleDrag(event);
        var startX = mouseStart.getX();
        var startY = mouseStart.getY() - height;
        var width = event.getX() - startX;

        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // probably can be done with a gradient, but whatever
        this.context.setStroke(Color.BLACK);
        this.context.setLineWidth(4);
        context.strokeRect(startX, startY, width, height);
        context.setStroke(Color.WHITE);
        this.context.setLineWidth(2);
        context.strokeRect(startX, startY, width, height);
    }

    @Override
    public void handleDragEnd(MouseEvent event) {
        context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        this.mouseEnd = new Point2D(event.getX(), event.getY());
        if (event.isControlDown()) {
            this.scale = (512 - this.scalePadding.getValue()) / (this.mouseEnd.getX()- this.mouseStart.getX());
        } else if (event.isAltDown()) {
            this.scale = 1;
        }
    }

    @Override
    public void handleToolEnter(MouseEvent event, Canvas toolCanvas) {
        toolCanvas.getScene().setCursor(Cursor.CROSSHAIR);
    }

    @Override
    public void handleToolLeave(MouseEvent event, Canvas toolCanvas) {
        toolCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    public Rectangle2D getSelection() {
        var height = this.textHeight.getValue();
        var startX = mouseStart.getX();
        var startY = mouseStart.getY() - height;
        var width = mouseEnd.getX() - startX;
        return new Rectangle2D(startX * this.scale,
                startY * this.scale,
                width * this.scale,
                height * this.scale
        );
    }

    public double getScale() {
        return this.scale;
    }
}
