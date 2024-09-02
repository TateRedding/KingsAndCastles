package handlers;

import entities.resources.ResourceObject;
import entities.units.Brute;
import entities.units.Unit;
import entities.units.Laborer;
import gamestates.Play;
import objects.Entity;
import objects.Player;
import pathfinding.AStar;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static entities.units.Unit.*;
import static main.Game.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class UnitHandler implements Serializable {

    private static final int NUM_MAX_STARTING_UNITS = 2;

    private Play play;
    private ArrayList<Unit> units = new ArrayList<>();

    private int id = 0;

    public UnitHandler(Play play) {
        this.play = play;
        createStartingUnits();
    }

    public void update() {
        for (Unit u : units) {
            if (u.isAlive()) {
                u.update();
                int unitType = u.getSubType();

                // Auto-attack
                if (unitType != LABORER && u.getState() == IDLE && u.getTargetEntity() == null)
                    findEnemyToAttack(u);

                if (u.getActionTick() >= u.getActionTickMax()) {
                    if (unitType == LABORER)
                        play.getResourceObjectHandler().gatherResource(u.getPlayer(), (ResourceObject) u.getTargetEntity(), (Laborer) u);
                    else
                        attack(u, (Unit) u.getTargetEntity());
                    u.setActionTick(0);
                }
            }
        }
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Unit u : units) {
            if (u.isAlive()) {
                int dir = u.getDirection();
                if (dir == UP_LEFT || dir == DOWN_LEFT)
                    dir = LEFT;
                else if (dir == UP_RIGHT || dir == DOWN_RIGHT)
                    dir = RIGHT;

                g.drawImage(Unit.getSprite(u.getSubType(), u.getState(), dir, u.getAnimationFrame()), u.getHitbox().x - xOffset, u.getHitbox().y - yOffset, null);

                if (u.getHealth() < u.getMaxHealth())
                    u.drawHealthBar(g, u.getHealth(), u.getMaxHealth(), xOffset, yOffset);

                // Debugging
                drawPath(u, g, xOffset, yOffset);
                drawHitbox(u, g, xOffset, yOffset);
                drawTargetHitbox(u, g, xOffset, yOffset);
            }
        }
    }

    private void createStartingUnits() {
        ArrayList<ArrayList<Point>> castleZones = play.getMap().getCastleZones();
        ArrayList<Player> players = play.getPlayers();
        Random random = new Random(play.getSeed());

        for (int i = 0; i < players.size(); i++) {
            int maxStartingUnits = Math.min(NUM_MAX_STARTING_UNITS, castleZones.get(i).size());
            ArrayList<Point> spawnPoints = new ArrayList<>(castleZones.get(i));
            Collections.shuffle(spawnPoints, random);
            for (int j = 0; j < maxStartingUnits + 1; j++) {
                Point spawn = spawnPoints.get(j);
                // Debugging - Starting with one Brute each. Remove this check and just spawn laborers for production
                if (j == maxStartingUnits)
                    createUnit(players.get(i), spawn, BRUTE);
                else
                    createUnit(players.get(i), spawn, LABORER);
            }
        }
    }

    public void createUnit(Player player, Point spawn, int unitType) {
        switch (unitType) {
            case LABORER -> units.add(new Laborer(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case BRUTE -> units.add(new Brute(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
        }
        player.setPopulation(player.getPopulation() + 1);
    }

    private void drawPath(Unit u, Graphics g, int xOffset, int yOffset) {
        if (u.getPath() != null && !u.getPath().isEmpty()) {
            g.setColor(new Color(255, 0, 255, 100));
            for (Point p : u.getPath()) {
                g.fillRect(toPixelX(p.x) - xOffset, toPixelY(p.y) - yOffset, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawHitbox(Unit u, Graphics g, int xOffset, int yOffset) {
        g.setColor(Color.RED);
        Rectangle bounds = u.getHitbox();
        g.drawRect(bounds.x - xOffset, bounds.y - yOffset, bounds.width, bounds.height);
    }

    private void drawTargetHitbox(Unit u, Graphics g, int xOffset, int yOffset) {
        Entity e = u.getTargetEntity();
        if (e != null) {
            g.setColor(Color.BLUE);
            Rectangle bounds = e.getHitbox();
            g.drawRect(bounds.x - xOffset, bounds.y - yOffset, bounds.width, bounds.height);
        }
    }

    private void findEnemyToAttack(Unit attacker) {
        for (Unit target : units) {
            if (target.isAlive() && target.getPlayer().getPlayerID() != attacker.getPlayer().getPlayerID() && attacker.isTargetInRange(target, attacker.getSightRange())) {
                if (attacker.isTargetInRange(target, attacker.getActionRange()) && attacker.isLineOfSightOpen(target)) {
                    attacker.setTargetEntity(target);
                    return;
                }

                Point targetTile;
                ArrayList<Point> targetPath = target.getPath();
                if (targetPath != null && !targetPath.isEmpty())
                    targetTile = targetPath.get(0);
                else
                    targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));
                ArrayList<Point> path = attacker.getUnitHandler().getPathToNearestAdjacentTile(attacker, targetTile.x, targetTile.y);
                if (path != null) {
                    attacker.setPath(path);
                    attacker.setTargetEntity(target);
                    return;
                }
            }
        }
    }

    private void attack(Unit attacker, Unit target) {
        if (attacker.getSubType() == -1 || target.getSubType() == -1)
            return;

        target.setHealth(target.getHealth() - attacker.getDamage());

        // Auto-retaliate
        if (target.getSubType() != LABORER && target.getTargetEntity() == null && target.getState() == IDLE)
            target.setTargetEntity(attacker);

        if (target.getHealth() <= 0) {
            target.setAlive(false);
            attacker.setState(IDLE);
            attacker.setTargetEntity(null);
        }
    }

    public void setPathToTile(Unit u, int tileX, int tileY) {
        Point goal = new Point(tileX, tileY);
        if (u.getPath() != null && !u.getPath().isEmpty()) {
            ArrayList<Point> path = AStar.pathFind(u.getPath().get(0), goal, play);
            if (path != null) {
                path.add(0, u.getPath().get(0));
                u.setPath(path);
            }
        } else {
            Point start = new Point(toTileX(u.getHitbox().x), toTileY(u.getHitbox().y));
            u.setPath(AStar.pathFind(start, goal, play));
        }
    }

    public ArrayList<Point> getPathToNearestAdjacentTile(Unit u, int goalTileX, int goalTileY) {
        HashMap<Double, Point> openTiles = new HashMap<Double, Point>();
        Point start;
        if (u.getPath() != null && !u.getPath().isEmpty())
            start = u.getPath().get(0);
        else
            start = new Point(toTileX(u.getHitbox().x), toTileY(u.getHitbox().y));

        for (int x = goalTileX - 1; x < goalTileX + 2; x++)
            for (int y = goalTileY - 1; y < goalTileY + 2; y++) {
                if (x < 0 || y < 0 || x >= play.getMap().getTileData()[0].length || y >= play.getMap().getTileData().length)
                    continue;
                Point currTarget = new Point(x, y);
                if (AStar.isPointOpen(currTarget, play)) {
                    boolean isCardinal = (x == goalTileX || y == goalTileY);

                    if (!isCardinal && !isAdjacentDiagonalOpen(new Point(x, y), new Point(goalTileX, goalTileY)))
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
                if (u.getPath() != null && !u.getPath().isEmpty())
                    path.add(0, u.getPath().get(0));
                return path;
            }
        }
        return null;
    }

    public boolean isAdjacentDiagonalOpen(Point origin, Point target) {
        // Check if point in vertical direction of target & cardinal of the origin is open
        Point verticalPoint = new Point(origin.x, origin.y + (target.y - origin.y));
        boolean isVerticalPointOpen = (play.getEntityAtTile(verticalPoint.x, verticalPoint.y) == null && AStar.isPointOpen(verticalPoint, play));
        if (isVerticalPointOpen)
            return true;

        // Check if point in horizontal direction of target & cardinal of the origin is open
        Point horizontalPoint = new Point(origin.x + (target.x - origin.x), origin.y);
        return (play.getEntityAtTile(horizontalPoint.x, horizontalPoint.y) == null && AStar.isPointOpen(horizontalPoint, play));
    }

    public Unit getUnitAtCoord(int x, int y, boolean checkEntireTile) {
        if (checkEntireTile) {
            Rectangle tileBounds = new Rectangle(x / TILE_SIZE * TILE_SIZE, y / TILE_SIZE * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            for (Unit u : units)
                if (u.isAlive() && u.getHitbox().intersects(tileBounds))
                    return u;
        } else
            for (Unit u : units)
                if (u.isAlive() && u.getHitbox().contains(x, y))
                    return u;
        return null;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public Play getPlay() {
        return play;
    }
}
