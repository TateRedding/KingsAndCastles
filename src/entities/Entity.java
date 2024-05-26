package entities;

import objects.GameObject;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public abstract class Entity extends GameObject implements Serializable {

    public static final int UP = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;

    public static final int LABORER = 0;

    protected Player player;
    protected ArrayList<Point> path;

    protected int entityType;
    protected int health, maxHealth, damage;
    protected int attackTick, attackTickMax;
    protected int gatherTick, gatherTickMax = 50;
    protected float x, y, speed;

    protected boolean isAlive = true;

    public Entity(Player player, float x, float y, int entityType, int id) {
        super(id);
        this.player = player;
        this.x = x;
        this.y = y;
        this.entityType = entityType;
        this.maxHealth = getDefaultMaxHealth(entityType);
        this.health = maxHealth;
        this.damage = getDefaultDamage(entityType);
        this.attackTickMax = getDefaultAttackSpeed(entityType);
        this.speed = getDefaultMoveSpeed(entityType);
        this.hitbox = new Rectangle((int) x, (int) y, TILE_SIZE, TILE_SIZE);
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

    public void update() {
        if (isAlive) {
            if (path != null && !path.isEmpty()) {
                move();
            }
        }
    }

    protected void move() {
        // Check if Entity has reached the current path point based on movement speed
        float currentX = path.get(0).x * TILE_SIZE;
        float currentY = path.get(0).y * TILE_SIZE + TOP_BAR_HEIGHT;

        // Round to two decimal places to get rid of any floating point errors
        float roundX = Math.round(x * 100) / 100.0f;
        float roundY = Math.round(y * 100) / 100.0f;

        if (roundX >= currentX - speed && roundX <= currentX + speed && roundY >= currentY - speed
                && roundY <= currentY + speed) {
            x = currentX;
            y = currentY;
            path.remove(0);
        }
        if (path == null || path.size() <= 0)
            return;

        int direction = getDirection(path.get(0));
        moveInDirection(direction);
    }

    protected int getDirection(Point point) {
        int pX = point.x * TILE_SIZE;
        int pY = point.y * TILE_SIZE + TOP_BAR_HEIGHT;
        int dir = -1;

        if (pX == x && pY < y)
            dir = UP;
        else if (pX < x && pY == y)
            dir = LEFT;
        else if (pX > x && pY == y)
            dir = RIGHT;
        else if (pX == x && pY > y)
            dir = DOWN;

        return dir;
    }

    protected void moveInDirection(int direction) {
        switch (direction) {
            case UP:
                this.y -= speed;
                break;
            case LEFT:
                this.x -= speed;
                break;
            case RIGHT:
                this.x += speed;
                break;
            case DOWN:
                this.y += speed;
                break;
            default:
                break;
        }
        updateHitbox();
    }

    protected void updateHitbox() {
        hitbox.x = (int) x;
        hitbox.y = (int) y;
    }


    public int getEntityType() {
        return entityType;
    }

    public ArrayList<Point> getPath() {
        return path;
    }

    public void setPath(ArrayList<Point> path) {
        this.path = path;
    }
}
