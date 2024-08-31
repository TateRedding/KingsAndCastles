package entities;

import gamestates.Play;
import handlers.EntityHandler;
import objects.GameObject;
import objects.SelectableGameObject;
import objects.Player;
import pathfinding.AStar;
import resources.ResourceObject;
import utils.ImageLoader;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.Laborer.CHOPPING;
import static entities.Laborer.MINING;
import static main.Game.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public abstract class Entity extends SelectableGameObject implements Serializable {

    // Attack Styles
    public static final int NONE = 0;
    public static final int MELEE = 1;
    public static final int RANGED = 2;

    // Entity Types
    public static final int LABORER = 0;
    public static final int BRUTE = 1;

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
    public static final int ATTACKING = 3;

    protected ArrayList<Point> path;
    protected EntityHandler entityHandler;

    protected int entityType;
    protected int damage;
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
        super(player, GameObject.ENTITY, id);
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
            case BRUTE -> 100;
            default -> 0;
        };
    }

    public static int getDefaultDamage(int entityType) {
        return switch (entityType) {
            case BRUTE -> 5;
            default -> 0;
        };
    }

    public static int getDefaultActionSpeed(int entityType) {
        return switch (entityType) {
            case LABORER -> Laborer.getMaxAnimationTick(CHOPPING) * Laborer.getNumberOfFrames(CHOPPING);
            case BRUTE -> Brute.getMaxAnimationTick(ATTACKING) * Brute.getNumberOfFrames(ATTACKING);
            default -> 0;
        };
    }

    public static float getDefaultMoveSpeed(int entityType) {
        return switch (entityType) {
            case LABORER -> 0.8f;
            case BRUTE -> 1.0f;
            default -> 0.0f;
        };
    }

    public static int getDefaultActionRange(int entityType) {

        return switch (entityType) {
            case LABORER, BRUTE -> 1;
            default -> 0;
        };
    }

    public static int getDefaultSightRange(int entityType) {
        return switch (entityType) {
            case LABORER -> 2;
            case BRUTE -> 5;
            default -> 0;
        };
    }

    public static BufferedImage getSprite(int entityType, int state, int dir, int frame) {
        return switch (entityType) {
            case LABORER -> ImageLoader.laborer[state][dir][frame];
            case BRUTE -> ImageLoader.brute[state][dir][frame];
            default -> null;
        };
    }

    public static int getAttackStyle(int entityType) {
        return switch (entityType) {
            case LABORER -> NONE;
            case BRUTE -> MELEE;
            default -> -1;
        };
    }

    public static int getActionFrameIndex(int entityType, int state) {
        // Which animation frame should the action be performed on?
        return switch (entityType) {
            case LABORER -> Laborer.getActionFrameIndex(state);
            case BRUTE -> Brute.getActionFrameIndex(state);
            default -> 0;
        };
    }

    private static int getNumberOfFrames(int entityType, int state) {
        return switch (entityType) {
            case LABORER -> Laborer.getNumberOfFrames(state);
            case BRUTE -> Brute.getNumberOfFrames(state);
            default -> 0;
        };
    }

    private static int getMaxAnimationTick(int entityType, int state) {
        return switch (entityType) {
            case LABORER -> Laborer.getMaxAnimationTick(state);
            case BRUTE -> Brute.getMaxAnimationTick(state);
            default -> 0;
        };
    }

    public void update() {
        if (isAlive) {
            if (path != null && !path.isEmpty())
                move();
            // Below will need to check if an entity is also in a combat state if entityToAttack is not null.
            // It may also need to ensure the target is still within attacking range, should the target be moving.
            if ((resourceToGather != null && (state == CHOPPING || state == MINING)) || (entityToAttack != null && state == ATTACKING)) {
                if (state != WALKING && state != IDLE)
                    turnTowardsTarget();
                actionTick++;
            }

        }
    }

    public boolean isTargetInRange(GameObject target, int tileRange) {
        float startX = x - tileRange * TILE_SIZE;
        float startY = y - tileRange * TILE_SIZE;
        float size = (tileRange * 2 + 1) * TILE_SIZE;
        Ellipse2D range = new Ellipse2D.Double(startX, startY, size, size);
        Rectangle targetBounds = target.getHitbox();
        int middleX = targetBounds.x + targetBounds.width / 2;
        int middleY = targetBounds.y + targetBounds.height / 2;
        return range.contains(middleX, middleY);
    }

    public boolean isLineOfSightOpen(GameObject target) {
        Point entityTile;
        Point targetTile;
        Play play = entityHandler.getPlay();

        if (path != null && !path.isEmpty())
            entityTile = path.get(0);
        else
            entityTile = new Point(toTileX(hitbox.x), toTileY(hitbox.y)); // Default to entity's current tile

        if (target.getGameObjectType() == ENTITY && ((Entity) target).getPath() != null && !(((Entity) target).getPath().isEmpty()))
            targetTile = ((Entity) target).getPath().get(0);
        else
            targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y)); // Default to target's current tile

        Point currentTile = entityTile;

        while (!currentTile.equals(targetTile)) {
            ArrayList<Point> neighbors = getTilesClosestToTarget(currentTile, targetTile, play);

            if (neighbors.isEmpty())
                return false;

            Point nextTile = null;
            for (Point neighbor : neighbors) {
                if (neighbor.equals(targetTile))
                    return true;

                if (AStar.isPointOpen(neighbor, play)) {
                    nextTile = neighbor;
                    break;
                }
            }

            if (nextTile != null)
                currentTile = nextTile;
            else
                return false;
        }
        return true;
    }


    private ArrayList<Point> getTilesClosestToTarget(Point start, Point target, Play play) {
        double lowestDist = Double.POSITIVE_INFINITY;
        int gridWidth = play.getMap().getTileData()[0].length;
        int gridHeight = play.getMap().getTileData().length;

        ArrayList<Point> closestTiles = new ArrayList<>();
        ArrayList<Point> allCardinalTiles = new ArrayList<>();

        if (start.y > 0)
            allCardinalTiles.add(new Point(start.x, start.y - 1));

        if (start.x < gridWidth - 1)
            allCardinalTiles.add(new Point(start.x + 1, start.y));


        if (start.y < gridHeight - 1)
            allCardinalTiles.add(new Point(start.x, start.y + 1));

        if (start.x > 0)
            allCardinalTiles.add(new Point(start.x - 1, start.y));

        for (Point p : allCardinalTiles) {
            double dist = AStar.getDistance(p, target);
            if (dist == lowestDist)
                closestTiles.add(p);
            else if (dist < lowestDist) {
                lowestDist = dist;
                closestTiles.clear();
                closestTiles.add(p);
            }
        }

        return closestTiles;

    }

    protected void turnTowardsTarget() {
        int targetX = 0;
        int targetY = 0;
        int entityX = toTileX((int) x);
        int entityY = toTileY((int) y);

        if (resourceToGather != null) {
            targetX = resourceToGather.getTileX();
            targetY = resourceToGather.getTileY();
        } else if (entityToAttack != null) {
            targetX = toTileX(entityToAttack.getHitbox().x);
            targetY = toTileY(entityToAttack.getHitbox().y);
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
        if (state != WALKING)
            setState(WALKING);
        // Check if Entity has reached the current path point based on movement speed
        int currentX = toPixelX(path.get(0).x);
        int currentY = toPixelY(path.get(0).y);

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
                Entity e = entityHandler.getEntityAtCoord(toPixelX(next.x), toPixelY(next.y), true);
                if (e != null) {
                    Point start = new Point(toTileX(hitbox.x), toTileY(hitbox.y));
                    path = AStar.pathFind(start, path.get(path.size() - 1), entityHandler.getPlay());
                }
            }
        }

        if (path == null || path.isEmpty()) {
            setState(IDLE);
            return;
        }

        setDirectionWithPath(path.get(0));
        moveInDirection(direction);
    }

    protected void setDirectionWithPath(Point point) {
        int pX = toPixelX(point.x);
        int pY = toPixelY(point.y);

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

    public int getActionRange() {
        return actionRange;
    }

    public void setActionRange(int actionRange) {
        this.actionRange = actionRange;
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

    public boolean isAlive() {
        return isAlive;
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
        this.animationFrame = 0;
        this.actionTick = getMaxAnimationTick(entityType, state) * (getNumberOfFrames(entityType, state) - getActionFrameIndex(entityType, state));
        this.animationTick = 0;
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

    public EntityHandler getEntityHandler() {
        return entityHandler;
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

    public int getSightRange() {
        return sightRange;
    }

    public ArrayList<Point> getPath() {
        return path;
    }

    public void setPath(ArrayList<Point> path) {
        this.path = path;
    }

    public ResourceObject getResourceToGather() {
        return resourceToGather;
    }

    public void setResourceToGather(ResourceObject resourceToGather) {
        this.resourceToGather = resourceToGather;
    }
}
