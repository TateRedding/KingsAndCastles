package handlers;

import objects.*;
import entities.units.Laborer;
import gamestates.Play;
import entities.resources.*;
import utils.ImageLoader;
import utils.OpenSimplex2;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import static main.Game.*;
import static objects.Chunk.MAX_CHUNK_SIZE;
import static entities.resources.ResourceObject.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class ResourceObjectHandler implements Serializable {

    private Play play;
    private Random random;
    private Tile[][] tileData;
    private ResourceObject[][] resourceObjectData;
    private double[][] noiseMap;
    private int gridWidth;

    public ResourceObjectHandler(Play play) {
        this.play = play;
        this.tileData = play.getMap().getTileData();
        this.resourceObjectData = play.getMap().getResourceObjectData();
        this.gridWidth = tileData[0].length;
        this.random = new Random(play.getSeed());
        generateResourceObjects();
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (int y = 0; y < resourceObjectData.length; y++)
            for (int x = 0; x < resourceObjectData[y].length; x++) {
                ResourceObject currRO = resourceObjectData[y][x];
                if (currRO != null && currRO.getHealth() < currRO.getMaxHealth())
                    currRO.drawHealthBar(g, currRO.getHealth(), currRO.getMaxHealth(), xOffset, yOffset);
            }
    }

    private void generateResourceObjects() {
        generateCoalPoints();
        generateIronPoints();
        generateRockPoints();
        generateTreePoints();

        for (int y = 0; y < resourceObjectData.length; y++)
            for (int x = 0; x < resourceObjectData[y].length; x++) {
                ResourceObject currRO = resourceObjectData[y][x];
                if (currRO != null && currRO.getSubType() == TREE)
                    currRO.setSpriteId(getBitmaskId(x, y));
            }
    }

    private void generateCoalPoints() {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c);
                double chunkSize = c.getTileWdith() * c.getTileHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int maxCoal = (int) Math.max(Math.round(getMaxPerChunk(COAL) * percentage), 1);
                int coalCount = 0;

                if (!spawnablePoints.isEmpty()) {
                    int veinSourceIdx = random.nextInt(spawnablePoints.size());
                    Point veinSource = spawnablePoints.get(veinSourceIdx);
                    resourceObjectData[veinSource.y][veinSource.x] = new CoalMine(veinSource.x, veinSource.y, veinSource.y * gridWidth + veinSource.x);
                    coalCount++;
                    spawnablePoints.remove(veinSourceIdx);

                    ArrayList<Point> nextPoints = getSurroundingSpawnablePoints(veinSource, spawnablePoints);
                    while (coalCount < maxCoal && !nextPoints.isEmpty()) {
                        int remaining = maxCoal - coalCount;
                        if (remaining >= nextPoints.size()) {
                            for (Point np : nextPoints) {
                                resourceObjectData[np.y][np.x] = new CoalMine(np.x, np.y, np.y * gridWidth + np.x);
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
                            resourceObjectData[nextPoint.y][nextPoint.x] = new CoalMine(nextPoint.x, nextPoint.y, nextPoint.y * gridWidth + nextPoint.x);
                            nextPoints.remove(nextPointIndex);
                            spawnablePoints.remove(nextPoint);
                            coalCount++;
                        }
                    }
                }
            }
    }

    private void generateIronPoints() {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c);
                double chunkSize = c.getTileWdith() * c.getTileHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int maxIron = (int) Math.max(Math.round(getMaxPerChunk(IRON) * percentage), 1);
                int ironCount = 0;

                if (!spawnablePoints.isEmpty()) {
                    int veinSourceIdx = random.nextInt(spawnablePoints.size());
                    Point veinSource = spawnablePoints.get(veinSourceIdx);
                    resourceObjectData[veinSource.y][veinSource.x] = new IronMine(veinSource.x, veinSource.y, veinSource.y * gridWidth + veinSource.x);
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
                            resourceObjectData[currPoint.y][currPoint.x] = new IronMine(currPoint.x, currPoint.y, currPoint.y * gridWidth + currPoint.x);
                            ironCount++;
                        }
                    }
                }
            }
    }

    private void generateRockPoints() {
        Chunk[][] chunks = play.getMap().getChunks();
        for (Chunk[] row : chunks)
            for (Chunk c : row) {
                ArrayList<Point> spawnablePoints = getResourceSpawnablePoints(c);
                double chunkSize = c.getTileWdith() * c.getTileHeight();
                double maxSize = MAX_CHUNK_SIZE * MAX_CHUNK_SIZE;
                double percentage = chunkSize / maxSize;
                int max = (int) Math.min(Math.max(Math.round(getMaxPerChunk(ROCK) * percentage), 1), spawnablePoints.size());
                if (!spawnablePoints.isEmpty())
                    if (spawnablePoints.size() == max)
                        for (Point sp : spawnablePoints) {
                            resourceObjectData[sp.y][sp.x] = new Rock(sp.x, sp.y, sp.y * gridWidth + sp.x, random.nextInt(ImageLoader.rocks.length));
                            spawnablePoints.remove(sp);
                        }
                    else
                        for (int i = 0; i < max; i++) {
                            int rockIdx = random.nextInt(spawnablePoints.size());
                            Point rockPoint = spawnablePoints.get(rockIdx);
                            resourceObjectData[rockPoint.y][rockPoint.x] = new Rock(rockPoint.x, rockPoint.y, rockPoint.y * gridWidth + rockPoint.x, random.nextInt(ImageLoader.rocks.length));
                            spawnablePoints.remove(rockPoint);
                        }

            }
    }

    private void generateTreePoints() {
        int width = resourceObjectData[0].length;
        int height = resourceObjectData.length;
        double inc = 0.065;
        noiseMap = new double[height][width];
        double yOff = 0;
        for (int y = 0; y < height; y++) {
            double xOff = 0.0;
            for (int x = 0; x < width; x++) {
                noiseMap[y][x] = OpenSimplex2.noise2_ImproveX(play.getSeed(), xOff, yOff);
                if (noiseMap[y][x] > 0 && play.getMap().isFreeLand(x, y) && play.getMap().getTileData()[y][x].getTileType() != Tile.SAND && resourceObjectData[y][x] == null)
                    resourceObjectData[y][x] = new Tree(x, y, y * gridWidth + x, 0);
                xOff += inc;
            }
            yOff += inc;
        }
    }

    private ArrayList<Point> getResourceSpawnablePoints(Chunk chunk) {
        ArrayList<Point> resourceSpawnablePoints = new ArrayList<>();
        int startY = chunk.getyStart();
        int startX = chunk.getxStart();
        for (int y = startY; y < startY + chunk.getTileHeight(); y++)
            for (int x = startX; x < startX + chunk.getTileWdith(); x++)
                if (chunk.getMap().isFreeLand(x, y) && resourceObjectData[y][x] == null)
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

    private int getBitmaskId(int x, int y) {
        int bitmaskId = 0;
        if (y != 0 && resourceObjectData[y - 1][x] != null && resourceObjectData[y - 1][x].getSubType() == TREE)
            bitmaskId += 1;
        if (x != 0 && resourceObjectData[y][x - 1] != null && resourceObjectData[y][x - 1].getSubType() == TREE)
            bitmaskId += 2;
        if (x != resourceObjectData[y].length - 1 && resourceObjectData[y][x + 1] != null && resourceObjectData[y][x + 1].getSubType() == TREE)
            bitmaskId += 4;
        if (y != resourceObjectData.length - 1 && resourceObjectData[y + 1][x] != null && resourceObjectData[y + 1][x].getSubType() == TREE)
            bitmaskId += 8;
        return bitmaskId;
    }

    public void gatherResource(Player player, ResourceObject ro, Laborer laborer) {
        if (ro.getSubType() == -1)
            return;

        int resourceType = ro.getSubType();
        int currAmt = ro.getHealth();
        int gatherAmt = Math.min(ResourceObject.getAmountPerAction(resourceType), currAmt);
        switch (resourceType) {
            case GOLD -> player.setGold(player.getGold() + gatherAmt);
            case TREE -> player.setLogs(player.getLogs() + gatherAmt);
            case ROCK -> player.setStone(player.getStone() + gatherAmt);
            case COAL -> player.setCoal(player.getCoal() + gatherAmt);
            case IRON -> player.setIron(player.getIron() + gatherAmt);
        }
        int newAmt = currAmt - gatherAmt;

        // Check if resource is depleted
        if (newAmt <= 0) {
            int roTileX = toTileX(ro.getX());
            int roTileY = toTileY(ro.getY());
            int laborerTileX = toTileX(laborer.getHitbox().x);
            int laborerTileY = toTileY(laborer.getHitbox().y);
            Map map = play.getMap();
            map.getResourceObjectData()[roTileY][roTileX] = null;

            // If depleted resource is a tree, update its sprite
            if (resourceType == TREE)
                for (int y = roTileY - 1; y < roTileY + 2; y++)
                    for (int x = roTileX - 1; x < roTileX + 2; x++)
                        if (y >= 0 && y < map.getTileData().length && x >= 0 && x < map.getTileData()[0].length && !(y == roTileY && x == roTileX)) {
                            ResourceObject currRO = map.getResourceObjectData()[y][x];
                            if (currRO != null && currRO.getSubType() == TREE)
                                currRO.setSpriteId(getBitmaskId(x, y));
                        }

            // Locate the nearest resource of the same type that can be pathed to
            for (int radius = 1; radius <= laborer.getSightRange(); radius++)
                for (int y = laborerTileY - radius; y <= laborerTileY + radius; y++)
                    for (int x = laborerTileX - radius; x <= laborerTileX + radius; x++) {

                        // Skip inner loops when radius > 1
                        if (Math.abs(x - laborerTileX) != radius && Math.abs(y - laborerTileY) != radius) continue;
                        if (y >= 0 && y < tileData.length && x >= 0 && x < tileData[0].length) {
                            ResourceObject currRO = map.getResourceObjectData()[y][x];
                            if (currRO != null && currRO.getSubType() == resourceType) {
                                if (laborer.isTargetInRange(currRO, laborer.getActionRange()) && laborer.isLineOfSightOpen(currRO)) {
                                    laborer.setTargetEntity(currRO);
                                    return;
                                }
                                ArrayList<Point> path = laborer.getUnitHandler().getPathToNearestAdjacentTile(laborer, toTileX(currRO.getX()), toTileY(currRO.getY()));
                                if (path != null) {
                                    laborer.setPath(path);
                                    laborer.setTargetEntity(currRO);
                                    return;
                                }
                            }
                        }
                    }
            laborer.setTargetEntity(null);
        } else
            ro.setHealth(newAmt);
    }

}
