package buildings;

import objects.GameObject;
import objects.Player;

import java.awt.*;

import static main.Game.TILE_SIZE;

public abstract class Building extends GameObject {

    // Building Types
    public static final int FARM = 0;

    protected Player player;

    protected int x, y, health, maxHealth, buildingType;

    public Building(Player player, int id, int x, int y, int buildingType) {
        super(id);
        this.player = player;
        this.x = x;
        this.y = y;
        this.buildingType = buildingType;
        this.maxHealth = getDefaultMaxHealth(buildingType);
        this.health = maxHealth;
        this.hitbox = new Rectangle(x, y, getBuildingWidth(buildingType) * TILE_SIZE, getBuildingHeight(buildingType) * TILE_SIZE);
    }

    public static int getDefaultMaxHealth(int buildingType) {
        return switch (buildingType) {
            case FARM -> 50;
            default -> 0;
        };
    }

    public static int getBuildingWidth(int buildingType) {
        return switch (buildingType) {
            case FARM -> 1;
            default -> 0;
        };
    }

    public static int getBuildingHeight(int buildingType) {
        return switch (buildingType) {
            case FARM -> 1;
            default -> 0;
        };
    }

    public static int getCostGold(int buildingType) {
        return switch (buildingType) {
            case FARM -> 20;
            default -> 0;
        };
    }

    public static int getCostLogs(int buildingType) {
        return switch (buildingType) {
            case FARM -> 20;
            default -> 0;
        };
    }

    public static int getCostStone(int buildingType) {
        return switch (buildingType) {
            case FARM -> 15;
            default -> 0;
        };
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
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

    public Player getPlayer() {
        return player;
    }
}
