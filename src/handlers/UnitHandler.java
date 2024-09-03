package handlers;

import entities.resources.ResourceObject;
import entities.units.Brute;
import entities.units.Laborer;
import entities.units.Unit;
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
import static pathfinding.AStar.getPathToNearestAdjacentTile;

public class UnitHandler implements Serializable {

    private static final int NUM_MAX_STARTING_UNITS = 3;

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

                // Below concepts to be addressed and reworked at a later point

                // Check if target has moved
//                if (unitType != LABORER && u.getState() == WALKING && u.getTargetEntity() != null)
//                    changePathIfTargetMoved(u);

                // Auto-attack
//                if (unitType != LABORER && u.getState() == IDLE && u.getTargetEntity() == null)
//                    findEnemyToAttack(u);

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

    private void changePathIfTargetMoved(Unit u) {
        ArrayList<Point> currPath = u.getPath();
        if (currPath == null || currPath.isEmpty())
            return;

        int targetTileX = toTileX(u.getTargetEntity().getX());
        int targetTileY = toTileY(u.getTargetEntity().getY());
        Point pathGoal = u.getPath().get(u.getPath().size() - 1);

        if (Math.abs(targetTileX - pathGoal.x) > 1 || Math.abs(targetTileY - pathGoal.y) > 1) {
//            System.out.println("Target with ID: " + u.getTargetEntity().getId() + " has moved. Re-calculating path for unit with ID: " + u.getId() + ".");
            u.setPath(getPathToNearestAdjacentTile(u, targetTileX, targetTileY, play));
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
                ArrayList<Point> path = getPathToNearestAdjacentTile(attacker, targetTile.x, targetTile.y, play);
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
            if (play.getSelectedEntity() == target)
                play.setSelectedEntity(null);
        }
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
