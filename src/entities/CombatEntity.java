package entities;

import entities.buildings.Building;
import entities.units.Unit;
import gamestates.Play;
import handlers.CombatEntityHandler;
import objects.Player;
import pathfinding.AStar;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.toTileX;
import static main.Game.toTileY;

public abstract class CombatEntity extends Entity implements Serializable {

    protected CombatEntityHandler combatEntityHandler;

    protected int damage;
    protected int actionTick = 0;
    protected int actionTickMax;
    protected int actionRange, sightRange;
    protected int health, maxHealth;

    public CombatEntity(Player player, int entityType, int subType, float x, float y, int id, CombatEntityHandler combatEntityHandler) {
        super(player, entityType, subType, x, y, id);
        this.combatEntityHandler = combatEntityHandler;
    }

    protected int getDefaultMaxHealth() {
        return 0;
    }

    public void hurt(int damage) {
        health -= damage;
        if (health <= 0) {
            Play play = combatEntityHandler.getPlay();

            if (this instanceof Unit u)
                play.getUnitHandler().killUnit(u);
            else if (this instanceof Building b)
                play.getBuildingHandler().killBuilding(b);

            if (play.getSelectedEntity() == this)
                play.setSelectedEntity(null);
        }
    }

    public boolean isLineOfSightOpen(Entity target) {
        Point attackerTile;
        Point targetTile;
        Play play = combatEntityHandler.getPlay();

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

    public int getActionRange() {
        return actionRange;
    }

    public int getDamage() {
        return damage;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSightRange() {
        return sightRange;
    }

    protected abstract int getDefaultMaxHealth(int buildingType);
}
