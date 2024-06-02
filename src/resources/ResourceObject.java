package resources;

import java.awt.Rectangle;
import java.io.Serializable;

import main.Game;
import objects.GameObject;
import ui.bars.TopBar;

public abstract class ResourceObject extends GameObject implements Serializable {

    protected int tileX, tileY, spriteId;
    protected int currentAmount, totalAmount;

    public ResourceObject(int tileX, int tileY, int id, int resourceType, int spriteId) {
        super(id, RESOURCE, resourceType);
        this.spriteId = spriteId;
        this.tileX = tileX;
        this.tileY = tileY;
        this.totalAmount = getStartingTotal(resourceType);
        this.currentAmount = totalAmount;

        hitbox = new Rectangle(tileX * Game.TILE_SIZE, tileY * Game.TILE_SIZE + TopBar.TOP_BAR_HEIGHT, Game.TILE_SIZE, Game.TILE_SIZE);
    }

    public static int getStartingTotal(int resourceType) {
        return switch (resourceType) {
            case GOLD -> 1000;
            case TREE -> 20;
            case ROCK -> 45;
            case COAL -> 200;
            case IRON -> 60;
            default -> 0;
        };
    }

    public static int getAmountPerAction(int resourceType) {
        return switch (resourceType) {
            case GOLD -> 1;
            case TREE, IRON -> 4;
            case ROCK -> 5;
            case COAL -> 2;
            default -> 0;
        };
    }

    public static int getMaxPerChunk(int resourceType) {
        return switch (resourceType) {
            case ROCK -> 32;
            case COAL -> 5;
            case IRON -> 24;
            default -> 0;
        };
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public void setSpriteId(int spriteId) {
        this.spriteId = spriteId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

}
