package resources;

import java.awt.Rectangle;
import java.io.Serializable;

import main.Game;
import objects.GameObject;
import ui.bars.TopBar;

public abstract class ResourceObjects extends GameObject implements Serializable {

    public static final int GOLD_MINE = 0;
    public static final int TREE = 1;
    public static final int ROCK = 2;
    public static final int COAL_MINE = 3;
    public static final int IRON_MINE = 4;

    protected int tileX, tileY, resourceType;
    protected int currentAmount, totalAmount;

    public ResourceObjects(int tileX, int tileY, int id, int resourceType) {
        super(id);
        this.resourceType = resourceType;
        this.tileX = tileX;
        this.tileY = tileY;
        this.totalAmount = getStartingTotal(resourceType);
        this.currentAmount = totalAmount;

        hitbox = new Rectangle(tileX * Game.TILE_SIZE, tileY * Game.TILE_SIZE + TopBar.TOP_BAR_HEIGHT, Game.TILE_SIZE, Game.TILE_SIZE);
    }

    public static int getStartingTotal(int resourceType) {
        return switch (resourceType) {
            case GOLD_MINE -> 1000;
            case TREE -> 20;
            default -> 0;
        };
    }

    public static int getAmountPerAction(int resourceType) {
        return switch (resourceType) {
            case GOLD_MINE -> 1;
            case TREE -> 4;
            default -> 0;
        };
    }

    public static int getMaxVeinSize(int resourceType) {
        return switch (resourceType) {
            case COAL_MINE -> 5;
            case IRON_MINE -> 16;
            default -> 0;
        };
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int getResourceType() {
        return resourceType;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

}
