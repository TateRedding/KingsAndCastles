package handlers;

import entities.Brute;
import entities.Entity;
import entities.Laborer;
import gamestates.Play;
import objects.GameObject;
import objects.Player;
import pathfinding.AStar;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static entities.Entity.*;
import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class EntityHandler implements Serializable {

    private Play play;
    private ArrayList<Entity> entities = new ArrayList<>();

    private int id = 0;
    private GameObject targetObject;

    public EntityHandler(Play play) {
        this.play = play;
        createStartingEntities();
    }

    public void update() {
        for (Entity e : entities) {
            if (e.isAlive()) {
                e.update();
                int entityType = e.getEntityType();

                // Auto-attack
                if (entityType != LABORER && e.getState() == IDLE && e.getEntityToAttack() == null)
                    findEntityToAttack(e);

                if (e.getActionTick() >= e.getActionTickMax()) {
                    if (entityType == LABORER)
                        play.getResourceObjectHandler().gatherResource(e.getPlayer(), e.getResourceToGather(), (Laborer) e);
                    else
                        attack(e, e.getEntityToAttack());
                    e.setActionTick(0);
                }
            }
        }
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities) {
            if (e.isAlive()) {
                int dir = e.getDirection();
                if (dir == UP_LEFT || dir == DOWN_LEFT)
                    dir = LEFT;
                else if (dir == UP_RIGHT || dir == DOWN_RIGHT)
                    dir = RIGHT;

                g.drawImage(Entity.getSprite(e.getEntityType(), e.getState(), dir, e.getAnimationFrame()), e.getHitbox().x - (xOffset * TILE_SIZE), e.getHitbox().y - (yOffset * TILE_SIZE), null);

                if (e.getHealth() < e.getMaxHealth())
                    e.drawHealthBar(g, e.getHealth(), e.getMaxHealth(), xOffset, yOffset);

                // Debugging
                drawPath(e, g, xOffset, yOffset);
                drawHitbox(e, g, xOffset, yOffset);
                drawTargetHitbox(e, g, xOffset, yOffset);
            }
        }
    }

    private void createStartingEntities() {
        ArrayList<ArrayList<Point>> castleZones = play.getMap().getCastleZones();
        ArrayList<Player> players = play.getPlayers();
        Random random = new Random(play.getSeed());

        for (int i = 0; i < players.size(); i++) {
            int maxStartingEntities = Math.min(players.get(i).getPopulation(), castleZones.get(i).size());
            ArrayList<Point> spawnPoints = new ArrayList<>(castleZones.get(i));
            Collections.shuffle(spawnPoints, random);
            for (int j = 0; j < maxStartingEntities + 1; j++) {
                Point spawn = spawnPoints.get(j);
                // Debugging - Starting with one Brute each. Remove this check and just spawn laborers for production
                if (j == maxStartingEntities)
                    entities.add(new Brute(players.get(i), spawn.x * TILE_SIZE, spawn.y * TILE_SIZE + TOP_BAR_HEIGHT, id++, this));
                else
                    entities.add(new Laborer(players.get(i), spawn.x * TILE_SIZE, spawn.y * TILE_SIZE + TOP_BAR_HEIGHT, id++, this));
            }
        }
    }

    private void drawPath(Entity e, Graphics g, int xOffset, int yOffset) {
        if (e.getPath() != null && !e.getPath().isEmpty()) {
            g.setColor(new Color(255, 0, 255, 100));
            for (Point p : e.getPath()) {
                g.fillRect((p.x - xOffset) * TILE_SIZE, (p.y - yOffset) * TILE_SIZE + TOP_BAR_HEIGHT, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawHitbox(Entity e, Graphics g, int xOffset, int yOffset) {
        g.setColor(Color.RED);
        Rectangle bounds = e.getHitbox();
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawTargetHitbox(Entity e, Graphics g, int xOffset, int yOffset) {
        GameObject go = e.getResourceToGather() == null ? e.getEntityToAttack() : e.getResourceToGather();
        if (go != null) {
            g.setColor(Color.BLUE);
            Rectangle bounds = go.getHitbox();
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void findEntityToAttack(Entity attacker) {
        for (Entity target : entities) {
            if (target.getPlayer().getPlayerID() != attacker.getPlayer().getPlayerID() && attacker.isTargetInRange(target, attacker.getSightRange())) {
                if (attacker.isTargetInRange(target, attacker.getActionRange()) && attacker.isLineOfSightOpen(target)) {
                    attacker.setEntityToAttack(target);
                    return;
                }

                Point targetTile;
                ArrayList<Point> targetPath = target.getPath();
                if (targetPath != null && !targetPath.isEmpty())
                    targetTile = targetPath.get(0);
                else
                    targetTile = new Point(target.getHitbox().x / TILE_SIZE, (target.getHitbox().y - TOP_BAR_HEIGHT) / TILE_SIZE);
                ArrayList<Point> path = attacker.getEntityHandler().getPathToNearestAdjacentTile(attacker, targetTile.x, targetTile.y);
                if (path != null) {
                    attacker.setPath(path);
                    attacker.setEntityToAttack(target);
                    return;
                }
            }
        }
    }

    private void attack(Entity attacker, Entity target) {
        target.setHealth(target.getHealth() - attacker.getDamage());

        // Auto-retaliate
        if (target.getEntityType() != LABORER && target.getEntityToAttack() == null && target.getState() == IDLE)
            target.setEntityToAttack(attacker);

        if (target.getHealth() <= 0) {
            target.setAlive(false);
            attacker.setState(IDLE);
            attacker.setEntityToAttack(null);
        }
    }

    public void setPathToTile(Entity e, int tileX, int tileY) {
        Point goal = new Point(tileX, tileY);
        if (e.getPath() != null && !e.getPath().isEmpty()) {
            ArrayList<Point> path = AStar.pathFind(e.getPath().get(0), goal, play);
            if (path != null) {
                path.add(0, e.getPath().get(0));
                e.setPath(path);
            }
        } else {
            Point start = new Point(e.getHitbox().x / TILE_SIZE, (e.getHitbox().y - TOP_BAR_HEIGHT) / TILE_SIZE);
            e.setPath(AStar.pathFind(start, goal, play));
        }
    }

    public ArrayList<Point> getPathToNearestAdjacentTile(Entity e, int tileX, int tileY) {
        HashMap<Double, Point> openTiles = new HashMap<Double, Point>();
        Point start;
        if (e.getPath() != null && !e.getPath().isEmpty())
            start = e.getPath().get(0);
        else
            start = new Point(e.getHitbox().x / TILE_SIZE, (e.getHitbox().y - TOP_BAR_HEIGHT) / TILE_SIZE);

        for (int x = tileX - 1; x < tileX + 2; x++)
            for (int y = tileY - 1; y < tileY + 2; y++) {
                if (x < 0 || y < 0 || x >= play.getMap().getTileData()[0].length || y >= play.getMap().getTileData().length)
                    continue;
                Point currTarget = new Point(x, y);
                if (AStar.isPointOpen(currTarget, play)) {
                    boolean isCardinal = (x == tileX || y == tileY);

                    if (!isCardinal && !isAdjacentDiagonalOpen(new Point(x, y), new Point(tileX, tileY)))
                        continue;

                    double distance = AStar.getDistance(start, currTarget);

                    if (!isCardinal)
                        distance *= 2;
                    openTiles.put(distance, currTarget);
                }
            }

        if (openTiles.isEmpty()) {
            return null;
        }

        ArrayList<Double> sorted = new ArrayList<>(openTiles.keySet());
        Collections.sort(sorted);

        for (int i = 0; i < openTiles.size(); i++) {
            Point target = openTiles.get(sorted.get(i));
            ArrayList<Point> path = AStar.pathFind(start, target, play);
            if (path != null) {
                if (e.getPath() != null && !e.getPath().isEmpty())
                    path.add(0, e.getPath().get(0));
                return path;
            }
        }
        return null;
    }

    public boolean isAdjacentDiagonalOpen(Point origin, Point target) {
        // Check if point in vertical direction of target & cardinal of the origin is open
        Point verticalPoint = new Point(origin.x, origin.y + (target.y - origin.y));
        boolean isVerticalPointOpen = (play.getGameObjectAt(verticalPoint.x * TILE_SIZE, verticalPoint.y * TILE_SIZE + TOP_BAR_HEIGHT, true) == null && AStar.isPointOpen(verticalPoint, play));
        if (isVerticalPointOpen)
            return true;

        // Check if point in horizontal direction of target & cardinal of the origin is open
        Point horizontalPoint = new Point(origin.x + (target.x - origin.x), origin.y);
        return (play.getGameObjectAt(horizontalPoint.x * TILE_SIZE, horizontalPoint.y * TILE_SIZE + TOP_BAR_HEIGHT, true) == null && AStar.isPointOpen(horizontalPoint, play));
    }

    public Entity getEntityAtCoord(int x, int y, boolean checkEntireTile) {
        if (checkEntireTile) {
            Rectangle tileBounds = new Rectangle(x / TILE_SIZE * TILE_SIZE, y / TILE_SIZE * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            for (Entity e : entities)
                if (e.isAlive() && e.getHitbox().intersects(tileBounds))
                    return e;
        } else
            for (Entity e : entities)
                if (e.isAlive() && e.getHitbox().contains(x, y))
                    return e;
        return null;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public Play getPlay() {
        return play;
    }

    public GameObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(GameObject targetObject) {
        this.targetObject = targetObject;
    }
}
