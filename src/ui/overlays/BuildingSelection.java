package ui.overlays;

import buildings.Building;
import gamestates.Play;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import static buildings.Building.getBuildingHeight;
import static buildings.Building.getBuildingWidth;
import static main.Game.TILE_SIZE;

public class BuildingSelection extends Overlay implements Serializable {

    private Play play;

    private int selectedBuildingType = -1;
    private int maxBuildingAmount = 1;

    private ArrayList<Rectangle> selectionBounds = new ArrayList<Rectangle>();

    public BuildingSelection(int x, int y, Play play) {
        super(OVERLAY_LARGE, x, y);
        this.play = play;

        int selectionSize = 192;
        int xStart = 0;
        int yStart = 0;
        for (int i = 0; i < 9 && i < maxBuildingAmount; i++) {
            selectionBounds.add(new Rectangle(xStart, yStart, selectionSize, (selectionSize)));
            xStart += selectionSize;
            if (xStart >= selectionSize * 3) {
                xStart = 0;
                yStart += selectionSize;
            }
        }
    }

    public void render(Graphics g) {
        int offset = 8;
        int maxSpriteSize = TILE_SIZE * 3;
        for (int i = 0; i < selectionBounds.size(); i++) {
            Rectangle currBounds = selectionBounds.get(i);
            float scale = maxSpriteSize / Math.max((float) getBuildingWidth(selectedBuildingType) * TILE_SIZE, (float) getBuildingHeight(selectedBuildingType) * TILE_SIZE);
            // Debugging - All farms
            g.drawImage(Building.getBuildingSprite(0), currBounds.x + offset, currBounds.y, + offset, )
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed())
                play.setShowBuildingSelection(false);
        exButton.reset(x, y);
    }
}
