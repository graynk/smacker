package space.graynk.sie.tools;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public abstract class Tool {
    protected Canvas canvas;
    protected GraphicsContext context;

    public void handleDragStart(MouseEvent event, Canvas canvas) {
        this.canvas = canvas;
        this.context = canvas.getGraphicsContext2D();
    }

    public void handleDrag(MouseEvent event) {

    }
    public void handleDragEnd(MouseEvent event) {

    }

    public void handleToolEnter(MouseEvent event, Canvas toolCanvas) {}
    public void handleToolLeave(MouseEvent event, Canvas toolCanvas) {}
}
