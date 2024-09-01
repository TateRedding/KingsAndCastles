package ui.overlays;

import gamestates.Play;
import main.Game;
import ui.buttons.TextButton;
import utils.ImageLoader;
import utils.RenderText;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import static entities.buildings.Building.*;
import static main.Game.TILE_SIZE;
import static main.Game.getGameFont;
import static ui.bars.UIBar.*;
import static ui.buttons.Button.*;

public class BuildingSelection extends Overlay {

    private static final int DISPLAY_COLS = 3;
    private static final int DISPLAY_ROWS = 3;
    private static final int MAX_SELECTION_SPRITE_SIZE = TILE_SIZE * 3;
    private static final int MAX_DETAILS_SPRITE_SIZE = TILE_SIZE * 4;
    private static final int MARGIN = 12;
    private static final int PADDING = 8;
    private static final int SCROLL_BAR_AREA_HEIGHT = 24;
    private static final int SELECTION_AREA_WIDTH = 192;
    private static final float SELECTION_AREA_HEIGHT = ((float) (Overlay.getOverlayHeight(OVERLAY_LARGE) - MARGIN * 2) / DISPLAY_ROWS) - SCROLL_BAR_AREA_HEIGHT;

    private Play play;

    private TextButton selectButton;
    private ArrayList<Rectangle>[] selectionBounds;
    private ArrayList<Integer>[] buildingArrays;
    private int[] startingIndecies, maxStartingIndecies;

    private int selectedBuildingType = -1, hoverCol = -1, hoverRow = -1;
    private int detailsAreaXStart;
    private int detailsAreaWidth;

    private boolean showCostWarning;

    public BuildingSelection(int x, int y, Play play) {
        super(OVERLAY_LARGE, x, y);
        this.play = play;

        this.buildingArrays = new ArrayList[DISPLAY_ROWS];
        this.selectionBounds = new ArrayList[DISPLAY_ROWS];
        this.startingIndecies = new int[DISPLAY_ROWS];
        this.maxStartingIndecies = new int[DISPLAY_ROWS];
        this.buildingArrays[0] = new ArrayList<>(Arrays.asList(CASTLE_WALL, CASTLE_TURRET));
        this.buildingArrays[1] = new ArrayList<>(Arrays.asList(VILLAGE, FARM, FARM_ROTATED, STORAGE_HUT, REFINERY));
        this.buildingArrays[2] = new ArrayList<>(Arrays.asList(BARRACKS_TIER_1, BARRACKS_TIER_2, BARRACKS_TIER_3));

        for (int row = 0; row < DISPLAY_ROWS; row++) {
            selectionBounds[row] = new ArrayList<>();
            startingIndecies[row] = 0;
            maxStartingIndecies[row] = Math.max(0, buildingArrays[row].size() - DISPLAY_ROWS);
            for (int col = 0; col < buildingArrays[row].size() && col < DISPLAY_COLS; col++) {
                int xStart = x + MARGIN + (col * SELECTION_AREA_WIDTH);
                int yStart = y + MARGIN + (int) ((row * SELECTION_AREA_HEIGHT)) + (row * SCROLL_BAR_AREA_HEIGHT);
                selectionBounds[row].add(new Rectangle(xStart, yStart, SELECTION_AREA_WIDTH, (int) SELECTION_AREA_HEIGHT));
            }
        }

        this.detailsAreaXStart = x + MARGIN * 2 + (SELECTION_AREA_WIDTH * DISPLAY_COLS);
        this.detailsAreaWidth = Overlay.getOverlayWidth(OVERLAY_LARGE) - MARGIN * 3 - (SELECTION_AREA_WIDTH * DISPLAY_COLS);

        int buttonXStart = detailsAreaXStart + ((detailsAreaWidth - getButtonWidth(TEXT_LARGE)) / 2);
        int buttonYStart = y + Overlay.getOverlayHeight(OVERLAY_LARGE) - getButtonHeight(TEXT_LARGE) - MARGIN;
        this.selectButton = new TextButton(TEXT_LARGE, buttonXStart, buttonYStart, 48f, "Select");
        this.selectButton.setDisabled(true);
    }

    @Override
    public void update() {
        super.update();
        selectButton.update();
    }

    public void render(Graphics g) {
        super.render(g);
        selectButton.render(g);

        renderOptions(g);
        if (hoverCol != -1 && hoverRow != -1) {
            Rectangle currRowBounds = selectionBounds[hoverRow].get(hoverCol);
            g.setColor(new Color(0, 0, 0, 75));
            g.fillRect(currRowBounds.x, currRowBounds.y, currRowBounds.width, currRowBounds.height);
        }

        renderBuildingDetails(g);
    }

    private void renderOptions(Graphics g) {
        for (int row = 0; row < buildingArrays.length; row++) {
            if (row < DISPLAY_ROWS - 1) {
                g.setColor(new Color(64, 27, 0));
                int horDivYStart = y + MARGIN + (int) (SELECTION_AREA_HEIGHT + SCROLL_BAR_AREA_HEIGHT) * (row + 1);
                g.drawRect(x + MARGIN, horDivYStart, SELECTION_AREA_WIDTH * DISPLAY_COLS, 1);
            }
            if (buildingArrays[row].size() > DISPLAY_COLS)
                renderScrollBar(g, row);
            for (int col = 0; col < Math.min(buildingArrays[row].size(), DISPLAY_COLS); col++) {
                Rectangle currBounds = selectionBounds[row].get(col);

                if (col < DISPLAY_COLS - 1) {
                    int vertDivHeight = currBounds.height - SCROLL_BAR_AREA_HEIGHT;
                    int vertDivYStart = currBounds.y + SCROLL_BAR_AREA_HEIGHT;
                    g.setColor(new Color(64, 27, 0));
                    g.drawRect(currBounds.x + currBounds.width, vertDivYStart, 1, vertDivHeight);
                }

                int currBuildingType = buildingArrays[row].get(col + startingIndecies[row]);
                int tileWidth = getBuildingTileWidth(currBuildingType);
                int tileHeight = getBuildingTileHeight(currBuildingType);
                float scale = MAX_SELECTION_SPRITE_SIZE / Math.max((float) tileWidth * TILE_SIZE, (float) tileHeight * TILE_SIZE);

                int width = (int) ((float) (tileWidth * TILE_SIZE) * scale);
                int height = (int) ((float) (tileHeight * TILE_SIZE) * scale);

                int xOffset = 0;
                int yOffset = 0;
                if (width < MAX_SELECTION_SPRITE_SIZE)
                    xOffset += (MAX_SELECTION_SPRITE_SIZE - width) / 2;
                if (height < MAX_SELECTION_SPRITE_SIZE)
                    yOffset += (MAX_SELECTION_SPRITE_SIZE - height) / 2;

                g.drawImage(ImageLoader.buildings[currBuildingType], currBounds.x + PADDING + xOffset, currBounds.y + PADDING + yOffset, width, height, null);

                renderCost(g, currBuildingType, selectionBounds[row].get(col));
                renderName(g, currBuildingType, selectionBounds[row].get(col));
            }
        }
    }

    private void renderScrollBar(Graphics g, int row) {
        int xOffset = 16;
        int barHeight = 8;
        int yOffset = (SCROLL_BAR_AREA_HEIGHT - barHeight) / 2;
        int totalWidth = SELECTION_AREA_WIDTH * DISPLAY_ROWS - (xOffset * 2);
        int yStart = y + MARGIN + (int) (SELECTION_AREA_HEIGHT * (row + 1)) + (SCROLL_BAR_AREA_HEIGHT * row) + yOffset;
        int xStart = x + xOffset;

        float scrollWidth = totalWidth / (float) buildingArrays[row].size();
        int barWidth = (int) (scrollWidth * DISPLAY_COLS);
        int barOffset = 1;

        g.setColor(new Color(64, 27, 0));
        g.fillRect(xStart, yStart, totalWidth, barHeight);

        g.setColor(new Color(230, 127, 92));
        int barXStart = xStart + barOffset + (int) (scrollWidth * startingIndecies[row]);
        if (startingIndecies[row] == maxStartingIndecies[row])
            barXStart = xStart + totalWidth - barWidth + barOffset;
        g.fillRect(barXStart, yStart + barOffset, barWidth - barOffset * 2, barHeight - barOffset * 2);
    }

    private void renderCost(Graphics g, int buildingType, Rectangle bounds) {
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
            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();
            int iconX = bounds.x + PADDING + MAX_SELECTION_SPRITE_SIZE + xOffset;
            int iconY = bounds.y + PADDING + (iconHeight * i);
            int textX = iconX + iconWidth + textXOffset;
            g.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
            RenderText.renderText(g, String.valueOf(cost), RenderText.LEFT, RenderText.CENTER, textX, iconY, g.getFontMetrics().stringWidth(String.valueOf(cost)), iconHeight);
        }
    }

    private void renderName(Graphics g, int buildingType, Rectangle bounds) {
        int yStart = bounds.y + PADDING + MAX_SELECTION_SPRITE_SIZE;
        int height = bounds.height - PADDING - MAX_SELECTION_SPRITE_SIZE;
        String[] name = getBuildingName(buildingType).split(" - ");
        g.setFont(getGameFont(name.length == 1 ? 36f : 28f));

        RenderText.renderText(g, name, RenderText.CENTER, RenderText.CENTER, bounds.x, yStart, bounds.width, height);
    }

    private void renderBuildingDetails(Graphics g) {
        // Setting name to an array of one ensures the text size will be adjusted to fit
        String[] name = new String[]{""};
        String[] text = {"Select a building", "for more details"};
        if (selectedBuildingType != -1) {
            text = getBuildingDetails(selectedBuildingType);
            name[0] = getBuildingName(selectedBuildingType);
        }
        int yNameOffset = MARGIN + exButton.getBounds().height;
        int yStart = y + yNameOffset;

        // Building name
        g.setColor(Color.BLACK);
        g.setFont(getGameFont(52f));
        int nameHeight = g.getFontMetrics().getHeight();
        RenderText.renderText(g, name, RenderText.CENTER, RenderText.TOP, detailsAreaXStart, yStart, detailsAreaWidth, nameHeight);

        // Building sprite
        yStart += nameHeight + PADDING;
        if (selectedBuildingType != -1) {
            int tileWidth = getBuildingTileWidth(selectedBuildingType);
            int tileHeight = getBuildingTileHeight(selectedBuildingType);
            float scale = MAX_DETAILS_SPRITE_SIZE / Math.max((float) tileWidth * TILE_SIZE, (float) tileHeight * TILE_SIZE);

            int spriteWidth = (int) ((float) (tileWidth * TILE_SIZE) * scale);
            int spriteHeight = (int) ((float) (tileHeight * TILE_SIZE) * scale);
            int xSpriteOffset = (detailsAreaWidth - spriteWidth) / 2;
            int ySpriteOffset = 0;
            if (spriteHeight < MAX_DETAILS_SPRITE_SIZE)
                ySpriteOffset += (MAX_DETAILS_SPRITE_SIZE - spriteHeight) / 2;

            g.drawImage(ImageLoader.buildings[selectedBuildingType], detailsAreaXStart + xSpriteOffset, yStart + ySpriteOffset, spriteWidth, spriteHeight, null);
        }

        // Cost Warning
        int costWarningHeight = g.getFontMetrics().getHeight();
        if (showCostWarning) {
            g.setFont(getGameFont(28f));
            g.setColor(Color.RED);
            int costWarningYStart = selectButton.getBounds().y - costWarningHeight;
            RenderText.renderText(g, "Not enough entities.resources!", RenderText.CENTER, RenderText.CENTER, detailsAreaXStart, costWarningYStart, detailsAreaWidth, costWarningHeight);
        }

        // Building detail text
        yStart += MAX_DETAILS_SPRITE_SIZE;
        g.setFont(getGameFont(28f));
        g.setColor(Color.BLACK);
        if (selectedBuildingType != -1) {
            int detailTextHeight = (selectButton.getBounds().y - costWarningHeight) - yStart;
            RenderText.renderText(g, text, RenderText.CENTER, RenderText.CENTER, detailsAreaXStart, yStart, detailsAreaWidth, detailTextHeight);
        } else
            RenderText.renderText(g, text, RenderText.CENTER, RenderText.CENTER, detailsAreaXStart, y, detailsAreaWidth, Overlay.getOverlayHeight(OVERLAY_LARGE));
    }

    private void closeOverlay() {
        play.setShowBuildingSelection(false);
        Arrays.fill(startingIndecies, 0);
        selectedBuildingType = -1;
        selectButton.setDisabled(true);
        showCostWarning = false;
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (selectButton.getBounds().contains(x, y))
                selectButton.setMousePressed(true);
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed())
                closeOverlay();
            else if (selectButton.getBounds().contains(x, y) && selectButton.isMousePressed() && selectedBuildingType != -1) {
                play.setSelectedBuildingType(selectedBuildingType);
                play.getActionBar().setSelectedBuildingButtonType(selectedBuildingType);
                closeOverlay();
            } else
                for (int row = 0; row < selectionBounds.length; row++)
                    for (int col = 0; col < selectionBounds[row].size(); col++)
                        if (selectionBounds[row].get(col).contains(x, y)) {
                            selectedBuildingType = buildingArrays[row].get(col + startingIndecies[row]);
                            showCostWarning = !play.canAffordBuilding(selectedBuildingType);
                            selectButton.setDisabled(showCostWarning);
                        }
        }
        exButton.reset(x, y);
        selectButton.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        selectButton.setMouseOver(false);
        if (selectButton.getBounds().contains(x, y))
            selectButton.setMouseOver(true);

        hoverRow = -1;
        hoverCol = -1;
        for (int row = 0; row < selectionBounds.length; row++)
            for (int col = 0; col < selectionBounds[row].size(); col++)
                if (selectionBounds[row].get(col).contains(x, y)) {
                    hoverRow = row;
                    hoverCol = col;
                }

    }

    public void mouseWheelMoved(int dir, int amt) {
        if (hoverRow != -1)
            if (dir == 1) {
                if (startingIndecies[hoverRow] > 0)
                    startingIndecies[hoverRow]--;
            } else if (startingIndecies[hoverRow] < maxStartingIndecies[hoverRow])
                startingIndecies[hoverRow]++;

    }
}
