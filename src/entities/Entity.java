package entities;

import entities.buildings.Building;
import entities.units.Unit;
import gamestates.Play;
import objects.Player;
import pathfinding.AStar;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.units.Unit.IDLE;
import static main.Game.*;

public abstract class Entity implements Serializable {

    public static final int BUILDING = 0;
    public static final int PROJECTILE = 1;
    public static final int RESOURCE = 1;
    public static final int UNIT = 2;

    public static final int HEALTH_BAR_MAX_WIDTH = TILE_SIZE / 4 * 3;

    protected Player player;
    protected Rectangle hitbox;
    protected float x, y;
    protected int id;
    protected int entityType, subType;
    protected int health, maxHealth;

    protected boolean active = true;

    public Entity(Player player, int entityType, int subType, float x, float y, int id) {
        this.player = player;
        this.entityType = entityType;
        this.subType = subType;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public void drawHealthBar(Graphics g, int current, int max, int xOffset, int yOffset) {
        int xStart = (hitbox.x + (hitbox.width - HEALTH_BAR_MAX_WIDTH) / 2) - xOffset;
        int yStart = (hitbox.y + 3) - yOffset;

        int fillWidth = (int) (((float) current / (float) max) * HEALTH_BAR_MAX_WIDTH);

        g.setColor(new Color(64, 27, 0));
        g.drawRect(xStart - 3, yStart - 3, HEALTH_BAR_MAX_WIDTH + 6, 8);
        g.drawRect(xStart - 1, yStart - 1, HEALTH_BAR_MAX_WIDTH + 2, 4);

        g.setColor(new Color(255, 201, 128));
        g.drawRect(xStart - 2, yStart - 2, HEALTH_BAR_MAX_WIDTH + 4, 6);

        g.setColor(new Color(136, 33, 42));
        g.drawRect(xStart, yStart, fillWidth, 1);

        g.setColor(new Color(189, 79, 79));
        g.drawRect(xStart, yStart + 1, fillWidth, 1);

    }

    public boolean isTargetInRange(Entity target, int tileRange) {
        float startX = x - tileRange * TILE_SIZE;
        float startY = y - tileRange * TILE_SIZE;
        float size = (tileRange * 2 + 1) * TILE_SIZE;
        Ellipse2D range = new Ellipse2D.Double(startX, startY, size, size);
        Rectangle targetBounds = target.getHitbox();

        int numTilesX = targetBounds.width / TILE_SIZE;
        int numTilesY = targetBounds.height / TILE_SIZE;
        int halfTileSize = TILE_SIZE / 2;

        for (int tilesX = 0; tilesX < numTilesX; tilesX++)
            for (int tilesY = 0; tilesY < numTilesY; tilesY++) {
                int middleX = targetBounds.x + (tilesX * TILE_SIZE) + halfTileSize;
                int middleY = targetBounds.y + (tilesY * TILE_SIZE) + halfTileSize;
                if (range.contains(middleX, middleY))
                    return true;
            }
        return false;
    }

    public boolean isLineOfSightOpen(Entity target) {
        Point attackerTile;
        Point targetTile;
        Play play = null;
        if (this instanceof Unit u)
            play = u.getUnitHandler().getPlay();
        else if (this instanceof Building b)
            play = b.getBuildingHandler().getPlay();

        if (play == null) return false;

        if (this instanceof Unit unit && unit.getPath() != null && !unit.getPath().isEmpty())
            attackerTile = unit.getPath().get(0);
        else
            attackerTile = new Point(toTileX(hitbox.x), toTileY(hitbox.y));

        if (target.getEntityType() == UNIT && ((Unit) target).getPath() != null && !(((Unit) target).getPath().isEmpty()))
            targetTile = ((Unit) target).getPath().get(0);
        else
            targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));

        Point currentTile = attackerTile;

        while (!currentTile.equals(targetTile)) {
            ArrayList<Point> neighbors = getTilesClosestToTarget(currentTile, targetTile, play);

            if (neighbors.isEmpty())
                return false;

            Point nextTile = null;
            for (Point neighbor : neighbors) {
                if (neighbor.equals(targetTile))
                    return true;

                if (!play.isTileBlockedOrReserved(neighbor.x, neighbor.y, null)) {
                    nextTile = neighbor;
                    break;
                }
            }

            if (nextTile != null)
                currentTile = nextTile;
            else
                return false;
        }
        return true;
    }

    private ArrayList<Point> getTilesClosestToTarget(Point start, Point target, Play play) {
        double lowestDist = Double.POSITIVE_INFINITY;
        int gridWidth = play.getMap().getTileData()[0].length;
        int gridHeight = play.getMap().getTileData().length;

        ArrayList<Point> closestTiles = new ArrayList<>();
        ArrayList<Point> allCardinalTiles = new ArrayList<>();

        if (start.y > 0)
            allCardinalTiles.add(new Point(start.x, start.y - 1));

        if (start.x < gridWidth - 1)
            allCardinalTiles.add(new Point(start.x + 1, start.y));


        if (start.y < gridHeight - 1)
            allCardinalTiles.add(new Point(start.x, start.y + 1));

        if (start.x > 0)
            allCardinalTiles.add(new Point(start.x - 1, start.y));

        for (Point p : allCardinalTiles) {
            double dist = AStar.getDistance(p, target);
            if (dist == lowestDist)
                closestTiles.add(p);
            else if (dist < lowestDist) {
                lowestDist = dist;
                closestTiles.clear();
                closestTiles.add(p);
            }
        }

        return closestTiles;

    }

    public void updateHitbox() {
        hitbox.x = (int) x;
        hitbox.y = (int) y;
    }

    public void hurt(int damage) {
        health -= damage;
        if (health <= 0) {
            Play play = null;
            if (this instanceof Unit u)
                play = u.getUnitHandler().getPlay();
            else if (this instanceof Building b)
                play = b.getBuildingHandler().getPlay();

            if (play == null) return;

            if (this instanceof Unit u)
                play.getUnitHandler().killUnit(u);
            else if (this instanceof Building b)
                play.getBuildingHandler().killBuilding(b);

            if (play.getSelectedEntity() == this)
                play.setSelectedEntity(null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSubType() {
        return subType;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getEntityType() {
        return entityType;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getId() {
        return id;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public Player getPlayer() {
        return player;
    }
}
