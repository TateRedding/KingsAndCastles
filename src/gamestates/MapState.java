package gamestates;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.MiniMap;
import ui.bars.TopBar;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import static main.Game.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public abstract class MapState extends State implements Serializable {

    protected Map map;
    protected MiniMap miniMap;
    protected Rectangle gameBounds;
    protected Tile[][] tileData;

    // Pixel coordinates are relative to the entire screen
    // Tile coordinates are relative to indexes of 2D grid arrays such as tileData

    // Pixel coordinates of the top left pixel of the tile the cursor is in relative to the entire map
    protected int gameX, gameY;

    // Pixel coordinates of the top left pixel of the tile the cursor
    protected int mouseX, mouseY;

    // Tile coordinates of the tile the cursor is in
    protected int tileX, tileY;

    protected int mapXOffset, mapYOffset;
    protected int maxMapXOffset, maxMapYOffset;
    protected int mouseDownX, mouseDownY;
    protected boolean inGameArea;
    protected boolean leftMouseDown, rightMouseDown, isMouseDownInMiniMap;

    public MapState(Game game, Map map) {
        super(game);
        this.map = map;
        this.tileData = map.getTileData();
        this.miniMap = new MiniMap(this, tileData);
        this.maxMapXOffset = (map.getTileData()[0].length - GAME_AREA_TILE_WIDTH) * TILE_SIZE;
        this.maxMapYOffset = (map.getTileData().length - GAME_AREA_TILE_HEIGHT) * TILE_SIZE;
        this.gameBounds = new Rectangle(0, TopBar.TOP_BAR_HEIGHT, Game.GAME_AREA_WIDTH, Game.GAME_AREA_HEIGHT);
    }

    @Override
    public void update() {
        if (miniMap != null)
            miniMap.update();
    }

    @Override
    public void render(Graphics g) {
        game.getTileHandler().drawTiles(tileData, g, mapXOffset, mapYOffset);
        drawCastleZones(g, mapXOffset, mapYOffset);
    }

    private void drawCastleZones(Graphics g, int xOffset, int yOffset) {
        int alpha = 75;
        ArrayList<ArrayList<Point>> castleZones = map.getCastleZones();
        ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.RED));
        for (int i = 0; i < castleZones.size(); i++) {
            g.setColor(new Color(colors.get(i).getRed(), colors.get(i).getGreen(), colors.get(i).getBlue(), alpha));
            for (Point p : castleZones.get(i))
                g.fillRect(toPixelX(p.x) - xOffset, toPixelY(p.y) - yOffset, TILE_SIZE, TILE_SIZE);
        }
    }

    protected void dragScreen(int x, int y) {
        if (x - mouseDownX > 15 && mapXOffset > 0) {
            mapXOffset -= TILE_SIZE;
            mouseDownX = x;
        }
        if (mouseDownX - x > 15 && mapXOffset < maxMapXOffset) {
            mapXOffset += TILE_SIZE;
            mouseDownX = x;
        }
        if (y - mouseDownY > 15 && mapYOffset > 0) {
            mapYOffset -= TILE_SIZE;
            mouseDownY = y;
        }
        if (mouseDownY - y > 15 && mapYOffset < maxMapYOffset) {
            mapYOffset += TILE_SIZE;
            mouseDownY = y;
        }
    }

    protected void updateCoords(int x, int y) {
        gameX = toTileX(x + mapXOffset) * TILE_SIZE;
        gameY = toTileY(y + mapYOffset) * TILE_SIZE + TOP_BAR_HEIGHT;
        tileX = toTileX(gameX);
        tileY = toTileY(gameY);
        mouseX = (x / TILE_SIZE) * TILE_SIZE;
        mouseY = (y / TILE_SIZE) * TILE_SIZE;
    }

    private boolean checkIfInGameArea(int x, int y) {
        return gameBounds.contains(x, y) && (!miniMap.isMapExpanded() || !miniMap.getBounds().contains(x, y));
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            leftMouseDown = true;
        else if (button == MouseEvent.BUTTON3)
            rightMouseDown = true;

        if (!inGameArea && miniMap.getBounds().contains(x, y))
            miniMap.mousePressed(x, y, button);

        mouseDownX = x;
        mouseDownY = y;
    }

    public void mouseEntered(int x, int y) {
        if (y < Game.SCREEN_HEIGHT + TopBar.TOP_BAR_HEIGHT && y > TopBar.TOP_BAR_HEIGHT)
            inGameArea = true;
    }

    @Override
    public void mouseDragged(int x, int y) {
        inGameArea = checkIfInGameArea(x, y);
        isMouseDownInMiniMap = (miniMap.isMapExpanded() && miniMap.getMiniMapBounds().contains(new Point(mouseDownX, mouseDownY)));
        if (inGameArea)
            updateCoords(x, y);
        else {
            if (miniMap.getMiniMapBounds().contains(x, y) && miniMap.getMiniMapBounds().contains(new Point(mouseDownX, mouseDownY)))
                miniMap.mouseDragged(x, y);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (!inGameArea && miniMap.getBounds().contains(x, y) && miniMap.getBounds().contains(new Point(mouseDownX, mouseDownY)))
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
            if (mapXOffset <= 0)
                mapXOffset = 0;
            else
                mapXOffset -= TILE_SIZE;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (mapXOffset >= maxMapXOffset)
                mapXOffset = maxMapXOffset;
            else
                mapXOffset += TILE_SIZE;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (mapYOffset <= 0)
                mapYOffset = 0;
            else
                mapYOffset -= TILE_SIZE;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (mapYOffset >= maxMapYOffset)
                mapYOffset = maxMapYOffset;
            else
                mapYOffset += TILE_SIZE;
        }

    }

    public Map getMap() {
        return map;
    }

    public void setXTileOffset(int mapXOffset) {
        this.mapXOffset = mapXOffset;
    }

    public void setYTileOffset(int mapYOffset) {
        this.mapYOffset = mapYOffset;
    }

    public int getMaxMapXOffset() {
        return maxMapXOffset;
    }

    public int getMaxMapYOffset() {
        return maxMapYOffset;
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }

    public void setMiniMap(MiniMap miniMap) {
        this.miniMap = miniMap;
    }
}
