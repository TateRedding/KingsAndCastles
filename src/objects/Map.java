package objects;

import utils.LoadSave;
import utils.Savable;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static objects.Chunk.MAX_CHUNK_SIZE;
import static objects.Tile.GRASS;
import static objects.Tile.WATER_SAND;

public class Map implements Savable, Serializable {

    public static final int MIN_WIDTH = 50;
    public static final int MAX_WIDTH = 250;
    public static final int MIN_HEIGHT = 50;
    public static final int MAX_HEIGHT = 250;

    private Tile[][] tileData;
    private Chunk[][] chunks;
    private String name;
    /*
     * This is being defaulted to 2 for the demo, but I'm hoping to use this so that
     * the game can be easily adjusted to incorporate more than two players
     */
    private int numPlayers = 2;
    private ArrayList<ArrayList<Point>> castleZones = new ArrayList<ArrayList<Point>>();
    private ArrayList<Point> goldMinePoints;
    private Point[] throneRoomPoints;
    private int[] tileCounts = new int[5];

    public Map(String name, int tileWidth, int tileHeight) {
        this.name = name;
        for (int i = 0; i < numPlayers; i++)
            castleZones.add(new ArrayList<>());
        createDefaultMap(tileWidth, tileHeight);
        initChunks();
    }

    private void createDefaultMap(int tileWidth, int tileHeight) {
        tileData = new Tile[tileHeight][tileWidth];
        goldMinePoints = new ArrayList<>();
        throneRoomPoints = new Point[numPlayers];
        for (int j = 0; j < tileData.length; j++)
            for (int i = 0; i < tileData[j].length; i++) {
                tileData[j][i] = new Tile(GRASS, 0);
                tileCounts[GRASS]++;
            }
        LoadSave.saveMap(this);
    }

    private void initChunks() {
        int tileWidth = tileData[0].length;
        int tileHeight = tileData.length;
        int widthRemainder = tileWidth % MAX_CHUNK_SIZE;
        int heightRemainder = tileHeight % MAX_CHUNK_SIZE;

        int numChunksX = (tileWidth / MAX_CHUNK_SIZE) + Math.min(widthRemainder, 2);
        int numChunksY = (tileHeight / MAX_CHUNK_SIZE) + Math.min(heightRemainder, 2);

        int firstWidth = widthRemainder / 2;
        int lastWidth = widthRemainder - firstWidth;
        int firstHeight = heightRemainder / 2;
        int lastHeight = heightRemainder - firstHeight;

        chunks = new Chunk[numChunksY][numChunksX];

        int startY = 0;
        for (int chunkY = 0; chunkY < numChunksY; chunkY++) {
            int height;
            if (chunkY == 0 && firstHeight > 0)
                height = firstHeight;
            else if (chunkY == numChunksY - 1 && lastHeight > 0)
                height = lastHeight;
            else
                height = MAX_CHUNK_SIZE;
            int startX = 0;
            for (int chunkX = 0; chunkX < numChunksX; chunkX++) {
                int width;
                if (chunkX == 0 && firstWidth > 0)
                    width = firstWidth;
                else if (chunkX == numChunksX - 1 && lastWidth > 0)
                    width = lastWidth;
                else
                    width = MAX_CHUNK_SIZE;
                chunks[chunkY][chunkX] = new Chunk(this, startX, startY, width, height);
                startX += width;
            }
            startY += height;
        }
    }

    public boolean isFreeLand(int tileX, int tileY) {
        for (ArrayList<Point> zone : castleZones) {
            if (zone.contains(new Point(tileX, tileY)))
                return false;
        }
        return (tileData[tileY][tileX].getTileType() != Tile.WATER_GRASS && tileData[tileY][tileX].getTileType() != WATER_SAND);
    }

    public ArrayList<ArrayList<Point>> getCastleZones() {
        return castleZones;
    }

    public Chunk[][] getChunks() {
        return chunks;
    }

    public ArrayList<Point> getGoldMinePoints() {
        return goldMinePoints;
    }

    public Point[] getThroneRoomPoints() {
        return throneRoomPoints;
    }

    public int[] getTileCounts() {
        return tileCounts;
    }

    public Tile[][] getTileData() {
        return tileData;
    }

    public String getName() {
        return name;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

}
