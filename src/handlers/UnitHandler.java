package handlers;

import entities.resources.ResourceObject;
import entities.units.Brute;
import entities.units.Laborer;
import entities.units.Unit;
import gamestates.Debug;
import gamestates.Play;
import objects.Entity;
import objects.Player;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static entities.units.Unit.*;
import static main.Game.*;
import static pathfinding.AStar.getUnitPathToNearestAdjacentTile;

public class UnitHandler implements Serializable {

    private static final Font DEBUG_UNIT_ID_FONT = new Font("Monospaced", Font.BOLD, 14);
    private static final int NUM_MAX_STARTING_UNITS = 3;

    private Play play;
    private ArrayList<Unit> units = new ArrayList<>();
    private ArrayList<Unit> deadUnits = new ArrayList<>();

    private int id = 0;

    public UnitHandler(Play play) {
        this.play = play;
        createStartingUnits();
    }

    public void update() {
        if (!deadUnits.isEmpty())
            cleanupUnitList();

        for (Unit u : units) {
            if (u.isAlive()) {
                u.update();
                int unitType = u.getSubType();

                // Check if target has moved
                if (unitType != LABORER && u.getState() == WALKING && u.getTargetEntity() != null)
                    changePathIfTargetMovedOutOfActionRange(u);

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

    private void cleanupUnitList() {
        for (Unit deadUnit : deadUnits) units.remove(deadUnit);
        deadUnits.clear();
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
                if (Debug.config.get(Debug.DebugToggle.SHOW_PATHS))
                    drawPath(u, g, xOffset, yOffset);
                if (Debug.config.get(Debug.DebugToggle.SHOW_HITBOXES))
                    drawHitbox(u, g, xOffset, yOffset);
                if (Debug.config.get(Debug.DebugToggle.SHOW_TARGET_HITBOXES))
                    drawTargetHitbox(u, g, xOffset, yOffset);
                if (Debug.config.get(Debug.DebugToggle.SHOW_UNIT_IDS))
                    drawUnitIDs(u, g, xOffset, yOffset);
            }
        }
    }

    private void createStartingUnits() {
        ArrayList<ArrayList<Point>> castleZones = play.getMap().getCastleZones();
        ArrayList<Player> players = play.getPlayers();
        Random random = new Random(play.getSeed());

        // Debugging
        int maxLaborers = 2;

        for (int i = 0; i < players.size(); i++) {
            // Debugging
            int numLaborers = 0;

            int maxStartingUnits = Math.min(NUM_MAX_STARTING_UNITS, castleZones.get(i).size());
            ArrayList<Point> spawnPoints = new ArrayList<>(castleZones.get(i));
            Collections.shuffle(spawnPoints, random);
            for (int j = 0; j < maxStartingUnits; j++) {
                Point spawn = spawnPoints.get(j);

                // Debugging
                if (numLaborers < maxLaborers) {
                    createUnit(players.get(i), spawn, LABORER);
                    numLaborers++;
                    continue;
                }

                createUnit(players.get(i), spawn, BRUTE);
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

    private void drawUnitIDs(Unit u, Graphics g, int xOffset, int yOffset) {
        Rectangle bounds = u.getHitbox();
        String id = String.valueOf(u.getId());
        int xStart = (bounds.x - xOffset) + (bounds.width - g.getFontMetrics().stringWidth(id)) / 2;
        int yStart = (bounds.y - yOffset) + (bounds.height - g.getFontMetrics().getHeight());
        g.setFont(DEBUG_UNIT_ID_FONT);
        g.setColor(Color.RED);
        g.drawString(id, xStart, yStart);
    }

    private void changePathIfTargetMovedOutOfActionRange(Unit u) {
        ArrayList<Point> currPath = u.getPath();
        Entity target = u.getTargetEntity();
        if (currPath == null || currPath.isEmpty() || target.getEntityType() != UNIT)
            return;
        Point targetTile = getTargetTile((Unit) target);
        Point currPathEnd = u.getPath().get(u.getPath().size() - 1);

        if (Math.abs(targetTile.x - currPathEnd.x) > u.getActionRange() || Math.abs(targetTile.y - currPathEnd.y) > u.getActionRange()) {
//            System.out.println("Target moved out of action range. [UNIT - ID:" + u.getId() +
//                    " @ Tile: " + toTileX(u.getHitbox().x) + "," + toTileY(u.getHitbox().y) +
//                    " Coord: " + u.getHitbox().x + "," + u.getHitbox().y + "] [TARGET - ID:" + target.getId() +
//                    " @ Tile: " + toTileX(target.getX()) + "," + toTileY(target.getY()) +
//                    " Coord: " + target.getX() + "," + target.getY() + "]");
            ArrayList<Point> newPath = getUnitPathToNearestAdjacentTile(u, targetTile.x, targetTile.y, play);

            if (newPath == null) {
//                System.out.println("Can't re-route, target entirely blocked. Stopping at next tile [" + currPath.get(0) + "]");
                newPath = new ArrayList<>();
                newPath.add(currPath.get(0));
            }
//            else
//                System.out.println("Re-routing. [UNIT - ID:" + u.getId() + " @ " + toTileX(u.getHitbox().x) + "," + toTileY(u.getHitbox().y) + "] [GOAL: " + newPath.get(newPath.size() - 1).x + "," + newPath.get(newPath.size() - 1).y + "]");

            u.setPath(newPath);
        }
    }

    private void findEnemyToAttack(Unit attacker) {
        for (Unit target : units) {
            if (target.isAlive() && target.getPlayer().getPlayerID() != attacker.getPlayer().getPlayerID() && attacker.isTargetInRange(target, attacker.getSightRange())) {
                if (attacker.isTargetInRange(target, attacker.getActionRange()) && attacker.isLineOfSightOpen(target)) {
                    attacker.setTargetEntity(target);
                    return;
                }

                Point targetTile = getTargetTile(target);
                ArrayList<Point> path = getUnitPathToNearestAdjacentTile(attacker, targetTile.x, targetTile.y, play);
                if (path != null) {
                    attacker.setPath(path);
                    attacker.setTargetEntity(target);
//                    System.out.println("Pathing to Target. [UNIT - ID:" + attacker.getId()
//                            + " @ Tile: " + toTileX(attacker.getHitbox().x) + "," + toTileY(attacker.getHitbox().y) +
//                            " Coord: " + attacker.getHitbox().x + "," + attacker.getHitbox().y + "] [TARGET - ID:" + target.getId() +
//                            " @ Tile: " + toTileX(target.getX()) + "," + toTileY(target.getY()) +
//                            " Coord: " + target.getX() + "," + target.getY() + "]");

                    return;
                }
            }
        }
    }

    private Point getTargetTile(Unit target) {
        // Returns the tile the target is currently in, or the tile they are moving into if their path is not empty
        Point targetTile;
        ArrayList<Point> targetPath = target.getPath();
        if (targetPath != null && !targetPath.isEmpty())
            targetTile = targetPath.get(0);
        else
            targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));
        return targetTile;
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
            deadUnits.add(target);
            attacker.setState(IDLE);
            attacker.setTargetEntity(null);
            if (play.getSelectedEntity() == target)
                play.setSelectedEntity(null);
        }
    }

    public Unit getUnitAtCoord(int x, int y) {
        for (Unit u : units)
            if (u.isAlive() && u.getHitbox().contains(x, y))
                return u;
        return null;
    }

    public Unit getUnitAtTile(int tileX, int tileY) {
        Rectangle tileBounds = new Rectangle(toPixelX(tileX), toPixelY(tileY), TILE_SIZE, TILE_SIZE);
        for (Unit u : units)
            if (u.isAlive() && u.getHitbox().intersects(tileBounds))
                return u;
        return null;
    }

    public boolean isTileReserved(int tileX, int tileY, Unit excludedUnit) {
        Point p = new Point(tileX, tileY);
        for (Unit unit : units) {
            if (excludedUnit != null && excludedUnit.getId() == unit.getId())
                continue;

            ArrayList<Point> path = unit.getPath();
            if (unit.isAlive() && path != null && !path.isEmpty())
                if (path.get(0).equals(p))
                    return true;
        }
        return false;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public Play getPlay() {
        return play;
    }
}
