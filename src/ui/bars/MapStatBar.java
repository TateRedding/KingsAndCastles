package ui.bars;

import gamestates.Edit;
import main.Game;
import objects.Map;
import utils.RenderText;

import java.awt.*;

import static objects.Tile.*;

public class MapStatBar extends TopBar {

    private Edit edit;
    private Map map;

    private String[][] toolTips;
    private String defaultToolTip;
    private int numCols = 6;
    private int offset = 8;
    private float colWidth = (UI_WIDTH - offset * 2) / numCols;
    private int headerY = TOP_BAR_Y + offset;
    private int headerHeight = 24;
    private int statHeight = TOP_BAR_HEIGHT - offset * 2 - headerHeight;
    private float headerFontSize = 32f;
    private float statFontSize = 28f;

    public MapStatBar(Edit edit) {
        this.edit = edit;
        this.map = edit.getMap();

        this.defaultToolTip = "Drag/ Arrow Keys: Move map";
        String[] terrainToolTip = {"Left-click: Place tile", "Esc: Unselect", "Scroll: Adjust brush size", "Right-click & Drag: Move Map"};
        this.toolTips = new String[][]{
                terrainToolTip,
                terrainToolTip,
                terrainToolTip,
                terrainToolTip,
                terrainToolTip,
                {"Left-click: Place zone", "Right-click: Delete zone", "Scroll: Switch player", "Esc: Unselect"},
                {"Left-click: Place mine", "Right-click: Delete mine", "Esc: Unselect", "Right-click & Drag: Move Map"},
                {"Left-click: Place Throne Room", "Right-click: Delete Throne Room", "Scroll: Switch player", "Esc: Unselect"},
        };
    }

    public void update() {

    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        g.setColor(Color.BLACK);
        renderTileStats(g);
        renderCastleZoneStats(g);
        renderResourceStats(g);
        renderToolTips(g);
    }

    private void renderTileStats(Graphics g) {
        g.setFont(Game.getGameFont(headerFontSize));
        int xStart = X + offset;
        RenderText.renderText(g, "Terrain", RenderText.CENTER, RenderText.CENTER, xStart, headerY, (int) colWidth, headerHeight);

        int[] tileCounts = map.getTileCounts();
        String[] tileStats = {
                "Grass: " + tileCounts[GRASS],
                "Dirt: " + tileCounts[DIRT],
                "Sand: " + tileCounts[SAND],
                "Water: " + (tileCounts[WATER_GRASS] + tileCounts[WATER_SAND])
        };
        g.setFont(Game.getGameFont(statFontSize));
        RenderText.renderText(g, tileStats, RenderText.CENTER, RenderText.TOP, xStart, headerY + headerHeight, (int) colWidth, statHeight);
    }

    private void renderCastleZoneStats(Graphics g) {
        g.setFont(Game.getGameFont(headerFontSize));
        int xStart = X + offset + (int) colWidth;
        RenderText.renderText(g, "Castle Zones", RenderText.CENTER, RenderText.CENTER, xStart, headerY, (int) colWidth, headerHeight);

        int numPlayers = map.getNumPlayers();
        String[] castleZoneStats = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++)
            castleZoneStats[i] = "P" + (i + 1) + ": " + map.getCastleZones().get(i).size();
        g.setFont(Game.getGameFont(statFontSize));
        RenderText.renderText(g, castleZoneStats, RenderText.CENTER, RenderText.TOP, xStart, headerY + headerHeight, (int) colWidth, statHeight);
    }

    private void renderResourceStats(Graphics g) {
        g.setFont(Game.getGameFont(headerFontSize));
        int xStart = X + offset + (int) (colWidth * 2);
        RenderText.renderText(g, "Resources", RenderText.CENTER, RenderText.CENTER, xStart, headerY, (int) colWidth, headerHeight);

        String resourceStats = "Gold Mines: " + map.getGoldMinePoints().size();
        g.setFont(Game.getGameFont(statFontSize));
        RenderText.renderText(g, resourceStats, RenderText.CENTER, RenderText.TOP, xStart, headerY + headerHeight, (int) colWidth, statHeight);
    }

    private void renderToolTips(Graphics g) {
        g.setFont(Game.getGameFont(headerFontSize));
        int xStart = X + offset + (int) (colWidth * 3);
        RenderText.renderText(g, "Controls", RenderText.CENTER, RenderText.CENTER, xStart, headerY, (int) colWidth, headerHeight);

        int selectedType = edit.getSelectedType();
        g.setFont(Game.getGameFont(statFontSize));
        if (selectedType == -1)
            RenderText.renderText(g, defaultToolTip, RenderText.CENTER, RenderText.TOP, xStart, headerY + headerHeight, (int) colWidth, statHeight);
        else
            RenderText.renderText(g, toolTips[selectedType], RenderText.CENTER, RenderText.TOP, xStart, headerY + headerHeight, (int) colWidth, statHeight);
    }

    public void mousePressed(int x, int y, int button) {

    }

    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseMoved(int x, int y) {

    }

}
