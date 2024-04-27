package ui;

import gamestates.MapState;
import objects.Map;
import objects.Tile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static main.Game.GAME_AREA_TILE_HEIGHT;
import static main.Game.GAME_AREA_TILE_WIDTH;
import static objects.Tile.*;
import static ui.bars.UIBar.UI_WIDTH;


public class MiniMap implements Serializable {

    private MapState mapState;
    private BufferedImage worldMap;
    private Rectangle bounds;

    private Tile[][] lvlData;
    private int x, y, width, height;

    public MiniMap(MapState mapState, Tile[][] lvlData) {
        this.mapState = mapState;
        this.lvlData = lvlData;
        this.width = Map.MAX_WIDTH;
        this.height = Map.MAX_HEIGHT;

        int offset = 8;
        this.x = UI_WIDTH - offset - width;
        this.y = offset;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        createMap();
        drawMap(g);
        drawMapHighlight(g, xOffset, yOffset);
    }

    private void createMap() {

        worldMap = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics g = worldMap.getGraphics();

        // Draw the base Map
        for (int y = 0; y < lvlData.length; y++)
            for (int x = 0; x < lvlData[y].length; x++) {
                int tileType = lvlData[y][x].getTileType();
                switch (tileType) {
                    case WATER_GRASS:
                    case WATER_SAND:
                        g.setColor(new Color(70, 160, 255));
                        break;
                    case SAND:
                        g.setColor(new Color(220, 210, 165));
                        break;
                    case DIRT:
                        g.setColor(new Color(150, 110, 75));
                        break;
                    case GRASS:
                        g.setColor(new Color(0, 125, 45));
                        break;
                }
                g.fillRect(x, y, 1, 1);
            }

    }

    private void drawMap(Graphics g) {
        g.drawImage(worldMap, x, y, null);
    }

    private void drawMapHighlight(Graphics g, int xOffset, int yOffset) {
        int hlWidth = GAME_AREA_TILE_WIDTH;
        int hlHeight = GAME_AREA_TILE_HEIGHT;

        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(x + xOffset, y + yOffset, hlWidth, hlHeight);

        g.setColor(Color.GRAY);
        g.drawRect(x + xOffset, y + yOffset, hlWidth - 1, hlHeight - 1);
    }

    private void setScreenPosition(int x, int y) {
        int minX = GAME_AREA_TILE_WIDTH;
        int minY = GAME_AREA_TILE_HEIGHT;
        int maxX = lvlData[0].length - minX;
        int maxY = lvlData.length - minY;

        if (x < minX)
            mapState.setXTileOffset(0);
        else if (x > maxX)
            mapState.setXTileOffset(mapState.getMaxXTileOffset());
        else
            mapState.setXTileOffset(x - minX);

        if (y < minY)
            mapState.setYTileOffset(0);
        else if (y > maxY)
            mapState.setYTileOffset(mapState.getMaxYTileOffset());
        else
            mapState.setYTileOffset(y - minY);
    }

    public void mousePressed(int x, int y) {

    }

    public void mouseReleased(int x, int y) {
        setScreenPosition(x, y);
    }

    public void mouseDragged(int x, int y) {
        setScreenPosition(x, y);
    }

    public void mouseMoved(int x, int y) {

    }

    public Rectangle getBounds() {
        return bounds;
    }

}
