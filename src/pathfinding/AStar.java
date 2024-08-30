package pathfinding;

import static main.Game.TILE_SIZE;
import static objects.Tile.WATER_GRASS;
import static objects.Tile.WATER_SAND;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import gamestates.Play;

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
            isUpOpen = isPointOpen(above, play);
            if (isUpOpen) neighbors.add(above);
        }

        if (parent.x < gridWidth - 1) {
            Point right = new Point(parent.x + 1, parent.y);
            isRightOpen = isPointOpen(right, play);
            if (isRightOpen) neighbors.add(right);
        }

        if (parent.y < gridHeight - 1) {
            Point below = new Point(parent.x, parent.y + 1);
            isDownOpen = isPointOpen(below, play);
            if (isDownOpen) neighbors.add(below);
        }

        if (parent.x > 0) {
            Point left = new Point(parent.x - 1, parent.y);
            isLeftOpen = isPointOpen(left, play);
            if (isLeftOpen) neighbors.add(left);
        }

        // Diagonal Directions (Top-left, Top-right, Bottom-left, Bottom-right)
        if (parent.y > 0 && parent.x > 0) {
            Point topLeft = new Point(parent.x - 1, parent.y - 1);
            if (isPointOpen(topLeft, play) && (isUpOpen || isLeftOpen))
                neighbors.add(topLeft);
        }

        if (parent.y > 0 && parent.x < gridWidth - 1) {
            Point topRight = new Point(parent.x + 1, parent.y - 1);
            if (isPointOpen(topRight, play) && (isUpOpen || isRightOpen))
                neighbors.add(topRight);
        }

        if (parent.y < gridHeight - 1 && parent.x > 0) {
            Point bottomLeft = new Point(parent.x - 1, parent.y + 1);
            if (isPointOpen(bottomLeft, play) && (isDownOpen || isLeftOpen))
                neighbors.add(bottomLeft);
        }

        if (parent.y < gridHeight - 1 && parent.x < gridWidth - 1) {
            Point bottomRight = new Point(parent.x + 1, parent.y + 1);
            if (isPointOpen(bottomRight, play) && (isDownOpen || isRightOpen))
                neighbors.add(bottomRight);
        }

        return neighbors;
    }

    public static boolean isPointOpen(Point point, Play play) {
        int x = point.x * TILE_SIZE;
        int y = point.y * TILE_SIZE + TOP_BAR_HEIGHT;
        int tileType = play.getMap().getTileData()[point.y][point.x].getTileType();
        return (tileType != WATER_GRASS && tileType != WATER_SAND && play.getGameObjectAt(x, y, true) == null);
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

}
