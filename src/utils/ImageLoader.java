package utils;

import static main.Game.TILE_SIZE;
import static objects.Tile.*;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static ui.TextBox.*;
import static ui.buttons.Button.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import buildings.Building;
import objects.Map;
import objects.Tile;
import ui.TextBox;

public class ImageLoader {

    private static final String ACTIONS = "actions.png";
    private static final String BOTTOM_BAR = "bottom_bar.png";
    private static final String BRUTE = "brute.png";
    private static final String BUILD_INDICATORS = "build_indicators.png";
    private static final String BUILDINGS = "buildings.png";
    private static final String CASTLE_ZONE = "castle_zone.png";
    private static final String DROP_DOWN_ARROW_LARGE = "drop_down_arrow_large.png";
    private static final String DROP_DOWN_BODY_LARGE = "drop_down_body_large.png";
    private static final String DROP_DOWN_BAR_LARGE = "drop_down_bar_large.png";
    private static final String DROP_DOWN_BAR_SMALL = "drop_down_bar_small.png";
    private static final String ICON_BUTTON = "icon_button.png";
    private static final String ICONS = "icons.png";
    private static final String LABORER = "laborer.png";
    private static final String ORES = "ores.png";
    private static final String OVERLAY_BG_LARGE = "overlay_bg_large.png";
    private static final String OVERLAY_BG_SMALL = "overlay_bg_small.png";
    private static final String ROCKS = "rocks.png";
    private static final String SELECT_INDICATOR = "select_indicator.png";
    private static final String SPRITE_BUTTON = "sprite_button.png";
    private static final String TEXT_BOX_BG = "text_box_bg.png";
    private static final String TEXT_BUTTON_LARGE = "text_button_large.png";
    private static final String TEXT_BUTTON_SMALL_LONG = "text_button_small_long.png";
    private static final String TEXT_BUTTON_SMALL_SHORT = "text_button_small_short.png";
    private static final String TILE_MAP = "tile_map.png";
    private static final String TOP_BAR = "top_bar.png";
    private static final String TREES = "trees.png";
    private static final int SPRITE_SIZE = 32;

    public static ArrayList<ArrayList<BufferedImage>> tiles;
    public static ArrayList<BufferedImage> editorBarButtonSprites;
    public static BufferedImage bottomBar, dropDownBodyLarge, dropDownBarLarge, dropDownBarSmall, overlayBgLarge, overlayBgSmall, topBar;
    public static BufferedImage[] actions, buildIndicators, buildings, dropDownArrowLarge, iconButton, icons, largeTextButton, ores, rocks, selectIndicator, smallTextButtonLong, smallTextButtonShort, spriteButton, textBoxBg, trees;
    public static BufferedImage[][] resourceObjects;
    public static BufferedImage[][][] brute, laborer;

    public static void loadImages() {
        loadBuildingImages();
        loadButtonImages();
        loadResourceImages();
        loadUnitImages();
        loadTerrainTiles();
        loadUIImages();
    }

    private static void loadBuildingImages() {
        BufferedImage atlas = LoadSave.loadImage(BUILDINGS);
        int maxBuildings = 11;
        buildings = new BufferedImage[maxBuildings];
        for (int i = 0; i < maxBuildings; i++)
            buildings[i] = atlas.getSubimage(i * TILE_SIZE * 2, 0, Building.getBuildingTileWidth(i) * TILE_SIZE, Building.getBuildingTileHeight(i) * TILE_SIZE);
    }

    private static void loadButtonImages() {
        int amount = 4;
        int rows = 4;
        int cols = 1;
        iconButton = getImageArray(ICON_BUTTON, getButtonWidth(ICON), getButtonHeight(ICON), rows, cols, amount);
        largeTextButton = getImageArray(TEXT_BUTTON_LARGE, getButtonWidth(TEXT_LARGE), getButtonHeight(TEXT_LARGE), rows, cols, amount);
        smallTextButtonLong = getImageArray(TEXT_BUTTON_SMALL_LONG, getButtonWidth(TEXT_SMALL_LONG), getButtonHeight(TEXT_SMALL_LONG), rows, cols, amount);
        smallTextButtonShort = getImageArray(TEXT_BUTTON_SMALL_SHORT, getButtonWidth(TEXT_SMALL_SHORT), getButtonHeight(TEXT_SMALL_SHORT), rows, cols, amount);
        spriteButton = getImageArray(SPRITE_BUTTON, getButtonWidth(SPRITE), getButtonHeight(SPRITE), rows, cols, amount);
    }

    private static void loadResourceImages() {
        ores = getSpriteArray(ORES, 0, 0, 1, 3, 3);
        rocks = getSpriteArray(ROCKS, 0, 0, 1, 3, 3);
        trees = getSpriteArray(TREES, 0, 0, 4, 4, 16);

        resourceObjects = new BufferedImage[][]{
                {ores[0]},
                trees,
                rocks,
                {ores[1]},
                {ores[2]}
        };
    }

    private static void loadUnitImages() {
        brute = getUnitAnimationArray(BRUTE, 4, 4);
        laborer = getUnitAnimationArray(LABORER, 5, 5);
    }

    private static void loadTerrainTiles() {
        tiles = new ArrayList<>();
        ArrayList<BufferedImage> grassTiles = new ArrayList<BufferedImage>();
        grassTiles.add(getSprite(LoadSave.loadImage(TILE_MAP), 0, 0));

        int rows = 6;
        int cols = 8;

        ArrayList<BufferedImage> dirtTiles = getSpriteArray(TILE_MAP, 1, 0, rows, cols);
        dirtTiles.remove(dirtTiles.size() - 1);
        ArrayList<BufferedImage> sandTiles = getSpriteArray(TILE_MAP, 7, 0, rows, cols);
        sandTiles.remove(sandTiles.size() - 1);
        ArrayList<BufferedImage> waterGrassTiles = getSpriteArray(TILE_MAP, 1, 8, rows, cols);
        waterGrassTiles.remove(waterGrassTiles.size() - 1);
        ArrayList<BufferedImage> waterSandTiles = getSpriteArray(TILE_MAP, 7, 8, rows, cols);
        waterSandTiles.remove(waterSandTiles.size() - 1);

        tiles.addAll(Arrays.asList(grassTiles, dirtTiles, sandTiles, waterGrassTiles, waterSandTiles));
    }

    private static void loadUIImages() {
        // Main UI Background
        bottomBar = LoadSave.loadImage(BOTTOM_BAR);
        topBar = LoadSave.loadImage(TOP_BAR);
        overlayBgLarge = LoadSave.loadImage(OVERLAY_BG_LARGE);
        overlayBgSmall = LoadSave.loadImage(OVERLAY_BG_SMALL);

        // Text Boxes
        BufferedImage textBoxes = LoadSave.loadImage(TEXT_BOX_BG);
        textBoxBg = new BufferedImage[2];
        textBoxBg[NUMBER] = textBoxes.getSubimage(0, 0, 144, 48);
        textBoxBg[TEXT] = textBoxes.getSubimage(TextBox.getTextBoxWidth(NUMBER), 0, 384, 48);

        // Drop Down Menus
        dropDownArrowLarge = getImageArray(DROP_DOWN_ARROW_LARGE, 20, 12, 1, 2, 2);
        dropDownBodyLarge = LoadSave.loadImage(DROP_DOWN_BODY_LARGE);
        dropDownBarLarge = LoadSave.loadImage(DROP_DOWN_BAR_LARGE);
        dropDownBarSmall = LoadSave.loadImage(DROP_DOWN_BAR_SMALL);

        // Misc
        actions = getSpriteArray(ACTIONS, 0, 0, 1, 8, 8);
        buildIndicators = getSpriteArray(BUILD_INDICATORS, 0, 0, 1, 2, 2);
        editorBarButtonSprites = new ArrayList<>(Arrays.asList(
                tiles.get(GRASS).get(0),
                tiles.get(DIRT).get(0),
                tiles.get(SAND).get(0),
                tiles.get(WATER_GRASS).get(0),
                tiles.get(WATER_SAND).get(0),
                getSprite(LoadSave.loadImage(CASTLE_ZONE), 0, 0),
                ores[0]
        ));
        icons = getImageArray(ICONS, 16, 16, 4, 5, 16);
        selectIndicator = getSpriteArray(SELECT_INDICATOR, 0, 0, 1, 4, 4);
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

    private static ArrayList<BufferedImage> getSpriteArray(String fileName, int rowStart, int colStart, int rows,
                                                           int cols) {
        BufferedImage atlas = LoadSave.loadImage(fileName);
        ArrayList<BufferedImage> sprites = new ArrayList<>();
        for (int y = rowStart; y < rowStart + rows; y++)
            for (int x = colStart; x < colStart + cols; x++)
                sprites.add(getSprite(atlas, x, y));
        return sprites;
    }

    private static BufferedImage[] getSpriteArray(String fileName, int rowStart, int colStart, int rows,
                                                  int cols, int amount) {
        BufferedImage atlas = LoadSave.loadImage(fileName);
        BufferedImage[] sprites = new BufferedImage[amount];
        for (int y = rowStart; y < rowStart + rows; y++)
            for (int x = colStart; x < colStart + cols; x++) {
                if ((x + cols * y) >= amount)
                    break;
                sprites[x + cols * y] = (getSprite(atlas, x, y));
            }
        return sprites;
    }

    private static BufferedImage[] getImageArray(String fileName, int width, int height, int rows, int cols, int amount) {
        BufferedImage atlas = LoadSave.loadImage(fileName);
        BufferedImage[] temp = new BufferedImage[amount];
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++) {
                if ((x + cols * y) >= amount)
                    break;
                temp[x + cols * y] = atlas.getSubimage(x * width, y * height, width, height);
            }
        return temp;
    }

    private static BufferedImage[][][] getUnitAnimationArray(String fileName, int numAnimations, int maxFrames) {
        BufferedImage atlas = LoadSave.loadImage(fileName);
        BufferedImage[][][] temp = new BufferedImage[numAnimations][4][maxFrames];
        int spriteSize = 32;
        int yStart = 0;
        for (int dir = 0; dir < temp.length; dir++) {
            for (int y = 0; y < temp[dir].length; y++)
                for (int x = 0; x < temp[dir][y].length; x++)
                    temp[dir][y][x] = atlas.getSubimage(x * spriteSize, yStart + y * spriteSize, spriteSize, spriteSize);
            yStart += spriteSize * 4;
        }
        return temp;
    }

    private static BufferedImage getSprite(BufferedImage atlas, int x, int y) {
        return atlas.getSubimage(x * SPRITE_SIZE, y * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
    }

}
