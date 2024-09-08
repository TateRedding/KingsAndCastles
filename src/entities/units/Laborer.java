package entities.units;

import entities.buildings.Building;
import entities.resources.ResourceObject;
import handlers.UnitHandler;
import objects.Player;

import java.awt.*;
import java.util.ArrayList;

import static entities.buildings.Building.REFINERY;
import static entities.buildings.Building.STORAGE_HUT;
import static entities.resources.ResourceObject.*;
import static main.Game.toTileX;
import static main.Game.toTileY;
import static pathfinding.AStar.getUnitPathToNearestAdjacentTile;

public class Laborer extends Unit {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    // Inventory Maximums
    public static final int MAX_COAL = 20;
    public static final int MAX_IRON = 35;
    public static final int MAX_LOGS = 50;
    public static final int MAX_STONE = 50;

    private int coal, iron, logs, stone;

    private Point previousTargetTile;
    private int previousTargetType = -1;

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

        if (targetEntity != null) {
            int entityType = targetEntity.getEntityType();

            if (entityType == RESOURCE) {
                if (state == IDLE && isTargetInRange(targetEntity, actionRange))
                    setState(targetEntity.getSubType() == TREE ? CHOPPING : MINING);
            } else if (state == CHOPPING || state == MINING)
                setState(IDLE);

            if (entityType == BUILDING) {
                if (state == IDLE && isTargetInRange(targetEntity, actionRange)) {
                    emptyInventory();
                    if (previousTargetTile != null && previousTargetType != -1) {
                        ResourceObject previousTarget = unitHandler.getPlay().getResourceObjectData()[previousTargetTile.y][previousTargetTile.x];
                        if (previousTarget != null) {
                            path = getUnitPathToNearestAdjacentTile(this, previousTargetTile.x, previousTargetTile.y, unitHandler.getPlay());
                            if (path != null)
                                targetEntity = previousTarget;
                        } else
                            locateAndTargetNearestResource(previousTargetType, previousTargetTile.x, previousTargetTile.y);
                    }
                    clearPreviousTarget();
                }
            }
        }
    }

    public void clearPreviousTarget() {
        previousTargetTile = null;
        previousTargetType = -1;
    }

    public boolean isInventoryFull(int resourceType) {
        return switch (resourceType) {
            case TREE -> logs >= MAX_LOGS;
            case ROCK -> stone >= MAX_STONE;
            case IRON -> iron >= MAX_IRON;
            case COAL -> coal >= MAX_COAL;
            default -> false;
        };

    }

    public void targetClosestDepositBuilding(int resourceType) {
        int buildingType = switch (resourceType) {
            case TREE, ROCK -> STORAGE_HUT;
            case IRON, COAL -> REFINERY;
            default -> -1;
        };

        if (buildingType != -1) {
            ArrayList<Building> buildings = unitHandler.getPlay().getBuildingHandler().getBuildings();
            Building closest = null;
            ArrayList<Point> pathToClosest = null;
            for (Building b : buildings)
                if (b.getSubType() == buildingType) {
                    if (isTargetInRange(b, actionRange) && isLineOfSightOpen(b)) {
                        closest = b;
                        break;
                    }
                    ArrayList<Point> currPath = getUnitPathToNearestAdjacentTile(this, toTileX(b.getX()), toTileY(b.getY()), unitHandler.getPlay());
                    if (currPath != null && (pathToClosest == null || (currPath.size() < pathToClosest.size()))) {
                        closest = b;
                        pathToClosest = currPath;
                    }
                }
            targetEntity = closest;
            path = pathToClosest;
            if (closest == null) {
                System.out.println("Could not locate a " + (buildingType == STORAGE_HUT ? "storage hut" : "refinery") + "!");
                setState(IDLE);
            }
        }
    }

    public boolean hasResourcesToDeposit(int buildingType) {
        return switch (buildingType) {
            case STORAGE_HUT -> logs > 0 || stone > 0;
            case REFINERY -> iron > 0 || coal > 0;
            default -> false;
        };
    }

    private void emptyInventory() {
        int buildingType = targetEntity.getSubType();
        if (buildingType == STORAGE_HUT) {
            player.setLogs(player.getLogs() + logs);
            player.setStone(player.getStone() + stone);
            logs = 0;
            stone = 0;
        } else if (buildingType == REFINERY) {
            player.setIron(player.getIron() + iron);
            player.setCoal(player.getCoal() + coal);
            iron = 0;
            coal = 0;
        }
    }

    private void locateAndTargetNearestResource(int resourceType, int tileX, int tileY) {
        unitHandler.getPlay().getResourceObjectHandler().locateAndTargetNearestResource(this, resourceType, tileX, tileY);
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

    public void setPreviousTargetTile(Point previousTargetTile) {
        this.previousTargetTile = previousTargetTile;
    }

    public void setPreviousTargetType(int previousTargetType) {
        this.previousTargetType = previousTargetType;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }
}
