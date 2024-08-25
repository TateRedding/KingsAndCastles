package buildings;

import objects.GameObject;
import objects.SelectableGameObject;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

import static main.Game.TILE_SIZE;

public abstract class Building extends SelectableGameObject {

    // Building Types
    public static final int FARM = 0;

    protected int x, y, buildingType;

    public Building(Player player, int id, int x, int y, int buildingType) {
        super(player, GameObject.BUILDING, id);
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

    public static int getCostCoal(int buildingType) {
        return switch (buildingType) {
            default -> 0;
        };
    }

    public static int getCostIron(int buildingType) {
        return switch (buildingType) {
            default -> 0;
        };
    }

    public static String getBuildingName(int buildingType) {
        return switch (buildingType) {
            case FARM -> "Farm";
            default -> "None";
        };
    }

    public static BufferedImage getBuildingSprite(int buildingType) {
        return switch (buildingType) {
            case FARM -> ImageLoader.buildings[0];
            default -> null;
        };
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

}