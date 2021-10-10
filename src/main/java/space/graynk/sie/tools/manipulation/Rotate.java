package space.graynk.sie.tools.manipulation;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import space.graynk.sie.tools.Tool;

public class Rotate extends Tool {
    @Override
    public void handleDrag(MouseEvent event) {
        event.consume();
        if (!event.isSecondaryButtonDown()) {
            return;
        }
        var angle = Math.atan2(
                event.getY() - canvas.getHeight(),
                event.getX() - canvas.getWidth()
        );
//            WritableImage writableImage = new WritableImage((int)mainCanvas.getWidth(), (int)mainCanvas.getHeight());
        var writableImage = canvas.snapshot(null, null);
//            context.save();
        context.setFill(Color.WHITE);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//            context.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        javafx.scene.transform.Rotate r = new javafx.scene.transform.Rotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
//        context.setImageSmoothing(false);

        context.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
//            context.rotate(1);
//            context.translate(mainCanvas.getWidth() / 2, 0);
        context.drawImage(writableImage, 0, 0);
//            context.restore();
    }

}
