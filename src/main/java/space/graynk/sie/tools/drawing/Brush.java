package space.graynk.sie.tools.drawing;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class Brush extends DrawingTool {

    @Override
    public void handleDragStart(MouseEvent event, Canvas canvas) {
        super.handleDragStart(event, canvas);
        canvas.getScene().setCursor(Cursor.CROSSHAIR);
        context.beginPath();
        context.setLineWidth(diameter);
        context.fillOval(event.getX() - radius, event.getY() - radius, diameter, diameter);
        context.moveTo(event.getX(), event.getY());
    }

    @Override
    public void handleDrag(MouseEvent event) {
        canvas.getScene().setCursor(Cursor.CROSSHAIR);
        context.lineTo(event.getX(), event.getY());
        context.stroke();
    }

    @Override
    public void handleDragEnd(MouseEvent event) {
        canvas.getScene().setCursor(Cursor.DEFAULT);
        context.lineTo(event.getX(), event.getY());
    }
}
