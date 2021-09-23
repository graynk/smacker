package space.graynk.sie.tools.drawing;

import space.graynk.sie.tools.Tool;

public abstract class DrawingTool extends Tool {
    protected int radius = 4;
    protected int diameter = radius << 1;

    public void setRadius(int radius) {
        this.radius = radius;
        this.diameter = radius << 1;
    }
}
