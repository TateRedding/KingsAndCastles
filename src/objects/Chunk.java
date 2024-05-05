package objects;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Chunk implements Serializable {

    public static final int MAX_CHUNK_SIZE = 32;

    private Map map;
    private int startX, startY, width, height;
    private ArrayList<Point> resourceSpawnablePoints = new ArrayList<>();

    public Chunk(Map map, int startX, int startY, int width, int height) {
        this.map = map;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        calculateResourceSpawnablePoints();
    }

    public void calculateResourceSpawnablePoints() {
        resourceSpawnablePoints.clear();
        for (int y = startY; y < startY + height; y++)
            for (int x = startX; x < startX + width; x++)
                if (map.isPointResourceSpawnable(x, y))
                    resourceSpawnablePoints.add(new Point(x, y));
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

    public ArrayList<Point> getResourceSpawnablePoints() {
        return resourceSpawnablePoints;
    }
}
