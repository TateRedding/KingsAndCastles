package entities.units;

import gamestates.Play;
import handlers.UnitHandler;
import objects.Entity;
import objects.Player;
import pathfinding.AStar;
import utils.ImageLoader;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.units.Brute.ATTACKING;
import static entities.units.Laborer.CHOPPING;
import static entities.units.Laborer.MINING;
import static main.Game.*;
import static pathfinding.AStar.getUnitPathToNearestAdjacentTile;
import static pathfinding.AStar.isPointOpen;

public abstract class Unit extends Entity implements Serializable {

    // Attack Styles
    public static final int NONE = 0;
    public static final int MELEE = 1;
    public static final int RANGED = 2;

    // Unit SubTypes
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

    // All Unit States
    public static final int DEAD = 0;
    public static final int IDLE = 1;
    public static final int WALKING = 2;

    protected ArrayList<Point> path;
    protected UnitHandler unitHandler;

    protected int unitType;
    protected int damage;
    protected int actionTick = 0;
    protected int actionTickMax;
    protected int actionRange, sightRange;
    protected int direction = DOWN;
    protected int state = IDLE;
    protected int animationFrame = 0;
    protected int animationTick = 0;
    protected float speed;

    protected boolean isAlive = true;

    protected Entity targetEntity;

    public Unit(Player player, float x, float y, int unitType, int id, UnitHandler unitHandler) {
        super(player, UNIT, unitType, x, y, id);
        this.unitType = unitType;
        this.unitHandler = unitHandler;
        this.maxHealth = getDefaultMaxHealth(unitType);
        this.health = maxHealth;
        this.damage = getDefaultDamage(unitType);
        this.actionTickMax = getDefaultActionSpeed(unitType);
        this.actionRange = getDefaultActionRange(unitType);
        this.speed = getDefaultMoveSpeed(unitType);
        this.sightRange = getDefaultSightRange(unitType);
        this.hitbox = new Rectangle((int) x, (int) y, TILE_SIZE, TILE_SIZE);
    }

    public static int getDefaultMaxHealth(int unitType) {
        return switch (unitType) {
            case LABORER -> 50;
            case BRUTE -> 100;
            default -> 0;
        };
    }

    public static int getDefaultDamage(int unitType) {
        return switch (unitType) {
            case BRUTE -> 5;
            default -> 0;
        };
    }

    public static int getDefaultActionSpeed(int unitType) {
        return switch (unitType) {
            case LABORER -> Laborer.getMaxAnimationTick(CHOPPING) * Laborer.getNumberOfFrames(CHOPPING);
            case BRUTE -> Brute.getMaxAnimationTick(ATTACKING) * Brute.getNumberOfFrames(ATTACKING);
            default -> 0;
        };
    }

    public static float getDefaultMoveSpeed(int unitType) {
        return switch (unitType) {
            case LABORER -> 0.8f;
            case BRUTE -> 1.0f;
            default -> 0.0f;
        };
    }

    public static int getDefaultActionRange(int unitType) {
        return switch (unitType) {
            case LABORER, BRUTE -> 1;
            default -> 0;
        };
    }

    public static int getDefaultSightRange(int unitType) {
        return switch (unitType) {
            case LABORER -> 2;
            case BRUTE -> 5;
            default -> 0;
        };
    }

    public static BufferedImage getSprite(int unitType, int state, int dir, int frame) {
        return switch (unitType) {
            case LABORER -> ImageLoader.laborer[state][dir][frame];
            case BRUTE -> ImageLoader.brute[state][dir][frame];
            default -> null;
        };
    }

    public static int getAttackStyle(int unitType) {
        return switch (unitType) {
            case LABORER -> NONE;
            case BRUTE -> MELEE;
            default -> -1;
        };
    }

    public static int getActionFrameIndex(int unitType, int state) {
        // Which animation frame should the action be performed on?
        return switch (unitType) {
            case LABORER -> Laborer.getActionFrameIndex(state);
            case BRUTE -> Brute.getActionFrameIndex(state);
            default -> 0;
        };
    }

    private static int getNumberOfFrames(int unitType, int state) {
        return switch (unitType) {
            case LABORER -> Laborer.getNumberOfFrames(state);
            case BRUTE -> Brute.getNumberOfFrames(state);
            default -> 0;
        };
    }

    private static int getMaxAnimationTick(int unitType, int state) {
        return switch (unitType) {
            case LABORER -> Laborer.getMaxAnimationTick(state);
            case BRUTE -> Brute.getMaxAnimationTick(state);
            default -> 0;
        };
    }

    public void update() {
        if (isAlive) {
            if (path != null && !path.isEmpty())
                move();

            if (targetEntity != null
                    && ((targetEntity.getEntityType() == RESOURCE && (state == CHOPPING || state == MINING))
                    || (targetEntity.getEntityType() == UNIT && state == ATTACKING))) {
                turnTowardsTarget();
                actionTick++;
            }

        }
    }

    public boolean isTargetInRange(Entity target, int tileRange) {
        float startX = x - tileRange * TILE_SIZE;
        float startY = y - tileRange * TILE_SIZE;
        float size = (tileRange * 2 + 1) * TILE_SIZE;
        Ellipse2D range = new Ellipse2D.Double(startX, startY, size, size);
        Rectangle targetBounds = target.getHitbox();
        int middleX = targetBounds.x + targetBounds.width / 2;
        int middleY = targetBounds.y + targetBounds.height / 2;
        return range.contains(middleX, middleY);
    }

    public boolean isLineOfSightOpen(Entity target) {
        Point unitTile;
        Point targetTile;
        Play play = unitHandler.getPlay();

        if (path != null && !path.isEmpty())
            unitTile = path.getFirst();
        else
            unitTile = new Point(toTileX(hitbox.x), toTileY(hitbox.y));

        if (target.getEntityType() == UNIT && ((Unit) target).getPath() != null && !(((Unit) target).getPath().isEmpty()))
            targetTile = ((Unit) target).getPath().getFirst();
        else
            targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));

        Point currentTile = unitTile;

        while (!currentTile.equals(targetTile)) {
            ArrayList<Point> neighbors = getTilesClosestToTarget(currentTile, targetTile, play);

            if (neighbors.isEmpty())
                return false;

            Point nextTile = null;
            for (Point neighbor : neighbors) {
                if (neighbor.equals(targetTile))
                    return true;

                if (isPointOpen(neighbor, play, false)) {
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
        int targetX = toTileX(targetEntity.getX());
        int targetY = toTileY(targetEntity.getY());
        int unitX = toTileX(x);
        int unitY = toTileY(y);

        if (targetY < unitY)
            direction = UP;
        else if (targetX < unitX)
            direction = LEFT;
        else if (targetX > unitX)
            direction = RIGHT;
        else if (targetY > unitY)
            direction = DOWN;

    }

    protected void move() {
        if (state != WALKING)
            setState(WALKING);
        // Check if Unit has reached the current path point based on movement speed
        int currentX = toPixelX(path.getFirst().x);
        int currentY = toPixelY(path.getFirst().y);

        // Round to two decimal places to get rid of any floating point errors
        float roundX = Math.round(x * 100) / 100.0f;
        float roundY = Math.round(y * 100) / 100.0f;

        if (roundX >= currentX - speed && roundX <= currentX + speed && roundY >= currentY - speed
                && roundY <= currentY + speed) {
            x = currentX;
            y = currentY;
            updateHitbox();
            path.removeFirst();
            if (!path.isEmpty()) {
                Point next = path.getFirst();

                if (unitHandler.getPlay().isTileBlockedOrReserved(next.x, next.y, this, true)) {
//                    System.out.println("Next tile - [" + next.x + "," + next.y + "] - blocked. [UNIT - ID:" + id + " @ " + toTileX(x) + "," + toTileY(y) + "]");
                    Point start = new Point(toTileX(x), toTileY(y));
                    Point goal = path.getLast();
                    if (unitHandler.getPlay().isTileBlockedOrReserved(goal.x, goal.y, this, true)) {
//                        System.out.println("Goal tile - [" + goal.x + "," + goal.y + "] - blocked. [UNIT - ID:" + id + " @ " + toTileX(x) + "," + toTileY(y) + "]");
                        ArrayList<Point> newPath = getUnitPathToNearestAdjacentTile(this, goal.x, goal.y, unitHandler.getPlay());

//                        if (newPath == null)
//                            System.out.println("Can't re-route, goal entirely blocked.");
//                        else
//                            System.out.println("Re-routing. [UNIT - ID:" + id + " @ " + toTileX(x) + "," + toTileY(y) + "] [GOAL: " + newPath.get(newPath.size() - 1).x + "," + newPath.get(newPath.size() - 1).y + "]");

                        setPath(newPath);
                    } else {
                        path = AStar.pathFind(start, goal, unitHandler.getPlay());
//                        System.out.println("Re-calculating path to [" + goal.x + "," + goal.y + "] - blocked. [UNIT - ID:" + id + " @ " + toTileX(x) + "," + toTileY(y) + "]");
                    }
                }
            }
        }

        if (path == null || path.isEmpty()) {
            setState(IDLE);
            return;
        }

        setDirectionWithPath(path.getFirst());
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        this.animationFrame = 0;
        this.actionTick = getMaxAnimationTick(unitType, state) * (getNumberOfFrames(unitType, state) - getActionFrameIndex(unitType, state));
        this.animationTick = 0;
    }

    public int getDamage() {
        return damage;
    }

    public int getDirection() {
        return direction;
    }

    public ArrayList<Point> getPath() {
        return path;
    }

    public void setPath(ArrayList<Point> path) {
        this.path = path;
    }

    public int getSightRange() {
        return sightRange;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public UnitHandler getUnitHandler() {
        return unitHandler;
    }

}
