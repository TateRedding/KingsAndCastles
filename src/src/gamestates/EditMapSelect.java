package src.gamestates;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.TEXT_LARGE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.DropDownMenu;
import ui.NewMapForm;
import ui.TextBox;
import ui.buttons.ExButton;
import ui.buttons.TextButton;
import utils.ImageLoader;
import utils.LoadSave;
import utils.RenderText;

public class EditMapSelect extends MapSelect implements StateMethods {

	private DropDownMenu dropDownMenu;
	private Map selectedMap;
	private String[][] selectedMapData;
	private NewMapForm newMapForm;
	private TextButton newMapButton, startButton;

	private int dropDownMenuYOffset = 48;
	private boolean showNewMapForm;

	public EditMapSelect(Game game) {
		super(game);

		initDropDownMenu();

		int formX = (Game.SCREEN_WIDTH - ImageLoader.overlayBg.getWidth()) / 2;
		int formY = (Game.SCREEN_HEIGHT - ImageLoader.overlayBg.getHeight()) / 2;
		newMapForm = new NewMapForm(formX, formY);

		float fontSize = 46f;
		int xStart = (SCREEN_WIDTH - getButtonWidth(TEXT_LARGE)) / 2;
		int yStart = (SCREEN_HEIGHT - getButtonHeight(TEXT_LARGE)) / 2;
		newMapButton = new TextButton(TEXT_LARGE, "New Map", fontSize, xStart, yStart);

		int formXEnd = formX + ImageLoader.overlayBg.getWidth();
		xStart = formXEnd + ((Game.SCREEN_WIDTH - formXEnd) - getButtonWidth(TEXT_LARGE)) / 2;
		startButton = new TextButton(TEXT_LARGE, "Start", fontSize, xStart, yStart);
	}

	private void initDropDownMenu() {
		int ddX = (Game.SCREEN_WIDTH - ImageLoader.dropDownTop.getWidth()) / 2;
		ArrayList<Map> maps = game.getMapHandler().getMaps();
		String[][] data = new String[maps.size()][2];
		for (int i = 0; i < maps.size(); i++) {
			Map currMap = maps.get(i);
			Tile[][] tileData = currMap.getTileData();
			data[i][0] = currMap.getName();
			data[i][1] = tileData[0].length + " x " + tileData.length;
		}
		dropDownMenu = new DropDownMenu("Select Saved Map", data, 5, ddX, dropDownMenuYOffset);
	}

	@Override
	public void update() {
		dropDownMenu.update();
		if (showNewMapForm) {
			newMapForm.update();
			startButton.setDisabled(!newMapForm.isValid());
		} else {
			startButton.setDisabled(false);
			if (selectedMap == null)
				newMapButton.update();
		}

		if (showNewMapForm || selectedMap != null)
			startButton.update();
	}

	@Override
	public void render(Graphics g) {
		if (showNewMapForm)
			newMapForm.render(g);
		else if (selectedMap == null)
			newMapButton.render(g);
		else
			renderSelectedMapData(g);

		dropDownMenu.render(g);

		if (showNewMapForm || selectedMap != null)
			startButton.render(g);
	}

	private void renderSelectedMapData(Graphics g) {
		int scale = 2;
		int previewWidth = Map.MAX_WIDTH * scale;
		int previewHeight = Map.MAX_HEIGHT * scale;
		int previewX = (SCREEN_WIDTH - previewWidth) / 2;
		int previewY = dropDownMenuYOffset * 2 + ImageLoader.dropDownTop.getHeight();

		g.setColor(new Color(0, 0, 0, 75));
		g.fillRect(previewX, previewY, previewWidth, previewHeight);

		BufferedImage previewImage = null;
		String path = LoadSave.mapPath + File.separator + selectedMap.getName() + LoadSave.previewImageSuffix;
		File previewImageFile = new File(path);
		if (previewImageFile.exists()) {
			try {
				previewImage = ImageIO.read(previewImageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (previewImage != null) {
			int previewImageWidth = previewImage.getWidth() * scale;
			int previewImageHeight = previewImage.getHeight() * scale;
			int previewImageX = previewX + (previewWidth - previewImageWidth) / 2;
			int previewImageY = previewY + (previewHeight - previewImageHeight) / 2;
			g.drawImage(previewImage, previewImageX, previewImageY, previewImageWidth, previewImageHeight, null);
		} else {
			String[] text = { "No preview", "available" };
			g.setColor(Color.BLACK);
			g.setFont(LoadSave.getGameFont(Font.BOLD, 74f));
			RenderText.renderText(g, text, RenderText.CENTER, previewX, previewY, previewWidth, previewHeight);
		}

		if (selectedMapData != null) {
			int yOffset = 48;
			int yStart = previewY + previewHeight + yOffset;
			int areaHeight = (SCREEN_HEIGHT - yStart - yOffset);
			Font leftFont = LoadSave.getGameFont(Font.BOLD, 46f);
			Font rightFont = LoadSave.getGameFont(Font.BOLD, 36f);
			g.setColor(Color.BLACK);
			RenderText.renderTextBoxed(g, selectedMapData, leftFont, rightFont, previewX, yStart, previewWidth,
					areaHeight);
		}
	}

	private void startNewMap() {
		String name = newMapForm.getMapName();
		if (name.length() == 0)
			return;

		int tileHeight = newMapForm.getMapHeight();
		if (tileHeight < Map.MIN_HEIGHT)
			tileHeight = Map.MIN_HEIGHT;
		else if (tileHeight > Map.MAX_HEIGHT)
			tileHeight = Map.MAX_HEIGHT;

		int tileWidth = newMapForm.getMapWidth();
		if (tileWidth < Map.MIN_WIDTH)
			tileWidth = Map.MIN_WIDTH;
		else if (tileWidth > Map.MAX_WIDTH)
			tileWidth = Map.MAX_WIDTH;

		Map newMap = new Map(name, tileWidth, tileHeight);
		startMapEditor(newMap);
		game.getMapHandler().loadMaps();
		initDropDownMenu();
	}

	private void startMapEditor(Map map) {
		game.editMap(map);
		GameStates.setGameState(GameStates.EDIT);
		showNewMapForm = false;
		selectedMap = null;
		selectedMapData = null;
		dropDownMenu.setExpanded(false);
	}

	public String[][] getMapData() {
		if (selectedMap == null)
			return null;
		Tile[][] tileData = selectedMap.getTileData();
		String[][] data = { { "Map Size", tileData[0].length + " x " + tileData[1].length },
				{ "Water Tiles", "Calculate Me" }, { "Gold Mines", "" + selectedMap.getGoldMinePoints().size() },
				{ "Castle Zones", selectedMap.getCastleZones().get(0).size() + " Tiles" } };
		return data;
	}

	@Override
	public void mousePressed(int x, int y, int button) {
		if (dropDownMenu.getBounds().contains(x, y))
			dropDownMenu.mousePressed(x, y, button);
		if (showNewMapForm) {
			if (newMapForm.getBounds().contains(x, y))
				newMapForm.mousePressed(x, y, button);
		} else if (selectedMap == null)
			if (button == MouseEvent.BUTTON1 && newMapButton.getBounds().contains(x, y))
				newMapButton.setMousePressed(true);

		if ((showNewMapForm || selectedMap != null) && button == MouseEvent.BUTTON1
				&& startButton.getBounds().contains(x, y))
			startButton.setMousePressed(true);
	}

	@Override
	public void mouseReleased(int x, int y, int button) {
		dropDownMenu.mouseReleased(x, y, button);
		if (dropDownMenu.getBounds().contains(x, y)) {
			int selectedIndex = dropDownMenu.getSelectedIndex();
			if (selectedIndex != -1) {
				selectedMap = game.getMapHandler().getMaps().get(selectedIndex);
				selectedMapData = getMapData();
			}
		}

		if (dropDownMenu.isExpanded())
			showNewMapForm = false;

		if (showNewMapForm) {
			if (newMapForm.getBounds().contains(x, y)) {
				ExButton xButton = newMapForm.getXButton();
				if (xButton.getBounds().contains(x, y) && xButton.isMousePressed()) {
					showNewMapForm = false;
					xButton.reset();
				} else {
					newMapForm.mouseReleased(x, y, button);
				}
			}
		} else if (selectedMap == null)
			if (newMapButton.getBounds().contains(x, y) && newMapButton.isMouseOver()) {
				showNewMapForm = true;
				newMapForm.resetTextBoxes();
				dropDownMenu.setExpanded(false);
			}

		if (startButton.getBounds().contains(x, y) && startButton.isMouseOver())
			if (showNewMapForm && selectedMap == null)
				startNewMap();
			else if (!showNewMapForm && selectedMap != null)
				startMapEditor(selectedMap);

		newMapButton.reset();
		startButton.reset();
	}

	@Override
	public void mouseMoved(int x, int y) {
		newMapButton.setMouseOver(false);
		startButton.setMouseOver(false);
		dropDownMenu.mouseMoved(x, y);
		if (showNewMapForm) {
			if (newMapForm.getBounds().contains(x, y))
				newMapForm.mouseMoved(x, y);
		} else if (selectedMap == null)
			if (newMapButton.getBounds().contains(x, y))
				newMapButton.setMouseOver(true);

		if ((showNewMapForm || selectedMap != null) && startButton.getBounds().contains(x, y))
			startButton.setMouseOver(true);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		dropDownMenu.mouseWheelMoved(e);
	}

	public void keyPressed(KeyEvent e) {
		if (showNewMapForm)
			newMapForm.keyPressed(e);
	}

}
