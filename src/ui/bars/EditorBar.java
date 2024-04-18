package ui.bars;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.TEXT_SMALL;
import static ui.buttons.Button.TILE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import gamestates.Edit;
import ui.buttons.TextButton;
import ui.buttons.TileButton;
import utils.ImageLoader;
import utils.LoadSave;

public class EditorBar extends BottomBar {

	private Edit edit;
	private TextButton save;
	private ArrayList<TileButton> tileButtons = new ArrayList<TileButton>();

	public EditorBar(Edit edit) {
		this.edit = edit;
		Rectangle menuBounds = menu.getBounds();
		save = new TextButton(TEXT_SMALL, "Save", 28f, menuBounds.x, menuBounds.y + menuBounds.height + 5);
		initTileButtons();
	}

	private void initTileButtons() {
		int numButtons = ImageLoader.editorBarTileButtonImages.size();
		int xOffset = 15;
		int buttonWidth = getButtonWidth(TILE);
		int buttonHeight = getButtonHeight(TILE);
		int x = (SCREEN_WIDTH - (buttonWidth * numButtons + xOffset * (numButtons - 1))) / 2;
		int y = BOTTOM_BAR_Y + ((BOTTOM_BAR_HEIGHT - buttonHeight) / 2);

		for (int i = 0; i < ImageLoader.editorBarTileButtonImages.size(); i++) {
			tileButtons.add(new TileButton(ImageLoader.editorBarTileButtonImages.get(i), x, y));
			x += buttonWidth + xOffset;
		}
	}

	@Override
	public void update() {
		super.update();
		save.update();
		for (TileButton tb : tileButtons) {
			tb.update();
		}
	}

	@Override
	public void render(Graphics g) {
		super.render(g);
		save.render(g);
		for (TileButton tb : tileButtons) {
			tb.render(g);
		}
	}

	@Override
	public void mousePressed(int x, int y, int button) {
		super.mousePressed(x, y, button);
		if (button == MouseEvent.BUTTON1)
			if (save.getBounds().contains(x, y))
				save.setMousePressed(true);
			for (TileButton tb : tileButtons)
				if (tb.getBounds().contains(x, y))
					tb.setMousePressed(true);
	}

	@Override
	public void mouseReleased(int x, int y, int button) {
		super.mouseReleased(x, y, button);
		if (button == MouseEvent.BUTTON1)
			if (save.getBounds().contains(x, y) && save.isMousePressed())
				edit.saveMap();
			for (int i = 0; i < tileButtons.size(); i++) {
				TileButton tb = tileButtons.get(i);
				if (tb.getBounds().contains(x, y) && tb.isMousePressed())
					edit.setSelectedTileType(i);
			}

		for (TileButton tb : tileButtons)
			tb.reset();
		save.reset();
	}

	@Override
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		save.setMouseOver(false);
		if (save.getBounds().contains(x, y))
			save.setMouseOver(true);
		for (TileButton tb : tileButtons) {
			tb.setMouseOver(false);
			if (tb.getBounds().contains(x, y))
				tb.setMouseOver(true);
		}
	}

	public Edit getEdit() {
		return edit;
	}
	
	public TextButton getSave() {
		return save;
	}

}
