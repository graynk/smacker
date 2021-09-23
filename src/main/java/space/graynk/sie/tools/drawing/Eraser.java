package space.graynk.sie.tools.drawing;

import javafx.scene.input.MouseEvent;

public class Eraser extends DrawingTool {

    @Override
    public void handleDrag(MouseEvent event) {
        context.clearRect(event.getX() - radius, event.getY() - radius, diameter, diameter);
    }

}
