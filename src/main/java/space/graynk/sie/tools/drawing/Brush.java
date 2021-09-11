package space.graynk.sie.tools.drawing;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import space.graynk.sie.tools.Tool;

public class Brush extends Tool {

    @Override
    public void handleDragStart(MouseEvent event, Canvas canvas) {
        super.handleDragStart(event, canvas);
        canvas.getScene().setCursor(Cursor.CROSSHAIR);
        var radius = 4;
        context.beginPath();
        context.setLineWidth(radius*2);
        context.fillOval(event.getX() - radius, event.getY() - radius, radius * 2, radius * 2);
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
