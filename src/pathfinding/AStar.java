package pathfinding;

import entities.units.Unit;
import gamestates.Play;

import java.awt.*;
import java.util.*;

import static main.Game.*;
import static main.Game.toTileY;
import static objects.Tile.WATER_GRASS;
import static objects.Tile.WATER_SAND;

public class AStar {

    private static PriorityQueue<Node> openList;
    private static HashSet<Point> closedList;
    private static HashMap<Point, Node> openMap;

    public static ArrayList<Point> pathFind(Point start, Point goal, Play play) {
        openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getfCost));
        closedList = new HashSet<>();
        openMap = new HashMap<>();

        Node current = new Node(start);
        openList.add(current);
        openMap.put(start, current);

        while (!openList.isEmpty()) {
            current = openList.poll();
            openMap.remove(current.getPoint());
            closedList.add(current.getPoint());

            if (current.getPoint().equals(goal)) {
                break;
            }

            ArrayList<Point> neighbors = getNeighbors(current.getPoint(), play);
            for (Point point : neighbors) {
                double gCost = current.getgCost() + getDistance(point, current.getPoint());
                double hCost = getDistance(point, goal);

                if (closedList.contains(point)) {
                    continue;
                }

                if (openMap.containsKey(point)) {
                    Node node = openMap.get(point);
                    if (gCost < node.getgCost()) {
                        openList.remove(node);
                        node.setgCost(gCost);
                        node.setfCost(gCost + hCost);
                        node.setParent(current);
                        openList.add(node);
                        openMap.put(point, node);
                    }
                } else {
                    Node newNode = new Node(point, current, gCost, hCost);
                    openList.add(newNode);
                    openMap.put(point, newNode);
                }
            }
        }

        if (!current.getPoint().equals(goal)) {
            return null;
        }

        ArrayList<Point> path = new ArrayList<>();
        while (!current.getPoint().equals(start)) {
            path.add(current.getPoint());
            current = current.getParent();
        }

        return reverse(path);
    }

    private static ArrayList<Point> getNeighbors(Point parent, Play play) {
        ArrayList<Point> neighbors = new ArrayList<>();
        int gridWidth = play.getMap().getTileData()[0].length;
        int gridHeight = play.getMap().getTileData().length;

        boolean isUpOpen = false, isRightOpen = false, isDownOpen = false, isLeftOpen = false;

        // Cardinal Directions (Up, Right, Down, Left)
        if (parent.y > 0) {
            Point above = new Point(parent.x, parent.y - 1);
            isUpOpen = isPointOpen(above, play, true);
            if (isUpOpen) neighbors.add(above);
        }

        if (parent.x < gridWidth - 1) {
            Point right = new Point(parent.x + 1, parent.y);
            isRightOpen = isPointOpen(right, play, true);
            if (isRightOpen) neighbors.add(right);
        }

        if (parent.y < gridHeight - 1) {
            Point below = new Point(parent.x, parent.y + 1);
            isDownOpen = isPointOpen(below, play, true);
            if (isDownOpen) neighbors.add(below);
        }

        if (parent.x > 0) {
            Point left = new Point(parent.x - 1, parent.y);
            isLeftOpen = isPointOpen(left, play, true);
            if (isLeftOpen) neighbors.add(left);
        }

        // Diagonal Directions (Top-left, Top-right, Bottom-left, Bottom-right)
        if (parent.y > 0 && parent.x > 0) {
            Point topLeft = new Point(parent.x - 1, parent.y - 1);
            if (isPointOpen(topLeft, play, true) && (isUpOpen || isLeftOpen))
                neighbors.add(topLeft);
        }

        if (parent.y > 0 && parent.x < gridWidth - 1) {
            Point topRight = new Point(parent.x + 1, parent.y - 1);
            if (isPointOpen(topRight, play, true) && (isUpOpen || isRightOpen))
                neighbors.add(topRight);
        }

        if (parent.y < gridHeight - 1 && parent.x > 0) {
            Point bottomLeft = new Point(parent.x - 1, parent.y + 1);
            if (isPointOpen(bottomLeft, play, true) && (isDownOpen || isLeftOpen))
                neighbors.add(bottomLeft);
        }

        if (parent.y < gridHeight - 1 && parent.x < gridWidth - 1) {
            Point bottomRight = new Point(parent.x + 1, parent.y + 1);
            if (isPointOpen(bottomRight, play, true) && (isDownOpen || isRightOpen))
                neighbors.add(bottomRight);
        }

        return neighbors;
    }

    public static boolean isPointOpen(Point point, Play play, boolean checkPathGoals) {
        int tileType = play.getMap().getTileData()[point.y][point.x].getTileType();
        return !(tileType == WATER_GRASS || tileType == WATER_SAND ||
                play.isTileBlockedOrReserved(point.x, point.y, null, checkPathGoals));
    }


    public static double getDistance(Point from, Point to) {
        double xDist = from.getX() - to.getX();
        double yDist = from.getY() - to.getY();

        // Calculate the Euclidean distance for any diagonal movement
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }

    private static ArrayList<Point> reverse(ArrayList<Point> path) {
        ArrayList<Point> reverse = new ArrayList<Point>();
        for (int i = path.size() - 1; i >= 0; i--)
            reverse.add(path.get(i));
        return reverse;
    }

    public static ArrayList<Point> getUnitPathToTile(Unit u, int tileX, int tileY, Play play) {
        Point goal = new Point(tileX, tileY);
        ArrayList<Point> path = null;
        if (u.getPath() != null && !u.getPath().isEmpty()) {
            path = pathFind(u.getPath().get(0), goal, play);
            if (path != null)
                path.add(0, u.getPath().get(0));
        } else {
            Point start = new Point(toTileX(u.getHitbox().x), toTileY(u.getHitbox().y));
            path = pathFind(start, goal, play);
        }
        return path;
    }

    public static ArrayList<Point> getUnitPathToNearestAdjacentTile(Unit u, int goalTileX, int goalTileY, Play play) {
        HashMap<Double, Point> openTiles = new HashMap<>();
        Point start = (u.getPath() != null && !u.getPath().isEmpty()) ? u.getPath().get(0)
                : new Point(toTileX(u.getHitbox().x), toTileY(u.getHitbox().y));

        int mapWidth = play.getMap().getTileData()[0].length;
        int mapHeight = play.getMap().getTileData().length;

        for (int x = goalTileX - 1; x <= goalTileX + 1; x++) {
            for (int y = goalTileY - 1; y <= goalTileY + 1; y++) {
                if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) continue;

                Point currTarget = new Point(x, y);

                if (AStar.isPointOpen(currTarget, play, true)) {
                    boolean isCardinal = (x == goalTileX || y == goalTileY);

                    if (!isCardinal && !isAdjacentDiagonalOpen(currTarget, new Point(goalTileX, goalTileY), play)) {
                        continue;
                    }

                    double distance = AStar.getDistance(start, currTarget);
                    if (!isCardinal) distance *= 2;  // Prioritize cardinally adjacent tiles

                    openTiles.put(distance, currTarget);
                }
            }
        }

        if (openTiles.isEmpty()) return null;

        ArrayList<Double> sortedDistances = new ArrayList<>(openTiles.keySet());
        Collections.sort(sortedDistances);

        for (double dist : sortedDistances) {
            Point target = openTiles.get(dist);
            ArrayList<Point> path = AStar.pathFind(start, target, play);
            if (path != null) {
                if (u.getPath() != null && !u.getPath().isEmpty())
                    path.add(0, u.getPath().get(0));
                return path;
            }
        }

        return null;
    }

    private static boolean isAdjacentDiagonalOpen(Point origin, Point target, Play play) {
        // Check if point in vertical direction of target & cardinal of the origin is open
        Point verticalPoint = new Point(origin.x, origin.y + (target.y - origin.y));
        boolean isVerticalPointOpen = (play.getEntityAtTile(verticalPoint.x, verticalPoint.y) == null && AStar.isPointOpen(verticalPoint, play, false));
        if (isVerticalPointOpen)
            return true;

        // Check if point in horizontal direction of target & cardinal of the origin is open
        Point horizontalPoint = new Point(origin.x + (target.x - origin.x), origin.y);
        return (play.getEntityAtTile(horizontalPoint.x, horizontalPoint.y) == null && AStar.isPointOpen(horizontalPoint, play, false));
    }

}
