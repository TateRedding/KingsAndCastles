package entities;

import objects.GameObject;
import objects.Player;
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

    // Directions
    public static final int UP = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;

    // All Entity Animations
    public static final int IDLE = 0;
    public static final int WALK = 1;

    protected ArrayList<Point> path;
    protected Player player;

    protected int entityType;
    protected int health, maxHealth, damage;
    protected int actionTick = 0;
    protected int actionTickMax;
    protected int actionRange, sightRange;
    protected int direction = DOWN;
    protected int animation = IDLE;
    protected int animationFrame = 0;
    protected int animationTick = 0;
    protected float x, y, speed;

    protected boolean isAlive = true;

    protected ResourceObject resourceToGather;
    protected Entity entityToAttack;

    public Entity(Player player, float x, float y, int entityType, int id) {
        super(id);
        this.player = player;
        this.x = x;
        this.y = y;
        this.entityType = entityType;
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

    public static BufferedImage getSprite(int entityType, int animation, int dir, int frame) {
        return switch (entityType) {
            case LABORER -> ImageLoader.laborer[animation][dir][frame];
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
                if (animation != WALK && animation != IDLE)
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
            targetX = (int) (entityToAttack.getHitbox().getX() / TILE_SIZE);
            targetY = (int) ((entityToAttack.getHitbox().getY() - TOP_BAR_HEIGHT) / TILE_SIZE);
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
        animation = WALK;
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
        }
        if (path.isEmpty()) {
            animation = IDLE;
            return;
        }

        setDirectionWithPath(path.get(0));
        moveInDirection(direction);
    }

    protected void setDirectionWithPath(Point point) {
        int pX = point.x * TILE_SIZE;
        int pY = point.y * TILE_SIZE + TOP_BAR_HEIGHT;

        if (pX == x && pY < y)
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

    public int getAnimation() {
        return animation;
    }

    public void setAnimation(int animation) {
        this.animation = animation;
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
