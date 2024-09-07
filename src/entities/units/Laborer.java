package entities.units;

import entities.buildings.Building;
import entities.buildings.StorageHut;
import entities.resources.ResourceObject;
import handlers.UnitHandler;
import objects.Player;

import java.awt.*;
import java.util.ArrayList;

import static entities.buildings.Building.REFINERY;
import static entities.buildings.Building.STORAGE_HUT;
import static entities.resources.ResourceObject.*;
import static java.lang.Float.POSITIVE_INFINITY;
import static main.Game.toTileX;
import static main.Game.toTileY;
import static pathfinding.AStar.getUnitPathToNearestAdjacentTile;

public class Laborer extends Unit {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    // Inventory Maximums
    public static final int MAX_COAL = 50;
    public static final int MAX_IRON = 50;
    public static final int MAX_LOGS = 250;
    public static final int MAX_STONE = 100;

    private int coal, iron, logs, stone;
    private ArrayList<Building> closestDepositBuildings = new ArrayList<>();

    public Laborer(Player player, int x, int y, int id, UnitHandler unitHandler) {
        super(player, x, y, LABORER, id, unitHandler);
    }

    public static int getNumberOfFrames(int state) {
        return switch (state) {
            case IDLE, WALKING -> 4;
            case CHOPPING, MINING -> 5;
            default -> 1;
        };
    }

    public static int getMaxAnimationTick(int state) {
        return switch (state) {
            case IDLE -> 25;
            case WALKING, CHOPPING, MINING -> 15;
            default -> 20;
        };
    }

    public static int getActionFrameIndex(int state) {
        return switch (state) {
            case CHOPPING, MINING -> 3;
            default -> getNumberOfFrames(state);
        };
    }

    public void update() {
        super.update();
        animationTick++;
        if (animationTick >= getMaxAnimationTick(state)) {
            animationTick = 0;
            animationFrame++;
            if (animationFrame >= getNumberOfFrames(state))
                animationFrame = 0;
        }

        if (targetEntity != null && targetEntity.getEntityType() == RESOURCE) {
            if (state == IDLE)
                setState((targetEntity.getSubType() == TREE) ? CHOPPING : MINING);
        } else if (state == CHOPPING || state == MINING)
            setState(IDLE);

        // Need to add logic of what to when closesDepositBuildings has something in it, from emptyInventory.
        // Perhaps a boolean like isEmptying thats true when emptyInventory is called, but can be interrupted?
    }

    public boolean isInventoryFull(int resourceType) {
        return switch(resourceType) {
            case TREE -> logs >= MAX_LOGS;
            case ROCK -> stone >= MAX_STONE;
            case IRON -> iron >= MAX_IRON;
            case COAL -> coal >= MAX_COAL;
            default -> false;
        };

    }

    public void emptyInventory() {
        ArrayList<Building> buildings = unitHandler.getPlay().getBuildingHandler().getBuildings();
        closestDepositBuildings.clear();
        if (logs > 0 || stone > 0) {
            Building closestStorageHut = null;
            int shortestDist = (int) POSITIVE_INFINITY;
            for (Building b : buildings)
                if (b.getSubType() == STORAGE_HUT) {
                    if (isTargetInRange(b, actionRange) && isLineOfSightOpen(b)) {
                        closestStorageHut = b;
                        break;
                    }
                    ArrayList<Point> shPath = getUnitPathToNearestAdjacentTile(this, toTileX(b.getX()), toTileY(b.getY()), unitHandler.getPlay());
                    if (shPath != null && shPath.size() < shortestDist) {
                        closestStorageHut = b;
                        shortestDist = shPath.size();
                    }
                }
            if (closestStorageHut != null)
                closestDepositBuildings.add(closestStorageHut);
        }

        if (iron > 0 || coal > 0) {
            Building closestRefinery = null;
            int shortestDist = (int) POSITIVE_INFINITY;
            for (Building b : buildings)
                if (b.getSubType() == REFINERY) {
                    if (isTargetInRange(b, actionRange) && isLineOfSightOpen(b)) {
                        closestRefinery = b;
                        break;
                    }
                    ArrayList<Point> shPath = getUnitPathToNearestAdjacentTile(this, toTileX(b.getX()), toTileY(b.getY()), unitHandler.getPlay());
                    if (shPath != null && shPath.size() < shortestDist) {
                        closestRefinery = b;
                        shortestDist = shPath.size();
                    }
                }
            if (closestRefinery != null)
                closestDepositBuildings.add(closestRefinery);
        }
    }

    public int getCoal() {
        return coal;
    }

    public void setCoal(int coal) {
        this.coal = coal;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getLogs() {
        return logs;
    }

    public void setLogs(int logs) {
        this.logs = logs;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }
}
