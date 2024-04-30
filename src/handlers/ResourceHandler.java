package handlers;

import gamestates.Play;
import main.Game;
import objects.Tile;
import resources.GoldMine;
import resources.Tree;
import ui.bars.TopBar;
import utils.ImageLoader;
import utils.PerlinNoise;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class ResourceHandler implements Serializable {

    private Play play;
    private ArrayList<GoldMine> goldMines = new ArrayList<>();
    private ArrayList<Tree> trees = new ArrayList<>();
    private Tile[][] tileData;
    double[][] noiseMap;

    public ResourceHandler(Play play) {
        this.play = play;
        this.tileData = play.getMap().getTileData();

        int goldMineId = 0;
        for (Point p : play.getMap().getGoldMinePoints())
            goldMines.add(new GoldMine(p.x, p.y, goldMineId++));

        generateTrees();
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Tree t : trees)
            g.drawImage(ImageLoader.trees.get(t.getBitmaskId()), t.getHitbox().x - (xOffset * Game.TILE_SIZE), t.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

        drawTreeMap(g);
    }

    private void drawTreeMap(Graphics g) {
        BufferedImage noiseImg = new BufferedImage(noiseMap[0].length, noiseMap.length, TYPE_INT_ARGB);
        Graphics imgG = noiseImg.getGraphics();
        for (int y = 0; y < noiseMap.length; y++)
            for (int x = 0; x < noiseMap[y].length; x++) {
                if (noiseMap[y][x] > 0.5)
                    imgG.setColor(Color.BLACK);
                else
                    imgG.setColor(Color.WHITE);
                imgG.fillRect(x, y, 1, 1);
            }

        g.drawImage(noiseImg, 32, 232, null);
    }

    private void generateTrees() {
        int width = tileData[0].length;
        int height = tileData.length;
        double frequency = 5;
        Random random = new Random(); // Create a Random object
        int xOffset = random.nextInt(101);
        int yOffset = random.nextInt(101);
        noiseMap = new double[height][width];

        ArrayList<Point> treePoints = new ArrayList<>();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                double xCoord = (double) (x + xOffset) / width;
                double yCoord = (double) (y + yOffset) / height;
                noiseMap[y][x] = PerlinNoise.noise(xCoord, yCoord, frequency);
                if (noiseMap[y][x] > .5
                        && (tileData[y][x].getTileType() == Tile.GRASS || tileData[y][x].getTileType() == Tile.DIRT)
                        && !play.getMap().getGoldMinePoints().contains(new Point(x, y)))
                    treePoints.add(new Point(x, y));
            }

        int treeId = 0;
        for (Point treePoint : treePoints) {
            int bitmaskId = getBitmaskId(treePoint, treePoints);
            trees.add(new Tree(treePoint.x, treePoint.y, treeId++, bitmaskId));
        }
    }

    private int getBitmaskId(Point treePoint, ArrayList<Point> treePoints) {
        int bitmaskId = 0;
        Point northPoint = new Point(treePoint.x, treePoint.y - 1);
        Point eastPoint = new Point(treePoint.x + 1, treePoint.y);
        Point southPoint = new Point(treePoint.x, treePoint.y + 1);
        Point westPoint = new Point(treePoint.x - 1, treePoint.y);
        if (treePoints.contains(northPoint))
            bitmaskId += 1;
        if (treePoints.contains(westPoint))
            bitmaskId += 2;
        if (treePoints.contains(eastPoint))
            bitmaskId += 4;
        if (treePoints.contains(southPoint))
            bitmaskId += 8;
        return bitmaskId;
    }

}
