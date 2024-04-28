package objects;

import static objects.Tile.*;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import utils.LoadSave;
import utils.Savable;

public class Map implements Savable, Serializable {

    public static final int MIN_WIDTH = 50;
    public static final int MAX_WIDTH = 250;
    public static final int MIN_HEIGHT = 50;
    public static final int MAX_HEIGHT = 250;

    private Tile[][] tileData;
    private String name;
    /*
     * This is being defaulted to 2 for the demo, but I'm hoping to use this so that
     * the game can be easily adjusted to incorporate more than two players
     */
    private int numPlayers = 2;
    private ArrayList<Point> goldMinePoints = new ArrayList<Point>();
    private ArrayList<ArrayList<Point>> castleZones = new ArrayList<ArrayList<Point>>();
    private int[] tileCounts = new int[5];

    public Map(String name, int tileWidth, int tileHeight) {
        this.name = name;
        for (int i = 0; i < numPlayers; i++)
            castleZones.add(new ArrayList<Point>());
        createDefaultMap(tileWidth, tileHeight);
    }

    private void createDefaultMap(int tileWidth, int tileHeight) {
        tileData = new Tile[tileHeight][tileWidth];
        for (int j = 0; j < tileData.length; j++)
            for (int i = 0; i < tileData[j].length; i++) {
                tileData[j][i] = new Tile(GRASS, 0);
                tileCounts[GRASS]++;
            }
        LoadSave.saveMap(this);
    }

    public ArrayList<ArrayList<Point>> getCastleZones() {
        return castleZones;
    }

    public ArrayList<Point> getGoldMinePoints() {
        return goldMinePoints;
    }

    public int[] getTileCounts() {
        return tileCounts;
    }

    public Tile[][] getTileData() {
        return tileData;
    }

    public void setTileData(Tile[][] tileData) {
        this.tileData = tileData;
    }

    public String getName() {
        return name;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

}
