package space.graynk.sie.tools.manipulation;

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
    private double selectionHeight;
    private Canvas canvas;
    private GraphicsContext context;

    @Override
    public void handleDragStart(MouseEvent event, Canvas canvas) {
        this.mouseStart = new Point2D(event.getX(), event.getY());
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
        this.context.setLineDashes(20, 10);
    }

    @Override
    public void handleDrag(MouseEvent event) {
        super.handleDrag(event);
        var startX = mouseStart.getX();
        var startY = mouseStart.getY() - selectionHeight;
        var width = event.getX() - startX;
        var height = selectionHeight;

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
    }

    @Override
    public void handleToolEnter(MouseEvent event, Canvas toolCanvas) {
        toolCanvas.getScene().setCursor(Cursor.CROSSHAIR);
    }

    @Override
    public void handleToolLeave(MouseEvent event, Canvas toolCanvas) {
        toolCanvas.getScene().setCursor(Cursor.DEFAULT);
    }

    public void setSelectionHeight(double selectionHeight) {
        this.selectionHeight = selectionHeight;
    }

    public Rectangle2D getSelection() {
        var startX = mouseStart.getX();
        var startY = mouseStart.getY() - selectionHeight;
        var width = mouseEnd.getX() - startX;
        var height = selectionHeight;
        return new Rectangle2D(startX, startY, width, height);
    }
}
