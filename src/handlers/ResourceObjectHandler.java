package handlers;

import gamestates.Play;
import main.Game;
import objects.Chunk;
import objects.Tile;
import resources.*;
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
    private Random random = new Random();
    private ArrayList<GoldMine> goldMines = new ArrayList<>();
    private ArrayList<Tree> trees = new ArrayList<>();
    private ArrayList<Rock> rocks = new ArrayList<>();
    private ArrayList<CoalMine> coalMines = new ArrayList<>();
    private ArrayList<IronMine> ironMines = new ArrayList<>();
    private Tile[][] tileData;
    double[][] noiseMap;

    public ResourceObjectHandler(Play play) {
        this.play = play;
        this.tileData = play.getMap().getTileData();

        generateResourceObjects();
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Tree t : trees)
            g.drawImage(ImageLoader.resourceObjects[TREE][t.getBitmaskId()], t.getHitbox().x - (xOffset * Game.TILE_SIZE), t.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

        for (Rock r : rocks)
            g.drawImage(ImageLoader.resourceObjects[ROCK][r.getSpriteId()], r.getHitbox().x - (xOffset * Game.TILE_SIZE), r.getHitbox().y - (yOffset * Game.TILE_SIZE), null);

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
        Integer[][] resourceData = new Integer[tileData.length][tileData[0].length];
        int goldMineId = 0;
        int treeId = 0;
        int rockId = 0;
        int coalId = 0;
        int ironId = 0;

        for (Point p : play.getMap().getGoldMinePoints()) {
            goldMines.add(new GoldMine(p.x, p.y, goldMineId++));
            resourceData[p.y][p.x] = GOLD_MINE;
        }

        generateCoalPoints(resourceData);
        generateIronPoints(resourceData);
        generateRockPoints(resourceData);
        generateTreePoints(resourceData);

        for (int y = 0; y < resourceData.length; y++)
            for (int x = 0; x < resourceData[y].length; x++) {
                if (resourceData[y][x] == null)
                    continue;
                switch (resourceData[y][x]) {
                    case TREE -> trees.add(new Tree(x, y, treeId++, getBitmaskId(x, y, resourceData)));
                    case ROCK -> rocks.add(new Rock(x, y, rockId++, random.nextInt(ImageLoader.rocks.length)));
                    case COAL_MINE -> coalMines.add(new CoalMine(x, y, coalId++));
                    case IRON_MINE -> ironMines.add(new IronMine(x, y, ironId++));
                }
            }
    }

    private void generateCoalPoints(Integer[][] resourceData) {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c, resourceData);
                double chunkSize = c.getWidth() * c.getHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int maxCoal = (int) Math.max(Math.round(getMaxPerChunk(COAL_MINE) * percentage), 1);
                int coalCount = 0;

                if (!spawnablePoints.isEmpty()) {
                    int veinSourceIdx = random.nextInt(spawnablePoints.size());
                    Point veinSource = spawnablePoints.get(veinSourceIdx);
                    resourceData[veinSource.y][veinSource.x] = COAL_MINE;
                    coalCount++;
                    spawnablePoints.remove(veinSourceIdx);

                    ArrayList<Point> nextPoints = getSurroundingSpawnablePoints(veinSource, spawnablePoints);
                    while (coalCount < maxCoal && !nextPoints.isEmpty()) {
                        int remaining = maxCoal - coalCount;
                        if (remaining >= nextPoints.size()) {
                            for (Point np : nextPoints) {
                                resourceData[np.y][np.x] = COAL_MINE;
                                coalCount++;
                                spawnablePoints.remove(np);
                            }
                            if (remaining > nextPoints.size()) {
                                ArrayList<Point> newNextPoints = new ArrayList<>();
                                for (Point np : nextPoints) {
                                    ArrayList<Point> surrounding = getSurroundingSpawnablePoints(np, spawnablePoints);
                                    newNextPoints.addAll(surrounding);
                                }
                                nextPoints = newNextPoints;
                            }
                        } else {
                            int nextPointIndex = random.nextInt(nextPoints.size());
                            Point nextPoint = nextPoints.get(nextPointIndex);
                            resourceData[nextPoint.y][nextPoint.x] = COAL_MINE;
                            nextPoints.remove(nextPointIndex);
                            spawnablePoints.remove(nextPoint);
                            coalCount++;
                        }
                    }
                }
            }
    }

    private void generateIronPoints(Integer[][] resourceData) {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c, resourceData);
                double chunkSize = c.getWidth() * c.getHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int maxIron = (int) Math.max(Math.round(getMaxPerChunk(IRON_MINE) * percentage), 1);
                int ironCount = 0;

                if (!spawnablePoints.isEmpty()) {
                    int veinSourceIdx = random.nextInt(spawnablePoints.size());
                    Point veinSource = spawnablePoints.get(veinSourceIdx);
                    resourceData[veinSource.y][veinSource.x] = IRON_MINE;
                    ironCount++;
                    spawnablePoints.remove(veinSourceIdx);

                    ArrayList<Point> possibleBranchPoints = getSurroundingSpawnablePoints(veinSource, spawnablePoints);
                    int numBranches = Math.min(Math.min(possibleBranchPoints.size(), 4), maxIron - 1);
                    ArrayList<Point> branchStartPoints = new ArrayList<>();
                    if (numBranches == possibleBranchPoints.size())
                        branchStartPoints.addAll(possibleBranchPoints);
                    else
                        for (int i = 0; i < numBranches; i++) {
                            int branchPointIdx = random.nextInt(possibleBranchPoints.size());
                            branchStartPoints.add(possibleBranchPoints.get(branchPointIdx));
                            possibleBranchPoints.remove(branchPointIdx);
                        }

                    for (int i = 0; i < numBranches; i++) {
                        int branchStartIdx = random.nextInt(branchStartPoints.size());
                        Point branchStart = branchStartPoints.get(branchStartIdx);
                        int numNodesInBranch = Math.round((float) (maxIron - ironCount) / (float) branchStartPoints.size());
                        branchStartPoints.remove(branchStartIdx);

                        ArrayList<Point> currBranchPoints = new ArrayList<>();
                        currBranchPoints.add(veinSource);
                        currBranchPoints.add(branchStart);
                        ArrayList<Point> currBranchNextPoints = getSurroundingSpawnablePointsInVector(veinSource, branchStart, spawnablePoints);

                        while (currBranchPoints.size() <= numNodesInBranch && !currBranchNextPoints.isEmpty()) {
                            Point next = currBranchNextPoints.get(random.nextInt(currBranchNextPoints.size()));
                            currBranchPoints.add(next);
                            spawnablePoints.remove(next);
                            currBranchNextPoints = getSurroundingSpawnablePointsInVector(
                                    currBranchPoints.get(currBranchPoints.size() - 2),
                                    currBranchPoints.get(currBranchPoints.size() - 1),
                                    spawnablePoints
                            );
                        }
                        for (int idx = 1; idx < currBranchPoints.size(); idx++) {
                            Point currPoint = currBranchPoints.get(idx);
                            resourceData[currPoint.y][currPoint.x] = IRON_MINE;
                            ironCount++;
                        }
                    }
                }
            }
    }

    private void generateRockPoints(Integer[][] resourceData) {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c, resourceData);
                double chunkSize = c.getWidth() * c.getHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int max = (int) Math.min(Math.max(Math.round(getMaxPerChunk(ROCK) * percentage), 1), spawnablePoints.size());
                if (!spawnablePoints.isEmpty())
                    if (spawnablePoints.size() == max)
                        for (Point sp : spawnablePoints) {
                            resourceData[sp.y][sp.x] = ROCK;
                            spawnablePoints.remove(sp);
                        }
                    else
                        for (int i = 0; i < max; i++) {
                            int rockIdx = random.nextInt(spawnablePoints.size());
                            Point rockPoint = spawnablePoints.get(rockIdx);
                            resourceData[rockPoint.y][rockPoint.x] = ROCK;
                            spawnablePoints.remove(rockPoint);
                        }

            }
    }

    private void generateTreePoints(Integer[][] resourceData) {
        int width = resourceData[0].length;
        int height = resourceData.length;
        double inc = 0.065;
        noiseMap = new double[height][width];
        double yOff = 0;
        for (int y = 0; y < height; y++) {
            double xOff = 0.0;
            for (int x = 0; x < width; x++) {
                noiseMap[y][x] = PerlinNoise.noise(xOff, yOff);
                if (noiseMap[y][x] > 0 && play.getMap().isLand(x, y) && play.getMap().getTileData()[y][x].getTileType() != Tile.SAND && resourceData[y][x] == null)
                    resourceData[y][x] = TREE;
                xOff += inc;
            }
            yOff += inc;
        }
    }

    private ArrayList<Point> getResourceSpawnablePoints(Chunk chunk, Integer[][] resourceData) {
        ArrayList<Point> resourceSpawnablePoints = new ArrayList<>();
        int startY = chunk.getStartY();
        int startX = chunk.getStartX();
        for (int y = startY; y < startY + chunk.getHeight(); y++)
            for (int x = startX; x < startX + chunk.getWidth(); x++)
                if (chunk.getMap().isLand(x, y) && resourceData[y][x] == null)
                    resourceSpawnablePoints.add(new Point(x, y));
        return resourceSpawnablePoints;
    }

    private ArrayList<Point> getSurroundingSpawnablePoints(Point start, ArrayList<Point> spawnablePoints) {
        ArrayList<Point> surroundingPoints = new ArrayList<>();
        for (int y = start.y - 1; y < start.y + 2; y++)
            for (int x = start.x - 1; x < start.x + 2; x++) {
                if (x == start.x && y == start.y)
                    continue;
                Point currPoint = new Point(x, y);
                if (spawnablePoints.contains(currPoint))
                    surroundingPoints.add(currPoint);
            }
        return surroundingPoints;
    }

    private ArrayList<Point> getSurroundingSpawnablePointsInVector(Point start, Point next, ArrayList<Point> spawnablePoints) {
        int dx = next.x - start.x;
        int dy = next.y - start.y;
        ArrayList<Point> surroundingPointsInVector = new ArrayList<>();
        if (dx == 0) {
            for (int x = next.x - 1; x < next.x + 2; x++) {
                Point currPoint = new Point(x, next.y + dy);
                if (spawnablePoints.contains(currPoint))
                    surroundingPointsInVector.add(currPoint);
            }
        } else if (dy == 0) {
            for (int y = next.y - 1; y < next.y + 2; y++) {
                Point currPoint = new Point(next.x + dx, y);
                if (spawnablePoints.contains(currPoint))
                    surroundingPointsInVector.add(currPoint);
            }
        } else {
            ArrayList<Point> temp = new ArrayList<>();
            temp.add(new Point(next.x + dx, next.y + dy));
            temp.add(new Point(next.x, next.y + dy));
            temp.add(new Point(next.x + dx, next.y));
            for (Point p : temp)
                if (spawnablePoints.contains(p))
                    surroundingPointsInVector.add(p);
        }
        return surroundingPointsInVector;
    }

    private int getBitmaskId(int x, int y, Integer[][] resourceData) {
        int bitmaskId = 0;
        if (y != 0 && resourceData[y - 1][x] != null && resourceData[y - 1][x] == TREE)
            bitmaskId += 1;
        if (x != 0 && resourceData[y][x - 1] != null && resourceData[y][x - 1] == TREE)
            bitmaskId += 2;
        if (x != resourceData[y].length - 1 && resourceData[y][x + 1] != null && resourceData[y][x + 1] == TREE)
            bitmaskId += 4;
        if (y != resourceData.length - 1 && resourceData[y + 1][x] != null && resourceData[y + 1][x] == TREE)
            bitmaskId += 8;
        return bitmaskId;
    }

}
