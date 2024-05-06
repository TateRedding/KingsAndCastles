package handlers;

import gamestates.Play;
import main.Game;
import objects.Chunk;
import objects.Tile;
import resources.CoalMine;
import resources.GoldMine;
import resources.IronMine;
import resources.Tree;
import utils.ImageLoader;
import utils.PerlinNoise;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static objects.Chunk.MAX_CHUNK_SIZE;
import static resources.ResourceObjects.*;

public class ResourceObjectHandler implements Serializable {

    private Play play;
    private ArrayList<GoldMine> goldMines = new ArrayList<>();
    private ArrayList<Tree> trees = new ArrayList<>();
    private ArrayList<CoalMine> coalMines = new ArrayList<>();
    private ArrayList<IronMine> ironMines = new ArrayList<>();
    private Tile[][] tileData;
    double[][] noiseMap;

    public ResourceObjectHandler(Play play) {
        this.play = play;
        this.tileData = play.getMap().getTileData();

        int goldMineId = 0;
        for (Point p : play.getMap().getGoldMinePoints())
            goldMines.add(new GoldMine(p.x, p.y, goldMineId++));

        generateResourceObjects();
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Tree t : trees)
            g.drawImage(ImageLoader.resourceObjects[TREE][t.getBitmaskId()], t.getHitbox().x - (xOffset * Game.TILE_SIZE), t.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

        for (CoalMine cm : coalMines)
            g.drawImage(ImageLoader.resourceObjects[COAL_MINE][0], cm.getHitbox().x - (xOffset * Game.TILE_SIZE), cm.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

        for (IronMine im : ironMines)
            g.drawImage(ImageLoader.resourceObjects[IRON_MINE][0], im.getHitbox().x - (xOffset * Game.TILE_SIZE), im.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

        // drawTreeMap(g);
    }

    // DEBUGGING
    private void drawTreeMap(Graphics g) {
        BufferedImage treeImg = new BufferedImage(noiseMap[0].length, noiseMap.length, TYPE_INT_ARGB);
        Graphics imgG = treeImg.getGraphics();
        for (int y = 0; y < noiseMap.length; y++)
            for (int x = 0; x < noiseMap[y].length; x++) {
                if (noiseMap[y][x] > 0)
                    imgG.setColor(Color.GREEN);
                else
                    imgG.setColor(Color.BLUE);
                imgG.fillRect(x, y, 1, 1);
            }

        BufferedImage noiseImg = new BufferedImage(noiseMap[0].length, noiseMap.length, TYPE_INT_ARGB);
        imgG = noiseImg.getGraphics();
        for (int y = 0; y < noiseMap.length; y++)
            for (int x = 0; x < noiseMap[y].length; x++) {
                int greyValue = (int) ((noiseMap[y][x] + 1) / 2 * 255);
                imgG.setColor(new Color(greyValue, greyValue, greyValue));
                imgG.fillRect(x, y, 1, 1);
            }

        g.drawImage(treeImg, 0, 160, null);
        g.drawImage(noiseImg, 0, 160 + treeImg.getHeight(), null);
    }

    private void generateResourceObjects() {
        ArrayList<Point> coalPoints = generateCoalPoints();
        ArrayList<Point> ironPoints = generateIronPoints();
        ArrayList<Point> treePoints = generateTreePoints();

        int coalId = 0;
        for (Point coalPoint : coalPoints) {
            coalMines.add(new CoalMine(coalPoint.x, coalPoint.y, coalId++));
            ironPoints.removeIf(ironPoint -> ironPoint.equals(coalPoint));
            treePoints.removeIf(treePoint -> treePoint.equals(coalPoint));
        }

        int ironId = 0;
        for (Point ironPoint : ironPoints) {
            ironMines.add(new IronMine(ironPoint.x, ironPoint.y, ironId++));
            treePoints.removeIf(treePoint -> treePoint.equals(ironPoint));
        }

        int treeId = 0;
        for (Point treePoint : treePoints) {
            int bitmaskId = getBitmaskId(treePoint, treePoints);
            trees.add(new Tree(treePoint.x, treePoint.y, treeId++, bitmaskId));
        }
    }

    private ArrayList<Point> generateTreePoints() {
        int width = tileData[0].length;
        int height = tileData.length;
        double inc = 0.065;
        noiseMap = new double[height][width];

        ArrayList<Point> treePoints = new ArrayList<>();
        double yOff = 0;
        for (int y = 0; y < height; y++) {
            double xOff = 0.0;
            for (int x = 0; x < width; x++) {
                noiseMap[y][x] = PerlinNoise.noise(xOff, yOff);
                if (noiseMap[y][x] > 0 && play.getMap().isPointResourceSpawnable(x, y) && play.getMap().getTileData()[y][x].getTileType() != Tile.SAND)
                    treePoints.add(new Point(x, y));
                xOff += inc;
            }
            yOff += inc;
        }

        return treePoints;
    }

    private ArrayList<Point> generateCoalPoints() {
        Random r = new Random();
        Chunk[][] chunks = play.getMap().getChunks();
        ArrayList<Point> coalPoints = new ArrayList<>();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnPoints = c.getResourceSpawnablePoints();
                double chunkSize = c.getWidth() * c.getHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int max = (int) Math.max(Math.round(getMaxVeinSize(COAL_MINE) * percentage), 1);

                if (!spawnPoints.isEmpty()) {
                    Point start = spawnPoints.get(r.nextInt(spawnPoints.size()));
                    coalPoints.add(start);
                    int count = 1;
                    ArrayList<Point> nextPoints = getSurroundingSpawnablePoints(start, spawnPoints);
                    while (count < max && !nextPoints.isEmpty()) {
                        int remaining = max - count;
                        if (remaining >= nextPoints.size()) {
                            coalPoints.addAll(nextPoints);
                            count += nextPoints.size();
                            if (remaining > nextPoints.size()) {
                                ArrayList<Point> newNextPoints = new ArrayList<>();
                                for (Point np : nextPoints) {
                                    ArrayList<Point> surrounding = getSurroundingSpawnablePoints(np, spawnPoints);
                                    surrounding.removeAll(coalPoints);
                                    newNextPoints.addAll(surrounding);
                                }
                                nextPoints = newNextPoints;
                            }
                        } else {
                            int idx = r.nextInt(nextPoints.size());
                            coalPoints.add(nextPoints.get(idx));
                            nextPoints.remove(idx);
                            count++;
                        }
                    }
                }
            }
        return coalPoints;
    }

    private ArrayList<Point> generateIronPoints() {
        Random r = new Random();
        Chunk[][] chunks = play.getMap().getChunks();
        ArrayList<Point> ironPoints = new ArrayList<>();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnPoints = c.getResourceSpawnablePoints();
                double chunkSize = c.getWidth() * c.getHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int max = (int) Math.max(Math.round(getMaxVeinSize(IRON_MINE) * percentage), 1);
                int count = 0;

                if (!spawnPoints.isEmpty()) {
                    Point veinSource = spawnPoints.get(r.nextInt(spawnPoints.size()));
                    ironPoints.add(veinSource);
                    count++;
                    ArrayList<Point> possibleBranchPoints = getSurroundingSpawnablePoints(veinSource, spawnPoints);
                    int numBranches = Math.min(Math.min(possibleBranchPoints.size(), 4), max - 1);
                    ArrayList<Point> branchStartPoints = new ArrayList<>();

                    if (numBranches == possibleBranchPoints.size())
                        branchStartPoints.addAll(possibleBranchPoints);
                    else
                        for (int i = 0; i < numBranches; i++) {
                            int idx = r.nextInt(possibleBranchPoints.size());
                            branchStartPoints.add(possibleBranchPoints.get(idx));
                            possibleBranchPoints.remove(idx);
                        }

                    for (int i = 0; i < numBranches; i++) {
                        int idx = r.nextInt(branchStartPoints.size());
                        Point branchStart = branchStartPoints.get(idx);
                        int numNodesInBranch = Math.round((float) (max - count) / (float) branchStartPoints.size());
                        branchStartPoints.remove(idx);
                        ArrayList<Point> currBranchPoints = new ArrayList<>();
                        currBranchPoints.add(veinSource);
                        currBranchPoints.add(branchStart);
                        ArrayList<Point> currBranchNextPoints = getSurroundingSpawnablePointsInVector(veinSource, branchStart, spawnPoints);
                        currBranchNextPoints.removeAll(ironPoints);
                        while (currBranchPoints.size() <= numNodesInBranch && !currBranchNextPoints.isEmpty()) {
                            Point next = currBranchNextPoints.get(r.nextInt(currBranchNextPoints.size()));
                            currBranchPoints.add(next);
                            currBranchNextPoints = getSurroundingSpawnablePointsInVector(
                                    currBranchPoints.get(currBranchPoints.size() - 2),
                                    currBranchPoints.get(currBranchPoints.size() - 1),
                                    spawnPoints
                            );
                            currBranchNextPoints.removeAll(currBranchPoints);
                        }
                        ironPoints.addAll(currBranchPoints);
                        count += currBranchPoints.size() - 1;
                    }
                }
            }
        return ironPoints;
    }

    private ArrayList<Point> getSurroundingSpawnablePoints(Point start, ArrayList<Point> spawnPoints) {
        ArrayList<Point> surroundingPoints = new ArrayList<>();
        for (int y = start.y - 1; y < start.y + 2; y++)
            for (int x = start.x - 1; x < start.x + 2; x++) {
                if (x == start.x && y == start.y)
                    continue;
                Point currPoint = new Point(x, y);
                if (spawnPoints.contains(currPoint))
                    surroundingPoints.add(currPoint);
            }
        return surroundingPoints;
    }

    private ArrayList<Point> getSurroundingSpawnablePointsInVector(Point start, Point next, ArrayList<Point> spawnPoints) {
        int dx = next.x - start.x;
        int dy = next.y - start.y;
        ArrayList<Point> surroundingPointsInVector = new ArrayList<>();
        if (dx == 0) {
            for (int x = next.x - 1; x < next.x + 2; x++) {
                Point currPoint = new Point(x, next.y + dy);
                if (spawnPoints.contains(currPoint))
                    surroundingPointsInVector.add(currPoint);
            }
        } else if (dy == 0) {
            for (int y = next.y - 1; y < next.y + 2; y++) {
                Point currPoint = new Point(next.x + dx, y);
                if (spawnPoints.contains(currPoint))
                    surroundingPointsInVector.add(currPoint);
            }
        } else {
            ArrayList<Point> temp = new ArrayList<>();
            temp.add(new Point(next.x + dx, next.y + dy));
            temp.add(new Point(next.x, next.y + dy));
            temp.add(new Point(next.x + dx, next.y));
            for (Point p : temp)
                if (spawnPoints.contains(p))
                    surroundingPointsInVector.add(p);
        }
        return surroundingPointsInVector;
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
