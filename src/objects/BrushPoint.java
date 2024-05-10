package objects;

import java.awt.*;

public class BrushPoint extends Point {

    private boolean isEdge;

    public BrushPoint(int x, int y) {
        super(x, y);
        this.isEdge = false;
    }

    public BrushPoint(int x, int y, boolean isEdge) {
        super(x, y);
        this.isEdge = isEdge;
    }

    public boolean isEdge() {
        return isEdge;
    }

    public void setIsEdge(boolean isEdge) {
        this.isEdge = isEdge;
    }
}
