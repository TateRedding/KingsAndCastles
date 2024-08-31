package objects;

import java.awt.*;
import java.io.Serializable;

import static main.Game.*;

public class Chunk implements Serializable {

    public static final int MAX_CHUNK_SIZE = 32;

    private Map map;
    private int xStart, yStart, tileWdith, tileHeight;
    private Rectangle bounds;

    public Chunk(Map map, int xStart, int yStart, int tileWidth, int tileHeight) {
        this.map = map;
        this.xStart = xStart;
        this.yStart = yStart;
        this.tileWdith = tileWidth;
        this.tileHeight = tileHeight;
        this.bounds = new Rectangle(toPixelX(xStart), toPixelY(yStart), tileWidth * TILE_SIZE, tileHeight * TILE_SIZE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Map getMap() {
        return map;
    }

    public int getxStart() {
        return xStart;
    }

    public int getyStart() {
        return yStart;
    }

    public int getTileWdith() {
        return tileWdith;
    }

    public int getTileHeight() {
        return tileHeight;
    }

}
