package ui.bars;

import static objects.Tile.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import gamestates.Edit;
import objects.Map;
import utils.LoadSave;
import utils.RenderText;

public class MapStatBar extends TopBar {

	private Edit edit;
	private Map map;

	private int numCols = 6;
	private int offset = 16;
	private float colWidth = (UI_WIDTH - offset * 2) / numCols;
	private int colHeight = TOP_BAR_HEIGHT - offset * 2;
	private int textY = TOP_BAR_Y + offset;
	private boolean showCastleZoneWarning;

	public MapStatBar(Edit edit) {
		this.edit = edit;
		this.map = edit.getMap();
	}

	public void update() {

	}

	@Override
	public void render(Graphics g) {
		super.render(g);
		renderTileStats(g);
		renderCastleZoneStats(g);

		if (showCastleZoneWarning) {
			g.setColor(Color.RED);
			g.setFont(g.getFont().deriveFont(32f));
			String[] warning = { "Castle Zones must be", "the same number of tiles", "for every player!" };
			int textAreaX = X + offset + (int) (colWidth * 2);
			RenderText.renderText(g, warning, RenderText.CENTER, textAreaX, textY, (int) colWidth, colHeight);
		}
	}

	private void renderTileStats(Graphics g) {
		int textX = X + offset;
		int[] tileCounts = map.getTileCounts();
		String[][] tileStats = { { "Grass: ", "" + tileCounts[GRASS] }, { "Dirt: ", "" + tileCounts[DIRT] },
				{ "Sand: ", "" + tileCounts[SAND] },
				{ "Water: ", "" + (tileCounts[WATER_GRASS] + tileCounts[WATER_SAND]) },
				{ "Gold Mines: ", "" + map.getGoldMinePoints().size() } };
		g.setColor(Color.BLACK);
		Font font = LoadSave.getGameFont(Font.BOLD, 28f);
		RenderText.renderTextBoxed(g, tileStats, font, font, textX, textY, (int) colWidth, colHeight);
	}

	private void renderCastleZoneStats(Graphics g) {
		int textX = X + offset + (int) colWidth;
		int numPlayers = map.getNumPlayers();
		String[][] castelZoneStats = new String[numPlayers + 1][2];
		castelZoneStats[0][0] = "Castle ";
		castelZoneStats[0][1] = "Zones";
		for (int i = 0; i < numPlayers; i++) {
			castelZoneStats[i + 1][0] = "P" + (i + 1) + ": ";
			castelZoneStats[i + 1][1] = "" + map.getCastleZones().get(i).size();
		}
		Font font = LoadSave.getGameFont(Font.BOLD, 34f);
		RenderText.renderTextSplitCentered(g, castelZoneStats, font, font, textX, textY, (int) colWidth, colHeight);
	}

	public void mousePressed(int x, int y, int button) {

	}

	public void mouseReleased(int x, int y, int button) {

	}

	public void mouseMoved(int x, int y) {

	}

	public Edit getEdit() {
		return edit;
	}

	public void setShowCastleZoneWarning(boolean showCastleZoneWarning) {
		this.showCastleZoneWarning = showCastleZoneWarning;
	}

}
