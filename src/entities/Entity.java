package entities;

import objects.GameObject;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;

public abstract class Entity extends GameObject implements Serializable {

    public static final int LABORER = 1;

    protected Player player;
    protected Point destination;
    protected ArrayList<Point> path;

    protected int entityType;
    protected int health, maxHealth, damage;
    protected int attackTick, attackTickMax;
    protected int gatherTick, gatherTickMax = 50;
    protected float x, y, speed;

    public Entity(Player player, int x, int y, int entityType, int id) {
        super(id);
        this.player = player;
        this.entityType = entityType;
        this.maxHealth = getDefaultMaxHealth(entityType);
        this.health = maxHealth;
        this.damage = getDefaultDamage(entityType);
        this.attackTickMax = getDefaultAttackSpeed(entityType);
        this.speed = getDefaultMoveSpeed(entityType);
        this.hitbox = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
    }

    public static int getDefaultMaxHealth(int entityType) {
        return switch (entityType) {
            case LABORER -> 50;
            default -> 0;
        };
    }

    public static int getDefaultDamage(int entityType) {
        return switch (entityType) {
            case LABORER -> 2;
            default -> 0;
        };
    }

    public static int getDefaultAttackSpeed(int entityType) {
        return switch (entityType) {
            case LABORER -> 50;
            default -> 0;
        };
    }

    public static float getDefaultMoveSpeed(int entityType) {
        return switch (entityType) {
            case LABORER -> 0.8f;
            default -> 0.0f;
        };
    }

    public static BufferedImage getSprite(int entityType) {
        return switch (entityType) {
            case LABORER -> ImageLoader.laborer;
            default -> null;
        };
    }

    public int getEntityType() {
        return entityType;
    }
}
