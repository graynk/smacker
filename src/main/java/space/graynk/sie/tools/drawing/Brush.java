package space.graynk.sie.tools.drawing;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class Brush extends DrawingTool {

    @Override
    public void handleDragStart(MouseEvent event, Canvas canvas) {
        super.handleDragStart(event, canvas);
        context.beginPath();
        context.setLineWidth(diameter);
        context.fillOval(event.getX() - radius, event.getY() - radius, diameter, diameter);
        context.moveTo(event.getX(), event.getY());
    }

    @Override
    public void handleDrag(MouseEvent event) {
        context.lineTo(event.getX(), event.getY());
        context.stroke();
    }

    @Override
    public void handleDragEnd(MouseEvent event) {
        context.lineTo(event.getX(), event.getY());
    }

    @Override
    public void handleToolEnter(MouseEvent event, Canvas toolCanvas) {
        var cursor = new WritableImage(diameter+1, diameter+1);
        for (var x = 0; x <= diameter; x++) {
            var offsetX = x - radius;
            var y = (int)Math.sqrt(radius*radius - offsetX*offsetX);
            cursor.getPixelWriter().setColor(x, radius + y, Color.BLACK);
            cursor.getPixelWriter().setColor(x, radius - y, Color.BLACK);
        }
        var realcursor = new ImageCursor(cursor);
    }

    @Override
    public void handleToolLeave(MouseEvent event, Canvas toolCanvas) {
        toolCanvas.getScene().setCursor(Cursor.DEFAULT);
    }
}
