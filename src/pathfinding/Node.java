package pathfinding;

import java.awt.*;

public class Node {

    private Point point;
    private Node parent;

    private double gCost; // distance from start node based on best path
    private double hCost; // euclidean heuristic to goal
    private double fCost; // gCost + hCost

    public Node(Point point) {
        this.point = point;
        this.parent = null;
    }

    public Node(Point point, Node parent, double gCost, double hCost) {
        this.point = point;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;

    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public double getgCost() {
        return gCost;
    }

    public void setgCost(double gCost) {
        this.gCost = gCost;
    }

    public double gethCost() {
        return hCost;
    }

    public void sethCost(double hCost) {
        this.hCost = hCost;
    }

    public double getfCost() {
        return fCost;
    }

    public void setfCost(double fCost) {
        this.fCost = fCost;
    }

    public Point getPoint() {
        return point;
    }

}
