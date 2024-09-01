package ui;

import gamestates.MapState;
import objects.Map;
import objects.Tile;
import entities.resources.ResourceObject;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import utils.ImageLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static main.Game.*;
import static objects.Tile.*;
import static entities.resources.ResourceObject.*;
import static ui.bars.UIBar.UI_WIDTH;
import static ui.buttons.Button.*;


public class MiniMap implements Serializable {

    private MapState mapState;
    private BufferedImage miniMap;
    private Rectangle bounds, mmBounds;
    private ImageButton expand, terrain, resources, units, buildings;
    private ArrayList<ImageButton> iconButtons = new ArrayList<>();

    private Tile[][] tileData;
    private int areaX, areaY, areaWidth, areaHeight;
    private int mmX, mmY, mmWidth, mmHeight;
    private float scale;
    private boolean mapExpanded = true;
    private boolean showTerrain = true;
    private boolean showResources = true;
    private boolean showUnits = true;
    private boolean showBuildings = true;

    public MiniMap(MapState mapState, Tile[][] tileData) {
        this.mapState = mapState;
        this.tileData = tileData;

        int areaXOffset = 16;
        int areaYOffset = 16;
        int buttonHeight = Button.getButtonHeight(ICON);

        this.areaWidth = Map.MAX_WIDTH;
        this.areaHeight = Map.MAX_HEIGHT + buttonHeight;
        this.areaX = UI_WIDTH - areaXOffset - areaWidth;
        this.areaY = areaYOffset;
        this.bounds = new Rectangle(areaX, areaY, areaWidth, areaHeight);

        int tileHeight = tileData.length;
        int tileWidth = tileData[0].length;

        if (tileHeight > tileWidth)
            scale = (float) Map.MAX_HEIGHT / (float) tileHeight;
        else
            scale = (float) Map.MAX_WIDTH / (float) tileWidth;

        this.mmWidth = (int) (tileWidth * scale);
        this.mmHeight = (int) (tileHeight * scale);


        this.mmX = areaX + (areaWidth - mmWidth) / 2;
        this.mmY = areaY + (areaHeight - buttonHeight - mmHeight) / 2 + buttonHeight;
        this.mmBounds = new Rectangle(mmX, mmY, mmWidth, mmHeight);

        initButtons();
    }

    private void initButtons() {
        int numButtons = 5;
        int buttonWidth = Button.getButtonWidth(ICON);
        int xOffset = (areaWidth - (Button.getButtonWidth(ICON) * numButtons)) / (numButtons + 1);
        int xStart = areaX + xOffset;
        float buttonScale = 1.0f;

        buildings = new ImageButton(ICON, xStart, areaY, ImageLoader.icons[ICON_BUILDING], buttonScale);
        units = new ImageButton(ICON, xStart += buttonWidth + xOffset, areaY, ImageLoader.icons[ICON_UNIT], buttonScale);
        resources = new ImageButton(ICON, xStart += buttonWidth + xOffset, areaY, ImageLoader.icons[ICON_RESOURCE], buttonScale);
        terrain = new ImageButton(ICON, xStart += buttonWidth + xOffset, areaY, ImageLoader.icons[ICON_TERRAIN], buttonScale);
        expand = new ImageButton(ICON, xStart += buttonWidth + xOffset, areaY, ImageLoader.icons[ICON_MAP], buttonScale);

        iconButtons.addAll(Arrays.asList(buildings, units, resources, terrain, expand));
    }

    public void update() {
        if (mapExpanded)
            for (ImageButton ib : iconButtons)
                ib.update();
        else
            expand.update();
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        if (mapExpanded) {
            createMap();
            drawMap(g);
            drawMapHighlight(g, xOffset, yOffset);
            for (ImageButton ib : iconButtons)
                ib.render(g);
        } else
            expand.render(g);
    }

    private void createMap() {
        miniMap = new BufferedImage(mmWidth, mmHeight, TYPE_INT_ARGB);
        Graphics g = miniMap.getGraphics();

        if (showTerrain)
            createTerrainLayer(g);
        if (mapState.getClass() == gamestates.Play.class) {
            if (showResources)
                createResourceLayer(g);
            if (showBuildings)
                createBuildingLayer(g);
            if (showUnits)
                createUnitLayer(g);
        }

    }

    private void createTerrainLayer(Graphics g) {
        for (int y = 0; y < tileData.length; y++)
            for (int x = 0; x < tileData[y].length; x++) {
                int tileType = tileData[y][x].getTileType();
                switch (tileType) {
                    case WATER_GRASS:
                    case WATER_SAND:
                        g.setColor(new Color(0, 105, 183));
                        break;
                    case SAND:
                        g.setColor(new Color(230, 180, 92));
                        break;
                    case DIRT:
                        g.setColor(new Color(93, 79, 25));
                        break;
                    case GRASS:
                        g.setColor(new Color(96, 121, 42));
                        break;
                }
                g.fillRect((int) (x * scale), (int) (y * scale), (int) Math.ceil(scale), (int) Math.ceil(scale));
            }
    }

    private void createResourceLayer(Graphics g) {
        ResourceObject[][] resourceObjectData = mapState.getMap().getResourceObjectData();
        for (ResourceObject[] resourceObjectDatum : resourceObjectData)
            for (ResourceObject ro : resourceObjectDatum) {
                if (ro != null) {
                    int resourceType = ro.getSubType();
                    switch (resourceType) {
                        case GOLD:
                            g.setColor(new Color(240, 214, 125));
                            break;
                        case TREE:
                            g.setColor(new Color(53, 97, 47));
                            break;
                        case ROCK:
                            g.setColor(new Color(127, 117, 116));
                            break;
                        case COAL:
                            g.setColor(Color.BLACK);
                            break;
                        case IRON:
                            g.setColor(new Color(120, 50, 50));
                            break;
                    }

                    int xStart = (int) (toTileX(ro.getX()) * scale);
                    int yStart = (int) (toTileY(ro.getY()) * scale);
                    g.fillRect(xStart, yStart, (int) Math.ceil(scale), (int) Math.ceil(scale));
                }
            }
    }

    private void createBuildingLayer(Graphics g) {

    }

    private void createUnitLayer(Graphics g) {

    }

    private void drawMap(Graphics g) {
        g.setColor(new Color(64, 27, 0));
        g.drawRect(areaX - 1, areaY - 1, areaWidth + 1, areaHeight + 1);
        g.drawRect(areaX - 2, areaY - 2, areaWidth + 3, areaHeight + 3);

        g.setColor(new Color(255, 180, 128, 150));
        g.fillRect(areaX, areaY, areaWidth, areaHeight);

        g.drawImage(miniMap, mmX, mmY, null);
    }

    private void drawMapHighlight(Graphics g, int xOffset, int yOffset) {
        int hlWidth = (int) (GAME_AREA_TILE_WIDTH * scale);
        int hlHeight = (int) (GAME_AREA_TILE_HEIGHT * scale);
        int hlX = mmX + (int) ((xOffset / TILE_SIZE) * scale);
        int hlY = mmY + (int) ((yOffset / TILE_SIZE) * scale);

        g.setColor(Color.GRAY);
        g.drawRect(hlX, hlY, hlWidth - 1, hlHeight - 1);

        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(hlX, hlY, hlWidth, hlHeight);
    }

    private void setScreenPosition(int x, int y) {
        int minX = GAME_AREA_TILE_WIDTH / 2;
        int minY = GAME_AREA_TILE_HEIGHT / 2;
        int maxX = tileData[0].length - minX;
        int maxY = tileData.length - minY;

        int tileX = (int) ((x - mmX) / scale);
        int tileY = (int) ((y - mmY) / scale);

        if (tileX < minX)
            mapState.setXTileOffset(0);
        else if (tileX > maxX)
            mapState.setXTileOffset(mapState.getMaxMapXOffset());
        else
            mapState.setXTileOffset((tileX - minX) * TILE_SIZE);

        if (tileY < minY)
            mapState.setYTileOffset(0);
        else if (tileY > maxY)
            mapState.setYTileOffset(mapState.getMaxMapYOffset());
        else
            mapState.setYTileOffset((tileY - minY) * TILE_SIZE);
    }

    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (mapExpanded) {
                for (ImageButton ib : iconButtons)
                    if (ib.getBounds().contains(x, y))
                        ib.setMousePressed(true);
            } else if (expand.getBounds().contains(x, y))
                expand.setMousePressed(true);
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (mapExpanded) {
                if (mmBounds.contains(x, y))
                    setScreenPosition(x, y);
                else if (buildings.getBounds().contains(x, y) && buildings.isMousePressed())
                    showBuildings = !showBuildings;
                else if (units.getBounds().contains(x, y) && units.isMousePressed())
                    showUnits = !showUnits;
                else if (resources.getBounds().contains(x, y) && resources.isMousePressed())
                    showResources = !showResources;
                else if (terrain.getBounds().contains(x, y) && terrain.isMousePressed())
                    showTerrain = !showTerrain;
            }
            if (expand.getBounds().contains(x, y) && expand.isMousePressed())
                mapExpanded = !mapExpanded;

            for (ImageButton ib : iconButtons)
                ib.reset(x, y);
        }
    }

    public void mouseDragged(int x, int y) {
        if (mapExpanded)
            setScreenPosition(x, y);
    }

    public void mouseMoved(int x, int y) {
        for (ImageButton ib : iconButtons)
            ib.setMouseOver(false);
        if (mapExpanded) {
            for (ImageButton ib : iconButtons)
                if (ib.getBounds().contains(x, y))
                    ib.setMouseOver(true);
        } else if (expand.getBounds().contains(x, y))
            expand.setMouseOver(true);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isMapExpanded() {
        return mapExpanded;
    }

    public Rectangle getMiniMapBounds() {
        return mmBounds;
    }

}
