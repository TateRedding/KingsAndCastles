package buildings;

import objects.GameObject;
import objects.SelectableGameObject;
import objects.Player;

import java.awt.*;

import static main.Game.TILE_SIZE;

public abstract class Building extends SelectableGameObject {

    // Building Types
    public static final int CASTLE_WALL = 0;
    public static final int CASTLE_TURRET = 1;
    public static final int VILLAGE = 2;
    public static final int STORAGE_HUT = 3;
    public static final int FARM = 4;
    public static final int BARRACKS_TIER_1 = 5;
    public static final int BARRACKS_TIER_2 = 6;
    public static final int BARRACKS_TIER_3 = 7;
    public static final int THRONE_ROOM = 8;

    protected int x, y, buildingType;

    public Building(Player player, int id, int x, int y, int buildingType) {
        super(player, GameObject.BUILDING, id);
        this.x = x;
        this.y = y;
        this.buildingType = buildingType;
        this.maxHealth = getDefaultMaxHealth(buildingType);
        this.health = maxHealth;
        this.hitbox = new Rectangle(x, y, getBuildingTileWidth(buildingType) * TILE_SIZE, getBuildingTileHeight(buildingType) * TILE_SIZE);
    }

    public static int getDefaultMaxHealth(int buildingType) {
        return switch (buildingType) {
            case FARM -> 50;
            default -> 0;
        };
    }

    public static int getBuildingTileWidth(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL, CASTLE_TURRET, VILLAGE, STORAGE_HUT, FARM, THRONE_ROOM -> 1;
            case BARRACKS_TIER_1, BARRACKS_TIER_2, BARRACKS_TIER_3 -> 2;
            default -> 0;
        };
    }

    public static int getBuildingTileHeight(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL, CASTLE_TURRET, VILLAGE, STORAGE_HUT, THRONE_ROOM -> 1;
            case FARM, BARRACKS_TIER_1, BARRACKS_TIER_2, BARRACKS_TIER_3 -> 2;
            default -> 0;
        };
    }

    public static int getCostGold(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL -> 100;
            case CASTLE_TURRET -> 150;
            case VILLAGE -> 30;
            case STORAGE_HUT -> 50;
            case FARM -> 20;
            case BARRACKS_TIER_1 -> 175;
            case BARRACKS_TIER_2 -> 250;
            case BARRACKS_TIER_3 -> 1000;
            default -> 0;
        };
    }

    public static int getCostLogs(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL -> 10;
            case CASTLE_TURRET -> 15;
            case VILLAGE -> 30;
            case STORAGE_HUT -> 50;
            case FARM -> 20;
            case BARRACKS_TIER_1 -> 100;
            case BARRACKS_TIER_2 -> 250;
            case BARRACKS_TIER_3 -> 1000;
            default -> 0;
        };
    }

    public static int getCostStone(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL -> 15;
            case CASTLE_TURRET -> 20;
            case VILLAGE -> 30;
            case STORAGE_HUT -> 55;
            case FARM -> 45;
            case BARRACKS_TIER_1 -> 110;
            case BARRACKS_TIER_2 -> 325;
            case BARRACKS_TIER_3 -> 1100;
            default -> 0;
        };
    }

    public static int getCostCoal(int buildingType) {
        return switch (buildingType) {
            case BARRACKS_TIER_2 -> 75;
            case BARRACKS_TIER_3 -> 200;
            default -> 0;
        };
    }

    public static int getCostIron(int buildingType) {
        return switch (buildingType) {
            case BARRACKS_TIER_1 -> 105;
            case BARRACKS_TIER_2 -> 190;
            case BARRACKS_TIER_3 -> 450;
            default -> 0;
        };
    }

    public static String getBuildingName(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL -> "Castle Wall";
            case CASTLE_TURRET -> "Turret";
            case VILLAGE -> "Village";
            case STORAGE_HUT -> "Storage Hut";
            case FARM -> "Farm";
            case BARRACKS_TIER_1 -> "Barrack - Tier 1";
            case BARRACKS_TIER_2 -> "Barrack - Tier 2";
            case BARRACKS_TIER_3 -> "Barrack - Tier 3";
            default -> "None";
        };
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

}
