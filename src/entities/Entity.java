package entities;

import handlers.EntityHandler;
import objects.GameObject;
import objects.Player;
import pathfinding.AStar;
import resources.ResourceObject;
import utils.ImageLoader;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public abstract class Entity extends GameObject implements Serializable {

    // Attack Styles
    public static final int NONE = 0;
    public static final int MELEE = 1;
    public static final int RANGED = 2;

    // Entity Types
    public static final int LABORER = 0;

    // Cardinal Directions
    public static final int UP = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;

    // Diagonal Directions
    public static final int UP_LEFT = 4;
    public static final int DOWN_LEFT = 5;
    public static final int UP_RIGHT = 6;
    public static final int DOWN_RIGHT = 7;

    // All Entity States
    public static final int DEAD = 0;
    public static final int IDLE = 1;
    public static final int WALKING = 2;

    protected ArrayList<Point> path;
    protected Player player;
    protected EntityHandler entityHandler;

    protected int entityType;
    protected int health, maxHealth, damage;
    protected int actionTick = 0;
    protected int actionTickMax;
    protected int actionRange, sightRange;
    protected int direction = DOWN;
    protected int state = IDLE;
    protected int animationFrame = 0;
    protected int animationTick = 0;
    protected float x, y, speed;

    protected boolean isAlive = true;

    protected ResourceObject resourceToGather;
    protected Entity entityToAttack;

    public Entity(Player player, float x, float y, int entityType, int id, EntityHandler entityHandler) {
        super(id);
        this.player = player;
        this.x = x;
        this.y = y;
        this.entityType = entityType;
        this.entityHandler = entityHandler;
        this.maxHealth = getDefaultMaxHealth(entityType);
        this.health = maxHealth;
        this.damage = getDefaultDamage(entityType);
        this.actionTickMax = getDefaultActionSpeed(entityType);
        this.actionRange = getDefaultActionRange(entityType);
        this.speed = getDefaultMoveSpeed(entityType);
        this.sightRange = getDefaultSightRange(entityType);
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
            case LABORER -> 0;
            default -> 0;
        };
    }

    public static int getDefaultActionSpeed(int entityType) {
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

    public static int getDefaultActionRange(int entityType) {

        return switch (entityType) {
            case LABORER -> 1;
            default -> 0;
        };
    }

    public static int getDefaultSightRange(int entityType) {
        return switch (entityType) {
            case LABORER -> 1;
            default -> 0;
        };
    }

    public static BufferedImage getSprite(int entityType, int state, int dir, int frame) {
        return switch (entityType) {
            case LABORER -> ImageLoader.laborer[state][dir][frame];
            default -> null;
        };
    }

    public static int getAttackStyle(int entityType) {
        return switch (entityType) {
            case LABORER -> NONE;
            default -> -1;
        };
    }

    public void update() {
        if (isAlive) {
            if (path != null && !path.isEmpty())
                move();
            if ((resourceToGather != null && isTargetInRange(resourceToGather)) || entityToAttack != null && isTargetInRange(entityToAttack)) {
                if (state != WALKING && state != IDLE)
                    turnTowardsTarget();
                actionTick++;
            }

        }
    }

    public boolean isTargetInRange(GameObject target) {
        double startX = x - actionRange * TILE_SIZE;
        double startY = y - actionRange * TILE_SIZE;
        double size = (actionRange * 2 + 1) * TILE_SIZE;
        Ellipse2D range = new Ellipse2D.Double(startX, startY, size, size);
        Rectangle targetBounds = target.getHitbox();
        int middleX = targetBounds.x + targetBounds.width / 2;
        int middleY = targetBounds.y + targetBounds.height / 2;
        return range.contains(middleX, middleY);
    }

    protected void turnTowardsTarget() {
        int targetX = 0;
        int targetY = 0;
        int entityX = (int) (x / TILE_SIZE);
        int entityY = (int) ((y - TOP_BAR_HEIGHT) / TILE_SIZE);

        if (resourceToGather != null) {
            targetX = resourceToGather.getTileX();
            targetY = resourceToGather.getTileY();
        } else if (entityToAttack != null) {
            targetX = (int) (entityToAttack.getHitbox().x / TILE_SIZE);
            targetY = (int) ((entityToAttack.getHitbox().y - TOP_BAR_HEIGHT) / TILE_SIZE);
        }

        if (targetY < entityY)
            direction = UP;
        else if (targetX < entityX)
            direction = LEFT;
        else if (targetX > entityX)
            direction = RIGHT;
        else if (targetY > entityY)
            direction = DOWN;

    }

    protected void move() {
        state = WALKING;
        // Check if Entity has reached the current path point based on movement speed
        int currentX = path.get(0).x * TILE_SIZE;
        int currentY = path.get(0).y * TILE_SIZE + TOP_BAR_HEIGHT;

        // Round to two decimal places to get rid of any floating point errors
        float roundX = Math.round(x * 100) / 100.0f;
        float roundY = Math.round(y * 100) / 100.0f;

        if (roundX >= currentX - speed && roundX <= currentX + speed && roundY >= currentY - speed
                && roundY <= currentY + speed) {
            x = currentX;
            y = currentY;
            updateHitbox();
            path.remove(0);
            if (!path.isEmpty()) {
                Point next = path.get(0);
                Entity e = entityHandler.getEntityAtCoord(next.x * TILE_SIZE, next.y * TILE_SIZE + TOP_BAR_HEIGHT, true);
                if (e != null) {
                    Point start = new Point(hitbox.x / TILE_SIZE, (hitbox.y - TOP_BAR_HEIGHT) / TILE_SIZE);
                    path = AStar.pathFind(start, path.get(path.size() - 1), entityHandler.getPlay());
                }
            }
        }

        if (path == null || path.isEmpty()) {
            state = IDLE;
            return;
        }

        setDirectionWithPath(path.get(0));
        moveInDirection(direction);
    }

    protected void setDirectionWithPath(Point point) {
        int pX = point.x * TILE_SIZE;
        int pY = point.y * TILE_SIZE + TOP_BAR_HEIGHT;

        // Diagonal Directions
        if (pX < x && pY < y)
            direction = UP_LEFT;
        else if (pX > x && pY < y)
            direction = UP_RIGHT;
        else if (pX < x && pY > y)
            direction = DOWN_LEFT;
        else if (pX > x && pY > y)
            direction = DOWN_RIGHT;

            // Cardinal Directions
        else if (pX == x && pY < y)
            direction = UP;
        else if (pX < x && pY == y)
            direction = LEFT;
        else if (pX > x && pY == y)
            direction = RIGHT;
        else if (pX == x && pY > y)
            direction = DOWN;
    }

    protected void moveInDirection(int direction) {
        switch (direction) {
            // Cardinal Directions
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

            // Diagonal Directions
            case UP_LEFT:
                this.x -= speed * Math.sqrt(0.5);
                this.y -= speed * Math.sqrt(0.5);
                break;
            case UP_RIGHT:
                this.x += speed * Math.sqrt(0.5);
                this.y -= speed * Math.sqrt(0.5);
                break;
            case DOWN_LEFT:
                this.x -= speed * Math.sqrt(0.5);
                this.y += speed * Math.sqrt(0.5);
                break;
            case DOWN_RIGHT:
                this.x += speed * Math.sqrt(0.5);
                this.y += speed * Math.sqrt(0.5);
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

    public int getActionTickMax() {
        return actionTickMax;
    }

    public void setActionTick(int actionTick) {
        this.actionTick = actionTick;
    }

    public int getActionTick() {
        return actionTick;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getAnimationFrame() {
        return animationFrame;
    }

    public void setAnimationFrame(int animationFrame) {
        this.animationFrame = animationFrame;
    }

    public int getAnimationTick() {
        return animationTick;
    }

    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDamage() {
        return damage;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Entity getEntityToAttack() {
        return entityToAttack;
    }

    public void setEntityToAttack(Entity entityToAttack) {
        this.entityToAttack = entityToAttack;
    }

    public int getEntityType() {
        return entityType;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public ArrayList<Point> getPath() {
        return path;
    }

    public void setPath(ArrayList<Point> path) {
        this.path = path;
    }

    public Player getPlayer() {
        return player;
    }

    public ResourceObject getResourceToGather() {
        return resourceToGather;
    }

    public void setResourceToGather(ResourceObject resourceToGather) {
        this.resourceToGather = resourceToGather;
    }
}
