package ui.overlays;

import gamestates.Play;

import java.awt.event.MouseEvent;
import java.io.Serializable;

public class BuildingSelection extends Overlay implements Serializable {

    private Play play;

    private int selectedBuildingType = -1;

    public BuildingSelection(int x, int y, Play play) {
        super(OVERLAY_LARGE, x, y);
        this.play = play;
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed())
                play.setShowBuildingSelection(false);
        exButton.reset(x, y);
    }
}
