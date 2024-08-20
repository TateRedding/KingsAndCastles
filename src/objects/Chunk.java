package objects;

import java.awt.*;
import java.io.Serializable;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Chunk implements Serializable {

    public static final int MAX_CHUNK_SIZE = 32;

    private Map map;
    private int startX, startY, width, height;
    private Rectangle bounds;

    public Chunk(Map map, int startX, int startY, int width, int height) {
        this.map = map;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(startX * TILE_SIZE, startY * TILE_SIZE + TOP_BAR_HEIGHT, width * TILE_SIZE, height * TILE_SIZE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Map getMap() {
        return map;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
