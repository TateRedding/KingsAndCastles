package buildings;

import objects.GameObject;
import objects.SelectableGameObject;
import objects.Player;

import java.awt.*;

import static main.Game.TILE_SIZE;

public abstract class Building extends SelectableGameObject {

    // Building Types
    public static final int THRONE_ROOM = 0;
    public static final int CASTLE_WALL = 1;
    public static final int CASTLE_TURRET = 2;
    public static final int VILLAGE = 3;
    public static final int STORAGE_HUT = 4;
    public static final int REFINERY = 5;
    public static final int FARM = 6;
    public static final int FARM_ROTATED = 7;
    public static final int BARRACKS_TIER_1 = 8;
    public static final int BARRACKS_TIER_2 = 9;
    public static final int BARRACKS_TIER_3 = 10;

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
            case FARM, FARM_ROTATED -> 50;
            default -> 0;
        };
    }

    public static int getBuildingTileWidth(int buildingType) {
        return switch (buildingType) {
            case THRONE_ROOM, CASTLE_WALL, CASTLE_TURRET, VILLAGE, STORAGE_HUT, REFINERY, FARM -> 1;
            case FARM_ROTATED, BARRACKS_TIER_1, BARRACKS_TIER_2, BARRACKS_TIER_3 -> 2;
            default -> 0;
        };
    }

    public static int getBuildingTileHeight(int buildingType) {
        return switch (buildingType) {
            case THRONE_ROOM, CASTLE_WALL, CASTLE_TURRET, VILLAGE, STORAGE_HUT, REFINERY, FARM_ROTATED -> 1;
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
            case REFINERY -> 105;
            case FARM, FARM_ROTATED -> 20;
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
            case REFINERY -> 95;
            case FARM, FARM_ROTATED -> 20;
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
            case REFINERY -> 65;
            case FARM, FARM_ROTATED -> 45;
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
            case REFINERY -> "Refinery";
            case FARM, FARM_ROTATED -> "Farm";
            case BARRACKS_TIER_1 -> "Barracks - Tier 1";
            case BARRACKS_TIER_2 -> "Barracks - Tier 2";
            case BARRACKS_TIER_3 -> "Barracks - Tier 3";
            default -> "None";
        };
    }

    public static String[] getBuildingDetails(int buildingType) {
        return switch (buildingType) {
            case CASTLE_WALL -> new String[]{
                    "Basic castle",
                    "defense",
                    "",
                    "Must be built",
                    "in castle zones"
            };
            case CASTLE_TURRET -> new String[]{
                    "Improved castle",
                    "defense",
                    "",
                    "Must be built",
                    "on castle walls",
                    "",
                    "Must be armed",
                    "by a ranged unit",
            };
            case VILLAGE -> new String[]{
                    "Homes for your",
                    "general population",
                    "",
                    "Villages are required",
                    "to increase your population",
                    "",
                    "Each village can",
                    "hold ?? population"
            };
            case STORAGE_HUT -> new String[]{
                    "Storage for",
                    "logs and stone",
                    "",
                    "Laborers will deposit",
                    "their resources here"
            };
            case REFINERY -> new String[]{
                    "Refine iron ore",
                    "into iron or",
                    "iron and coal",
                    "into steel",
                    "",
                    "Laborers will deposit",
                    "thier ore here"
            };
            case FARM, FARM_ROTATED -> new String[]{
                    "Grow food for",
                    "your population",
                    "",
                    "Must be worked by",
                    "one or more laborers"
            };
            case BARRACKS_TIER_1 -> new String[]{
                    "Train basic",
                    "combat units",
                    "",
                    "Enter names of units here"
            };
            case BARRACKS_TIER_2 -> new String[]{
                    "Train advanced",
                    "combat units",
                    "",
                    "Enter names of units here"
            };
            case BARRACKS_TIER_3 -> new String[]{
                    "Train expert",
                    "combat units",
                    "",
                    "Enter names of units here"
            };
            default -> new String[]{"No", "Details"};
        };
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

}
