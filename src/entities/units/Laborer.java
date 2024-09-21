package entities.units;

import entities.buildings.Building;
import entities.buildings.Farm;
import entities.buildings.Refinery;
import entities.buildings.StorageHut;
import entities.resources.ResourceObject;
import handlers.UnitHandler;
import objects.Player;

import java.awt.*;
import java.util.ArrayList;

import static entities.buildings.Building.*;
import static entities.buildings.Refinery.R_MAX_COAL;
import static entities.buildings.Refinery.R_MAX_IRON;
import static entities.buildings.StorageHut.SH_MAX_LOGS;
import static entities.buildings.StorageHut.SH_MAX_STONE;
import static entities.resources.ResourceObject.*;
import static main.Game.*;
import static pathfinding.AStar.getUnitPathToNearestAdjacentTile;

public class Laborer extends Unit {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    // Inventory Maximums
    public static final int L_MAX_COAL = 20;
    public static final int L_MAX_IRON = 75;
    public static final int L_MAX_LOGS = 100;
    public static final int L_MAX_STONE = 50;

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
                    int subType = targetEntity.getSubType();
                    if (subType == STORAGE_HUT || subType == REFINERY) {
                        depositResources();
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
                    } else if ((subType == FARM || subType == FARM_ROTATED) && active)
                        startFarming();
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
            case TREE -> logs >= L_MAX_LOGS;
            case ROCK -> stone >= L_MAX_STONE;
            case IRON -> iron >= L_MAX_IRON;
            case COAL -> coal >= L_MAX_COAL;
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
            for (Building b : buildings) {
                if (b.getSubType() == buildingType) {
                    boolean hasRoom = switch (resourceType) {
                        case TREE -> ((StorageHut) b).getLogs() < SH_MAX_LOGS;
                        case ROCK -> ((StorageHut) b).getStone() < SH_MAX_STONE;
                        case IRON -> ((Refinery) b).getIron() < R_MAX_IRON;
                        case COAL -> ((Refinery) b).getCoal() < R_MAX_COAL;
                        default -> false;
                    };
                    if (!hasRoom) continue;
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
            }
            targetEntity = closest;
            path = pathToClosest;
            if (closest == null) {
                System.out.println("Could not locate a " + (buildingType == STORAGE_HUT ? "storage hut" : "refinery") + " with enough space!");
                setState(IDLE);
            }
        }
    }

    public boolean canDepositResources(Building building) {
        int buildingType = building.getSubType();
        return switch (buildingType) {
            case STORAGE_HUT: {
                StorageHut sh = (StorageHut) building;
                yield ((logs > 0 && sh.getLogs() < SH_MAX_LOGS) || (stone > 0 && sh.getStone() < SH_MAX_STONE));
            }
            case REFINERY: {
                Refinery r = (Refinery) building;
                yield ((iron > 0 && r.getIron() < R_MAX_IRON) || (coal > 0 && r.getCoal() < R_MAX_COAL));
            }
            default:
                yield false;
        };
    }

    private void depositResources() {
        int buildingType = targetEntity.getSubType();
        if (buildingType == STORAGE_HUT) {
            StorageHut sh = (StorageHut) targetEntity;

            int logsAmt = Math.min(logs, SH_MAX_LOGS - sh.getLogs());
            sh.addLogs(logsAmt);
            logs -= logsAmt;

            int stoneAmt = Math.min(stone, SH_MAX_STONE - sh.getStone());
            sh.addStone(stoneAmt);
            stone -= stoneAmt;
        } else if (buildingType == REFINERY) {
            Refinery r = (Refinery) targetEntity;

            int ironAmt = Math.min(iron, R_MAX_IRON - r.getIron());
            r.addIron(ironAmt);
            iron -= ironAmt;

            int coalAmt = Math.min(coal, R_MAX_COAL - r.getCoal());
            r.addCoal(coalAmt);
            coal -= coalAmt;
        }
    }

    private void locateAndTargetNearestResource(int resourceType, int tileX, int tileY) {
        unitHandler.getPlay().getResourceObjectHandler().locateAndTargetNearestResource(this, resourceType, tileX, tileY);
    }

    private void startFarming() {
        Farm targetFarm = (Farm) targetEntity;
        ArrayList<Laborer> currFarmers = targetFarm.getFarmers();
        int farmType = targetEntity.getSubType();
        int farmX = (int) targetEntity.getX();
        int farmY = (int) targetEntity.getY();

        active = false;
        x = farmX;
        y = farmY;
        if (!currFarmers.isEmpty()) {
            int offset = TILE_SIZE * currFarmers.size();
            if (farmType == FARM)
                y = farmY + offset;
            else if (farmType == FARM_ROTATED)
                x = farmX + offset;
        }
        if (unitHandler.getPlay().getSelectedEntity() == this)
            unitHandler.getPlay().setSelectedEntity(targetFarm);
        currFarmers.add(this);
        updateHitbox();
    }

    public void reactivate(int x, int y) {
        this.x = x;
        this.y = y;
        updateHitbox();
        reactivate();
    }

    public void reactivate() {
        targetEntity = null;
        setState(IDLE);
        direction = DOWN;
        active = true;
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
