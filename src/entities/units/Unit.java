package entities.units;

import entities.buildings.CastleTurret;
import entities.buildings.Farm;
import gamestates.Play;
import handlers.UnitHandler;
import entities.Entity;
import objects.Player;
import pathfinding.AStar;
import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.Projectile.*;
import static entities.units.Brute.ATTACKING;
import static entities.units.Laborer.CHOPPING;
import static entities.units.Laborer.MINING;
import static main.Game.*;

public abstract class Unit extends Entity implements Serializable {

    // Attack Styles
    public static final int NONE = 0;
    public static final int MELEE = 1;
    public static final int RANGED = 2;

    // Unit SubTypes
    public static final int ARCHER = 0;
    public static final int BRUTE = 1;
    public static final int CROSSBOWMAN = 2;
    public static final int FOOT_SOLDIER = 3;
    public static final int KNIGHT = 4;
    public static final int LABORER = 5;
    public static final int STONE_THROWER = 6;

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

    private static final int MAX_CYCLES_WITHOUT_FOOD = 2;

    protected ArrayList<Point> path;
    protected UnitHandler unitHandler;

    protected int unitType;
    protected int damage;
    protected int attackStyle;
    protected int actionTick = 0;
    protected int actionTickMax;
    protected int actionRange, sightRange;
    protected int direction = DOWN;
    protected int state = IDLE;
    protected int animationFrame = 0;
    protected int animationTick = 0;
    protected float speed;

    protected int cyclesSinceLastFed = 0;

    protected Entity targetEntity;

    public Unit(Player player, float x, float y, int unitType, int id, UnitHandler unitHandler) {
        super(player, UNIT, unitType, x, y, id);
        this.unitType = unitType;
        this.unitHandler = unitHandler;
        this.maxHealth = getDefaultMaxHealth(unitType);
        this.health = maxHealth;
        this.damage = getDefaultDamage(unitType);
        this.attackStyle = getAttackStyle(unitType);
        this.actionTickMax = getDefaultActionSpeed(unitType);
        this.actionRange = getDefaultActionRange(unitType);
        this.speed = getDefaultMoveSpeed(unitType);
        this.sightRange = getDefaultSightRange(unitType);
        this.hitbox = new Rectangle((int) x, (int) y, TILE_SIZE, TILE_SIZE);
    }

    public static int getDefaultMaxHealth(int unitType) {
        return switch (unitType) {
            case LABORER -> 50;
            case BRUTE, STONE_THROWER -> 100;
            case ARCHER, FOOT_SOLDIER -> 175;
            case CROSSBOWMAN, KNIGHT -> 250;
            default -> 0;
        };
    }

    public static int getDefaultDamage(int unitType) {
        return switch (unitType) {
            case ARCHER -> 7;
            case BRUTE -> 5;
            case CROSSBOWMAN -> 11;
            case FOOT_SOLDIER -> 9;
            case KNIGHT -> 15;
            case STONE_THROWER -> 4;
            default -> 0;
        };
    }

    public static int getDefaultActionSpeed(int unitType) {
        return switch (unitType) {
            case LABORER -> Laborer.getMaxAnimationTick(CHOPPING) * Laborer.getNumberOfFrames(CHOPPING);
            case ARCHER, BRUTE, CROSSBOWMAN, FOOT_SOLDIER, KNIGHT, STONE_THROWER ->
                    CombatUnit.getMaxAnimationTick(ATTACKING) * CombatUnit.getNumberOfFrames(ATTACKING);
            default -> 0;
        };
    }

    public static float getDefaultMoveSpeed(int unitType) {
        return switch (unitType) {
            case ARCHER -> 1.35f;
            case BRUTE -> 1.0f;
            case CROSSBOWMAN -> 1.1f;
            case FOOT_SOLDIER -> 0.9f;
            case KNIGHT, LABORER -> 0.8f;
            case STONE_THROWER -> 1.2f;
            default -> 0.0f;
        };
    }

    public static int getDefaultActionRange(int unitType) {
        return switch (unitType) {
            case ARCHER -> 5;
            case BRUTE, FOOT_SOLDIER, KNIGHT, LABORER -> 1;
            case CROSSBOWMAN -> 7;
            case STONE_THROWER -> 3;
            default -> 0;
        };
    }

    public static int getDefaultSightRange(int unitType) {
        return switch (unitType) {
            case ARCHER, FOOT_SOLDIER -> 5;
            case BRUTE, STONE_THROWER -> 3;
            case CROSSBOWMAN, KNIGHT -> 7;
            case LABORER -> 2;
            default -> 0;
        };
    }

    public static BufferedImage getSprite(int unitType, int state, int dir, int frame) {

        return switch (unitType) {
            case ARCHER -> ImageLoader.archer[state][dir][frame];
            case BRUTE -> ImageLoader.brute[state][dir][frame];
            case CROSSBOWMAN -> ImageLoader.crowssbowman[state][dir][frame];
            case FOOT_SOLDIER -> ImageLoader.footSoldier[state][dir][frame];
            case KNIGHT -> ImageLoader.knight[state][dir][frame];
            case LABORER -> ImageLoader.laborer[state][dir][frame];
            case STONE_THROWER -> ImageLoader.stoneThrower[state][dir][frame];
            default -> null;
        };
    }

    public static int getAttackStyle(int unitType) {
        return switch (unitType) {
            case ARCHER, CROSSBOWMAN, STONE_THROWER -> RANGED;
            case BRUTE, FOOT_SOLDIER, KNIGHT -> MELEE;
            case LABORER -> NONE;
            default -> -1;
        };
    }

    public static int getActionFrameIndex(int unitType, int state) {
        // Which animation frame should the action be performed on?
        return switch (unitType) {
            case ARCHER, BRUTE, CROSSBOWMAN, FOOT_SOLDIER, KNIGHT, STONE_THROWER ->
                    CombatUnit.getActionFrameIndex(state);
            case LABORER -> Laborer.getActionFrameIndex(state);
            default -> 0;
        };
    }

    private static int getNumberOfFrames(int unitType, int state) {
        return switch (unitType) {
            case ARCHER, BRUTE, CROSSBOWMAN, FOOT_SOLDIER, KNIGHT, STONE_THROWER -> CombatUnit.getNumberOfFrames(state);
            case LABORER -> Laborer.getNumberOfFrames(state);
            default -> 0;
        };
    }

    private static int getMaxAnimationTick(int unitType, int state) {
        return switch (unitType) {
            case ARCHER, BRUTE, CROSSBOWMAN, FOOT_SOLDIER, KNIGHT, STONE_THROWER ->
                    CombatUnit.getMaxAnimationTick(state);
            case LABORER -> Laborer.getMaxAnimationTick(state);
            default -> 0;
        };
    }

    public static int getProjectileType(int unitType) {
        return switch (unitType) {
            case ARCHER -> ARROW;
            case CROSSBOWMAN -> BOLT;
            case STONE_THROWER -> THROWING_ROCK;
            default -> 0;
        };
    }

    private static String getUnitName(int unitType) {
        return switch (unitType) {
            case ARCHER -> "Archer";
            case BRUTE -> "Brute";
            case CROSSBOWMAN -> "Crowssbowman";
            case FOOT_SOLDIER -> "Foot Soldier";
            case KNIGHT -> "Knight";
            case LABORER -> "Laborer";
            case STONE_THROWER -> "Stone Thrower";
            default -> "Unknown unit";
        };
    }

    public void update() {
        if (active) {
            if (path != null && !path.isEmpty())
                move();

            if (targetEntity != null) {
                int targetEntityType = targetEntity.getEntityType();
                if ((targetEntityType == RESOURCE && (state == CHOPPING || state == MINING)
                        || ((targetEntityType == UNIT || targetEntityType == BUILDING) && state == ATTACKING))) {
                    turnTowardsTarget();
                    actionTick++;
                }
            }

            animationTick++;
            if (animationTick >= getMaxAnimationTick(subType, state)) {
                animationTick = 0;
                animationFrame++;
                if (animationFrame >= getNumberOfFrames(subType, state))
                    animationFrame = 0;
            }

        }
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
                // Check if they can reach their target from where they currently are
                if (targetEntity != null && isTargetActionable()) {
                    path = null;
                    setState(IDLE);
                    return;
                }

                Play play = unitHandler.getPlay();
                Point next = path.get(0);
                Point last = path.get(path.size() - 1);

                boolean isNextTileBlocked = play.isTileBlockedOrReserved(next.x, next.y, this);
                boolean isLastTileBlocked = play.isTileBlockedOrReserved(last.x, last.y, this);

                if (isLastTileBlocked) {
                    Point goal = targetEntity == null ? last : new Point(toTileX(targetEntity.getX()), toTileY(targetEntity.getY()));
                    if (isNextTileBlocked)
                        // Both the next and last tiles are blocked. Setting path to null ensures the next tile will not be used
                        setPath(null);
                    setPath(AStar.getUnitPathToNearestAdjacentTile(this, goal.x, goal.y, play));
                } else {
                    if (isNextTileBlocked) {
                        // Next tile is blocked, last tile is open
                        Point start = new Point(toTileX(x), toTileY(y));
                        path = AStar.pathFind(start, last, play);
                    }
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

    private boolean isTargetActionable() {
        if (isTargetSamePlayer()) {
            if ((targetEntity instanceof CastleTurret && attackStyle == RANGED) || (targetEntity instanceof Farm && this instanceof Laborer))
                return isTargetInRange(targetEntity, 1);
        } else
            return isTargetInRange(targetEntity, actionRange) && isLineOfSightOpen(targetEntity);
        return false;
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

    protected boolean isTargetSamePlayer() {
        return targetEntity != null && targetEntity.getPlayer().getPlayerID() == player.getPlayerID();
    }

    public void eat() {
        cyclesSinceLastFed = 0;
        player.setFood(player.getFood() - 1);
    }

    public void starve() {
        cyclesSinceLastFed++;
        if (cyclesSinceLastFed > MAX_CYCLES_WITHOUT_FOOD) {
            System.out.println(getUnitName(unitType) + " ID: " + id + " has starved to death!");
            active = false;
            if (unitHandler.getPlay().getSelectedEntity() == this)
                unitHandler.getPlay().setSelectedEntity(null);
        }
    }

    public void reactivate(int x, int y) {
        this.x = x;
        this.y = y;
        updateHitbox();
        reactivate();
    }

    public void reactivate() {
        targetEntity = null;
        setState(IDLE);
        direction = DOWN;
        active = true;
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

    public int getAnimationFrame() {
        return animationFrame;
    }

    public int getCyclesSinceLastFed() {
        return cyclesSinceLastFed;
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

    public void setDirection(int direction) {
        this.direction = direction;
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
