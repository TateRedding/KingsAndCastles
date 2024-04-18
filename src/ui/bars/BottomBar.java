package ui.bars;

import static main.Game.GAME_AREA_HEIGHT;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;
import static ui.buttons.Button.TEXT_SMALL;
import static objects.Tile.GRASS;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import gamestates.GameStates;
import main.Game;
import ui.buttons.TextButton;
import utils.ImageLoader;

public abstract class BottomBar extends UIBar {

	public final static int BOTTOM_BAR_Y = TOP_BAR_HEIGHT + GAME_AREA_HEIGHT;
	public final static int BOTTOM_BAR_HEIGHT = 160;

	protected TextButton menu;

	public BottomBar() {
		bounds = new Rectangle(X, BOTTOM_BAR_Y, UI_WIDTH, BOTTOM_BAR_HEIGHT);
		menu = new TextButton(TEXT_SMALL, "Menu", 28f, X + 15, BOTTOM_BAR_Y + 15);
	}

	public void update() {
		menu.update();
	}

	public void render(Graphics g) {
		g.drawImage(ImageLoader.terrainTiles.get(GRASS).get(0), X, BOTTOM_BAR_Y, Game.SCREEN_WIDTH, BOTTOM_BAR_HEIGHT,
				null);
		g.drawImage(ImageLoader.bottomBar, X, BOTTOM_BAR_Y, null);
		menu.render(g);
	}

	public void mousePressed(int x, int y, int button) {
		if (button == MouseEvent.BUTTON1)
			if (menu.getBounds().contains(x, y))
				menu.setMousePressed(true);
	}

	public void mouseReleased(int x, int y, int button) {
		if (button == MouseEvent.BUTTON1)
			if (menu.getBounds().contains(x, y) && menu.isMousePressed())
				GameStates.setGameState(GameStates.MENU);
		menu.reset();
	}

	public void mouseMoved(int x, int y) {
		menu.setMouseOver(false);
		if (menu.getBounds().contains(x, y))
			menu.setMouseOver(true);
	}

}
