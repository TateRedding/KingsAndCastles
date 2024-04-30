package resources;

import java.awt.Rectangle;
import java.io.Serializable;

import main.Game;
import objects.GameObject;
import ui.bars.TopBar;

public abstract class Resource extends GameObject implements Serializable {

    public static final int GOLD_MINE = 0;
    public static final int TREE = 1;

    protected int tileX, tileY, resourceType;
    protected int currentAmount, totalAmount;

    public Resource(int tileX, int tileY, int id, int resourceType) {
        super(id);
        this.resourceType = resourceType;
        this.tileX = tileX;
        this.tileY = tileY;
        this.totalAmount = getStartingTotal(resourceType);
        this.currentAmount = totalAmount;

        hitbox = new Rectangle(tileX * Game.TILE_SIZE, tileY * Game.TILE_SIZE + TopBar.TOP_BAR_HEIGHT, getHitboxWidth(resourceType), getHitboxHeight(resourceType));
    }

    public static int getHitboxHeight(int resourceType) {
        return switch (resourceType) {
            case GOLD_MINE, TREE -> 32;
            default -> 0;
        };
    }

    public static int getHitboxWidth(int resourceType) {
        return switch (resourceType) {
            case GOLD_MINE, TREE -> 32;
            default -> 0;
        };
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
