package src.utils;

import static objects.Tile.*;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static ui.TextBox.*;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import objects.Map;
import objects.Tile;
import ui.buttons.Button;

public class ImageLoader {

	private static final String BOTTOM_BAR = "bottom_bar.png";
	private static final String CASTLE_ZONE = "castle_zone.png";
	private static final String DROP_DOWN_BODY = "drop_down_body.png";
	private static final String DROP_DOWN_BUTTONS = "drop_down_buttons.png";
	private static final String DROP_DOWN_TOP = "drop_down_top.png";
	private static final String RESOURCES = "resources.png";
	private static final String OVERLAY_BG = "overlay_bg.png";
	private static final String SELECT = "select.png";
	private static final String TERRAIN = "terrain.png";
	private static final String TEXT_BOX_BG = "text_box_bg.png";
	private static final String TEXT_BUTTON_LARGE = "text_button_large.png";
	private static final String TEXT_BUTTON_SMALL = "text_button_small.png";
	private static final String TILE_BUTTON = "tile_button.png";
	private static final String TOP_BAR = "top_bar.png";
	private static final String X_BUTTONS = "x_button.png";
	private static final int SPRITE_SIZE = 32;

	public static ArrayList<ArrayList<BufferedImage>> terrainTiles;
	public static ArrayList<BufferedImage> editorBarTileButtonImages;
	public static BufferedImage bottomBar, dropDownBody, dropDownTop, overlayBg, select, topBar;
	public static BufferedImage[] largeTextButton, resources, smallTextButton, textBoxBg, tileButton, xButton;
	public static BufferedImage[][] dropDownButtons;

	public static void loadImages() {

		loadButtonImages();
		loadObjectImages();
		loadTerrainTiles();
		loadUIImages();

		editorBarTileButtonImages = new ArrayList<BufferedImage>(Arrays.asList(terrainTiles.get(GRASS).get(0),
				terrainTiles.get(DIRT).get(0), terrainTiles.get(SAND).get(0), terrainTiles.get(WATER_GRASS).get(0),
				terrainTiles.get(WATER_SAND).get(0), getSprite(LoadSave.loadImage(CASTLE_ZONE), 0, 0), resources[0]));
	}

	private static void loadButtonImages() {
		int amount = 4;

		smallTextButton = getVerticalImageArray(TEXT_BUTTON_SMALL, 0, getButtonWidth(Button.TEXT_SMALL),
				getButtonHeight(Button.TEXT_SMALL), amount);
		largeTextButton = getVerticalImageArray(TEXT_BUTTON_LARGE, 0, getButtonWidth(Button.TEXT_LARGE),
				getButtonHeight(Button.TEXT_LARGE), amount);
		tileButton = getVerticalImageArray(TILE_BUTTON, 0, getButtonWidth(Button.TILE), getButtonHeight(Button.TILE),
				amount);
		xButton = getVerticalImageArray(X_BUTTONS, 0, getButtonWidth(Button.EX), getButtonHeight(Button.EX), amount);
		dropDownButtons = get2DImageArray(DROP_DOWN_BUTTONS, 42, 42, 2, amount);
	}

	private static void loadObjectImages() {
		resources = getHorizontalImageArray(RESOURCES, 0, SPRITE_SIZE, SPRITE_SIZE, 1);
	}

	private static void loadTerrainTiles() {
		terrainTiles = new ArrayList<>();
		BufferedImage atlas = LoadSave.loadImage(TERRAIN);

		ArrayList<BufferedImage> grassTiles = new ArrayList<BufferedImage>();
		grassTiles.add(getSprite(atlas, 0, 0));

		int rows = 6;
		int cols = 8;

		ArrayList<BufferedImage> dirtTiles = getSpriteArray(atlas, 1, 0, rows, cols);
		dirtTiles.remove(dirtTiles.size() - 1);
		ArrayList<BufferedImage> sandTiles = getSpriteArray(atlas, 7, 0, rows, cols);
		sandTiles.remove(sandTiles.size() - 1);
		ArrayList<BufferedImage> waterGrassTiles = getSpriteArray(atlas, 1, 8, rows, cols);
		waterGrassTiles.remove(waterGrassTiles.size() - 1);
		ArrayList<BufferedImage> waterSandTiles = getSpriteArray(atlas, 7, 8, rows, cols);
		waterSandTiles.remove(waterSandTiles.size() - 1);

		terrainTiles.addAll(Arrays.asList(grassTiles, dirtTiles, sandTiles, waterGrassTiles, waterSandTiles));
	}

	private static void loadUIImages() {
		bottomBar = LoadSave.loadImage(BOTTOM_BAR);
		select = LoadSave.loadImage(SELECT);
		topBar = LoadSave.loadImage(TOP_BAR);
		overlayBg = LoadSave.loadImage(OVERLAY_BG);

		BufferedImage textBoxes = LoadSave.loadImage(TEXT_BOX_BG);
		textBoxBg = new BufferedImage[2];
		textBoxBg[NUMBER] = textBoxes.getSubimage(0, 0, 144, 48);
		textBoxBg[TEXT] = textBoxes.getSubimage(textBoxBg[NUMBER].getWidth(), 0, 384, 48);

		dropDownBody = LoadSave.loadImage(DROP_DOWN_BODY);
		dropDownTop = LoadSave.loadImage(DROP_DOWN_TOP);
	}
	
	public static void createMapPreviewImage(Map map) {
		Tile[][] tileData = map.getTileData();
		BufferedImage previewImage = new BufferedImage(tileData[0].length, tileData.length, TYPE_INT_ARGB);
		Graphics g = previewImage.getGraphics();
		Color waterColor = new Color(99, 197, 207);
		Color grassColor = new Color(78, 102, 24);
		Color dirtColor = new Color(93, 79, 25);
		Color sandColor = new Color(230, 180, 92);
		
		for (int i = 0; i < tileData.length; i++)
			for (int j = 0; j < tileData[i].length; j++) {
				switch (tileData[i][j].getTileType()) {
				case GRASS -> g.setColor(grassColor);
				case DIRT -> g.setColor(dirtColor);
				case SAND -> g.setColor(sandColor);
				case WATER_GRASS, WATER_SAND -> g.setColor(waterColor);
				}
				g.fillRect(j, i, 1, 1);
			}

		File previewImageFile = new File(LoadSave.mapPath + File.separator + map.getName() + LoadSave.previewImageSuffix);
		try {
			ImageIO.write(previewImage, "png", previewImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static ArrayList<BufferedImage> getSpriteArray(BufferedImage atlas, int yStart, int xStart, int rows,
			int cols) {
		ArrayList<BufferedImage> sprites = new ArrayList<>();
		for (int i = yStart; i < yStart + rows; i++)
			for (int j = xStart; j < xStart + cols; j++)
				sprites.add(getSprite(atlas, j, i));
		return sprites;
	}

	private static BufferedImage[] getVerticalImageArray(String fileName, int xStart, int width, int height,
			int amount) {
		BufferedImage atlas = LoadSave.loadImage(fileName);
		BufferedImage[] temp = new BufferedImage[amount];

		for (int i = 0; i < temp.length; i++) {
			temp[i] = atlas.getSubimage(xStart, i * height, width, height);
		}
		return temp;
	}

	private static BufferedImage[] getHorizontalImageArray(String fileName, int yStart, int width, int height,
			int amount) {
		BufferedImage atlas = LoadSave.loadImage(fileName);
		BufferedImage[] temp = new BufferedImage[amount];

		for (int i = 0; i < temp.length; i++) {
			temp[i] = atlas.getSubimage(i * width, yStart, width, height);
		}
		return temp;
	}

	private static BufferedImage getSprite(BufferedImage atlas, int x, int y) {
		return atlas.getSubimage(x * SPRITE_SIZE, y * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
	}
	
	private static BufferedImage[][] get2DImageArray(String fileName, int width, int height, int columns, int rows) {
		BufferedImage atlas = LoadSave.loadImage(fileName);
		BufferedImage[][] temp = new BufferedImage[columns][rows];

		for (int i = 0; i < temp.length; i++)
			for (int j = 0; j < temp[0].length; j++)
				temp[i][j] = atlas.getSubimage(i * width, j * height, width, height);
		return temp;
	}

}
