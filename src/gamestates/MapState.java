package gamestates;

import static main.Game.GAME_AREA_TILE_HEIGHT;
import static main.Game.GAME_AREA_TILE_WIDTH;
import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;
import static utils.Constants.Resources.GOLD_MINE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.MiniMap;
import ui.bars.TopBar;
import utils.ImageLoader;

public abstract class MapState extends State implements StateMethods {

    protected Map map;
    protected MiniMap miniMap;
    protected Rectangle gameBounds;
    protected Tile[][] tileData;

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
        this.miniMap = new MiniMap(this, tileData);
        this.maxXTileOffset = map.getTileData()[0].length - GAME_AREA_TILE_WIDTH;
        this.maxYTileOffset = map.getTileData().length - GAME_AREA_TILE_HEIGHT;
        this.gameBounds = new Rectangle(0, TopBar.TOP_BAR_HEIGHT, Game.GAME_AREA_WIDTH, Game.GAME_AREA_HEIGHT);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        game.getTileHandler().drawTileData(tileData, g, xTileOffset, yTileOffset);
        drawCastleZones(g, xTileOffset, yTileOffset);
        drawGoldMines(g, xTileOffset, yTileOffset);
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

    private void drawGoldMines(Graphics g, int xOffset, int yOffset) {
        for (Point gm : map.getGoldMinePoints())
            g.drawImage(ImageLoader.resources[GOLD_MINE], (gm.x - xOffset) * TILE_SIZE, (gm.y - yOffset) * TILE_SIZE + TOP_BAR_HEIGHT, null);
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

    @Override
    public void mousePressed(int x, int y, int button) {
        if (inGameArea) {
            mouseDownX = x;
            mouseDownY = y;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseEntered(int x, int y) {
        if (y < Game.SCREEN_HEIGHT + TopBar.TOP_BAR_HEIGHT && y > TopBar.TOP_BAR_HEIGHT)
            inGameArea = true;
    }

    public void mouseExited(int x, int y) {
        inGameArea = false;
    }

    public void mouseDragged(int x, int y) {
        inGameArea = gameBounds.contains(x, y);
        if (inGameArea)
            updateCoords(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        inGameArea = gameBounds.contains(x, y);
        if (inGameArea)
            updateCoords(x, y);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {

    }

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

}
