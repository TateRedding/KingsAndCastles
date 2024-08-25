package ui.overlays;

import gamestates.Play;
import main.Game;
import utils.ImageLoader;
import utils.RenderText;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import static buildings.Building.*;
import static main.Game.TILE_SIZE;
import static ui.bars.UIBar.*;

public class BuildingSelection extends Overlay {

    private Play play;
    private int maxBuildingAmount = 8;
    private int maxStartIndex;
    private int maxSelectionAreas = 9;
    private int numColumns = 3;

    private int selectedBuildingType = -1, hoverIndex = -1, startIndex = 0;


    private ArrayList<Rectangle> selectionBounds = new ArrayList<Rectangle>();

    public BuildingSelection(int x, int y, Play play) {
        super(OVERLAY_LARGE, x, y);
        this.play = play;
        if (maxBuildingAmount > maxSelectionAreas) {
            this.maxStartIndex = (int) (Math.ceil((float) (maxBuildingAmount - maxSelectionAreas) / numColumns));
        } else
            this.maxStartIndex = 0;
        int selectionSize = 192;
        int xStart = x;
        int yStart = y;
        for (int i = 0; i < maxSelectionAreas && i < Math.max(maxBuildingAmount, maxSelectionAreas); i++) {
            selectionBounds.add(new Rectangle(xStart, yStart, selectionSize, (selectionSize)));
            xStart += selectionSize;
            if (xStart >= selectionSize * numColumns) {
                xStart = x;
                yStart += selectionSize;
            }
        }
    }

    public void render(Graphics g) {
        super.render(g);

        renderOptions(g);
        if (maxBuildingAmount > maxSelectionAreas)
            renderScrollBar(g);
        if (hoverIndex != -1) {
            Rectangle currRowBounds = selectionBounds.get(hoverIndex);
            g.setColor(new Color(0, 0, 0, 75));
            g.fillRect(currRowBounds.x, currRowBounds.y, currRowBounds.width, currRowBounds.height);
        }
    }

    private void renderOptions(Graphics g) {
        int margin = 8;
        int maxSpriteSize = TILE_SIZE * 3;

        for (int i = 0; i < selectionBounds.size() && i < maxBuildingAmount - startIndex; i++) {
            int currBuildingType = i + startIndex;
            Rectangle currBounds = selectionBounds.get(i);
            int tileWidth = getBuildingTileWidth(currBuildingType);
            int tileHeight = getBuildingTileHeight(currBuildingType);

            float scale = maxSpriteSize / Math.max((float) tileWidth * TILE_SIZE, (float) tileHeight * TILE_SIZE);

            int width = (int) ((float) (tileWidth * TILE_SIZE) * scale);
            int height = (int) ((float) (tileHeight * TILE_SIZE) * scale);

            // Debugging
            g.drawImage(ImageLoader.buildings[currBuildingType], currBounds.x + margin, currBounds.y + margin, width, height, null);

            renderCost(g, currBuildingType, selectionBounds.get(i), margin, maxSpriteSize);

        }
    }

    private void renderScrollBar(Graphics g) {
        int yOffset = 16;
        int xOffset = 16;
        int containerWidth = 16;
        int buttonHeight = exButton.getBounds().height;
        int totalHeight = Overlay.getOverlayHeight(size) - buttonHeight * 3;
        int yStart = y + buttonHeight + yOffset * 2;
        int xStart = x + Overlay.getOverlayWidth(size) - containerWidth - xOffset;

        float scrollHeight = totalHeight;
        int numRows = (int) (Math.ceil((float) maxBuildingAmount / numColumns));
        int maxShownRows = maxSelectionAreas / numColumns;
        if (numRows > maxShownRows)
            scrollHeight /= (float) numRows;

        int barHeight = (int) (scrollHeight * maxShownRows);
        int barOffset = 1;
        if (barHeight > totalHeight)
            barHeight = totalHeight;

        g.setColor(new Color(64, 27, 0));
        g.fillRect(xStart, yStart, containerWidth, totalHeight);

        g.setColor(new Color(230, 127, 92));
        int barYStart = yStart + barOffset + (int) (((float) startIndex / numColumns) * scrollHeight);
        if (startIndex == maxStartIndex) {
            barYStart = yStart + totalHeight - barHeight + barOffset;
        }
        g.fillRect(xStart + barOffset, barYStart, containerWidth - barOffset * 2, barHeight - barOffset * 2);
    }

    private void renderCost(Graphics g, int buildingType, Rectangle bounds, int margin, int spriteSize) {
        ArrayList<BufferedImage> icons = new ArrayList<>(Arrays.asList(
                ImageLoader.icons[ICON_GOLD],
                ImageLoader.icons[ICON_LOG],
                ImageLoader.icons[ICON_STONE],
                ImageLoader.icons[ICON_IRON],
                ImageLoader.icons[ICON_COAL]
        ));

        ArrayList<Integer> costs = new ArrayList<>(Arrays.asList(
                getCostGold(buildingType),
                getCostLogs(buildingType),
                getCostStone(buildingType),
                getCostIron(buildingType),
                getCostCoal(buildingType)
        ));
        int maxDisplay = Math.min(icons.size(), costs.size());
        int textXOffset = 2;
        int xOffset = 8;
        g.setFont(Game.getGameFont(20f));
        g.setColor(Color.BLACK);
        for (int i = 0; i < maxDisplay; i++) {
            int cost = costs.get(i);
            if (cost <= 0)
                continue;
            BufferedImage icon = icons.get(i);
            int iconWidth = (int) (icon.getWidth());
            int iconHeight = (int) (icon.getHeight());
            int iconX = bounds.x + margin + spriteSize + xOffset;
            int iconY = bounds.y + margin + (iconHeight * i);
            int textX = iconX + iconWidth + textXOffset;
            g.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
            RenderText.renderText(g, String.valueOf(cost), RenderText.LEFT, RenderText.CENTER, textX, iconY, g.getFontMetrics().stringWidth(String.valueOf(cost)), iconHeight);
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                play.setShowBuildingSelection(false);
                startIndex = 0;
            }
            for (Rectangle selectionBounds : selectionBounds)
                if (selectionBounds.contains(x, y) && hoverIndex != -1)
                    selectedBuildingType = hoverIndex + startIndex;
        }
        exButton.reset(x, y);
    }

    public void mouseMoved(int x, int y) {
        hoverIndex = -1;
        for (int i = 0; i < selectionBounds.size(); i++)
            if (selectionBounds.get(i).contains(x, y))
                hoverIndex = i;

    }

    public void mouseWheelMoved(int dir, int amt) {
        if (dir == -1) {
            if (startIndex > 0)
                startIndex -= 3;
        } else if (startIndex < maxStartIndex)
            startIndex += 3;

    }
}
