package gamestates;

import static main.Game.GAME_AREA_TILE_HEIGHT;
import static main.Game.GAME_AREA_TILE_WIDTH;
import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import objects.Map;
import objects.Tile;
import resources.ResourceObject;
import ui.MiniMap;
import ui.bars.TopBar;
import utils.ImageLoader;

public abstract class MapState extends State implements Serializable {

    protected Map map;
    protected MiniMap miniMap;
    protected Rectangle gameBounds;
    protected Tile[][] tileData;
    protected ResourceObject[][] resourceObjectData;

    protected int gameX, gameY;
    protected int mouseX, mouseY;
    protected int tileX, tileY;
    protected int xTileOffset, yTileOffset;
    protected int maxXTileOffset, maxYTileOffset;
    protected int mouseDownX, mouseDownY;
    protected boolean inGameArea;

    public MapState(Game game, Map map) {
        super(game);
        this.map = map;
        this.tileData = map.getTileData();
        this.resourceObjectData = map.getResourceObjectData();
        this.miniMap = new MiniMap(this, tileData);
        this.maxXTileOffset = map.getTileData()[0].length - GAME_AREA_TILE_WIDTH;
        this.maxYTileOffset = map.getTileData().length - GAME_AREA_TILE_HEIGHT;
        this.gameBounds = new Rectangle(0, TopBar.TOP_BAR_HEIGHT, Game.GAME_AREA_WIDTH, Game.GAME_AREA_HEIGHT);
    }

    @Override
    public void update() {
        if (miniMap != null)
            miniMap.update();
    }

    @Override
    public void render(Graphics g) {
        game.getTileHandler().drawTiles(tileData, g, xTileOffset, yTileOffset);
        drawCastleZones(g, xTileOffset, yTileOffset);
        drawResourceObjects(g, xTileOffset, yTileOffset);
    }

    private void drawCastleZones(Graphics g, int xOffset, int yOffset) {
        int alpha = 75;
        ArrayList<ArrayList<Point>> castleZones = map.getCastleZones();
        ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED));
        for (int i = 0; i < castleZones.size(); i++) {
            g.setColor(new Color(colors.get(i).getRed(), colors.get(i).getGreen(), colors.get(i).getBlue(), alpha));
            for (Point p : castleZones.get(i))
                g.fillRect((p.x - xOffset) * TILE_SIZE, (p.y - yOffset) * TILE_SIZE + TOP_BAR_HEIGHT, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawResourceObjects(Graphics g, int xOffset, int yOffset) {
        int yStart = TopBar.TOP_BAR_HEIGHT / Game.TILE_SIZE;
        for (int y = 0; y < resourceObjectData.length; y++)
            for (int x = 0; x < resourceObjectData[y].length; x++) {
                ResourceObject currRO = resourceObjectData[y][x];
                if (currRO != null)
                    g.drawImage(ImageLoader.resourceObjects[currRO.getResourceType()][currRO.getSpriteId()], (x - xOffset) * Game.TILE_SIZE,
                            (y + yStart - yOffset) * Game.TILE_SIZE, null);
            }
    }

    protected void dragScreen(int x, int y) {
        if (x - mouseDownX > 15 && xTileOffset > 0) {
            xTileOffset--;
            mouseDownX = x;
        }
        if (mouseDownX - x > 15 && xTileOffset < maxXTileOffset) {
            xTileOffset++;
            mouseDownX = x;
        }
        if (y - mouseDownY > 15 && yTileOffset > 0) {
            yTileOffset--;
            mouseDownY = y;
        }
        if (mouseDownY - y > 15 && yTileOffset < maxYTileOffset) {
            yTileOffset++;
            mouseDownY = y;
        }
    }

    protected void updateCoords(int x, int y) {
        gameX = ((x + (xTileOffset * TILE_SIZE)) / TILE_SIZE) * TILE_SIZE;
        gameY = (((y - TOP_BAR_HEIGHT) + (yTileOffset * TILE_SIZE)) / TILE_SIZE) * TILE_SIZE;
        tileX = gameX / TILE_SIZE;
        tileY = gameY / TILE_SIZE;
        mouseX = (x / Game.TILE_SIZE) * Game.TILE_SIZE;
        mouseY = (y / Game.TILE_SIZE) * Game.TILE_SIZE;
    }

    private boolean checkIfInGameArea(int x, int y) {
        return gameBounds.contains(x, y) && (!miniMap.isMapExpanded() || !miniMap.getBounds().contains(x, y));
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (!inGameArea && miniMap.getBounds().contains(x, y))
            miniMap.mousePressed(x, y, button);
        if (inGameArea) {
            mouseDownX = x;
            mouseDownY = y;
        }
    }

    public void mouseEntered(int x, int y) {
        if (y < Game.SCREEN_HEIGHT + TopBar.TOP_BAR_HEIGHT && y > TopBar.TOP_BAR_HEIGHT)
            inGameArea = true;
    }

    @Override
    public void mouseDragged(int x, int y) {
        inGameArea = checkIfInGameArea(x, y);
        if (inGameArea)
            updateCoords(x, y);
        else if (miniMap.getMiniMapBounds().contains(x, y))
            miniMap.mouseDragged(x, y);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (!inGameArea && miniMap.getBounds().contains(x, y))
                miniMap.mouseReleased(x, y, button);
    }

    @Override
    public void mouseMoved(int x, int y) {
        miniMap.mouseMoved(x, y);
        inGameArea = checkIfInGameArea(x, y);
        if (inGameArea)
            updateCoords(x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (xTileOffset <= 0)
                xTileOffset = 0;
            else
                xTileOffset--;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (xTileOffset >= maxXTileOffset)
                xTileOffset = maxXTileOffset;
            else
                xTileOffset++;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (yTileOffset <= 0)
                yTileOffset = 0;
            else
                yTileOffset--;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (yTileOffset >= maxYTileOffset)
                yTileOffset = maxYTileOffset;
            else
                yTileOffset++;
        }

    }

    public Map getMap() {
        return map;
    }

    public void setXTileOffset(int xTileOffset) {
        this.xTileOffset = xTileOffset;
    }

    public void setYTileOffset(int yTileOffset) {
        this.yTileOffset = yTileOffset;
    }

    public int getMaxXTileOffset() {
        return maxXTileOffset;
    }

    public int getMaxYTileOffset() {
        return maxYTileOffset;
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }

    public void setMiniMap(MiniMap miniMap) {
        this.miniMap = miniMap;
    }
}
