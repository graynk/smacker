package space.graynk.sie.gui;

import javafx.scene.control.ToggleButton;
import space.graynk.sie.tools.Tool;
import space.graynk.sie.tools.ToolType;
import space.graynk.sie.tools.drawing.Brush;
import space.graynk.sie.tools.drawing.Bucket;
import space.graynk.sie.tools.drawing.Eraser;
import space.graynk.sie.tools.drawing.Pencil;
import space.graynk.sie.tools.drawing.Text;
import space.graynk.sie.tools.manipulation.Move;
import space.graynk.sie.tools.manipulation.Rotate;
import space.graynk.sie.tools.manipulation.Select;

public class ToolButton extends ToggleButton {
    private ToolType toolType;
    private Tool tool;

    public ToolType getToolType() {
        return toolType;
    }

    public Tool getTool() {
        return this.tool;
    }

    public void setToolType(ToolType toolType) {
        this.toolType = toolType;
        switch (toolType) {
            case BRUSH:
                this.tool = new Brush();
                break;
            case BUCKET:
                this.tool = new Bucket();
                break;
            case PENCIL:
                this.tool = new Pencil();
                break;
            case MOVE:
                this.tool = new Move();
                break;
            case ERASER:
                this.tool = new Eraser();
                break;
            case COLOR_PICKER:
                break;
            case DRAW_LINE:
                break;
            case DRAW_RECTANGLE:
                break;
            case DRAW_ROUNDED_RECTANGLE:
                break;
            case DRAW_OVAL:
                break;
            case TEXT:
                this.tool = new Text();
                break;
            case SELECT:
                this.tool = new Select();
                break;
            case ROTATE:
                this.tool = new Rotate();
                break;
        }
    }
}
