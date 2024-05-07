package objects;

import java.io.Serializable;

public class Chunk implements Serializable {

    public static final int MAX_CHUNK_SIZE = 32;

    private Map map;
    private int startX, startY, width, height;

    public Chunk(Map map, int startX, int startY, int width, int height) {
        this.map = map;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
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
