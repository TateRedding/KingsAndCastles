package entities.resources;

import entities.Entity;

import java.awt.*;
import java.io.Serializable;

import static main.Game.*;

public abstract class ResourceObject extends Entity implements Serializable {

    // Resource SubTypes
    public static final int GOLD = 0;
    public static final int TREE = 1;
    public static final int ROCK = 2;
    public static final int COAL = 3;
    public static final int IRON = 4;

    protected int spriteId;
    protected int amount, maxAmount;

    public ResourceObject(int tileX, int tileY, int id, int resourceType, int spriteId) {
        super(null, RESOURCE, resourceType, toPixelX(tileX), toPixelY(tileY), id);
        this.spriteId = spriteId;
        this.maxAmount = getStartingTotal(resourceType);
        this.amount = maxAmount;
        this.hitbox = new Rectangle(toPixelX(tileX), toPixelY(tileY), TILE_SIZE, TILE_SIZE);
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public void setSpriteId(int spriteId) {
        this.spriteId = spriteId;
    }

}
