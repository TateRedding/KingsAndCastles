package handlers;

import entities.buildings.Building;
import entities.resources.ResourceObject;
import entities.units.*;
import gamestates.Debug;
import gamestates.Play;
import entities.Entity;
import objects.Player;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

import static entities.units.Brute.ATTACKING;
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

    public void update(boolean foodCycleThisUpdate) {
        for (Unit u : units) {
            if (!u.isActive()) continue;

            if (foodCycleThisUpdate) {
                if (u.getPlayer().getFood() > 0)
                    u.eat();
                else
                    u.starve();

                if (!u.isActive()) continue;
            }

            u.update();
            int unitType = u.getSubType();
            Entity target = u.getTargetEntity();

            // Check if the target is now inactive
            if (unitType != LABORER && target != null && target.getEntityType() == UNIT) {
                if (!target.isActive()) {
                    u.setTargetEntity(null);
                    target = null;
                    resetPathToFirstTile(u);
                }
            }

            // Check if target has moved out of range
            if (unitType != LABORER && (u.getState() == WALKING || u.getState() == ATTACKING) && target != null) {
                adjustPathIfTargetMovedOutOfActionRange(u);
            }

            // Auto-attack if idle and no target
            if (unitType != LABORER && u.getState() == IDLE && target == null) {
                findEnemyToAttack(u);
            }

            if (u.getActionTick() >= u.getActionTickMax()) {
                performUnitAction(u, target);
                u.setActionTick(0);
            }
        }

        if (foodCycleThisUpdate)
            units.sort(Comparator.comparingInt(Unit::getCyclesSinceLastFed));
    }

    private void resetPathToFirstTile(Unit u) {
        if (u.getPath() != null && !u.getPath().isEmpty())
            u.setPath(new ArrayList<>(Arrays.asList(u.getPath().get(0))));
        else
            u.setPath(null);
    }

    private void performUnitAction(Unit u, Entity target) {
        if (u.getSubType() == LABORER && target instanceof ResourceObject)
            play.getResourceObjectHandler().gatherResource(u.getPlayer(), (ResourceObject) target, (Laborer) u);
        else if (target instanceof Unit || target instanceof Building)
            attack(u, target);
    }


    public void render(Graphics g, int xOffset, int yOffset) {
        for (Unit u : units) {
            if (u.isActive()) {
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
        if (!deadUnits.isEmpty())
            cleanupUnitList();
    }

    private void cleanupUnitList() {
        units.removeIf(unit -> deadUnits.contains(unit));
        deadUnits.clear();
    }

    private void createStartingUnits() {
        ArrayList<ArrayList<Point>> castleZones = play.getMap().getCastleZones();
        ArrayList<Player> players = play.getPlayers();
        Random random = new Random(play.getSeed());

        for (int i = 0; i < players.size(); i++) {
            int maxStartingUnits = Math.min(NUM_MAX_STARTING_UNITS, castleZones.get(i).size());
            ArrayList<Point> spawnPoints = new ArrayList<>(castleZones.get(i));
            Point throneRoomPoint = play.getMap().getThroneRoomPoints()[i];
            if (throneRoomPoint != null)
                spawnPoints.remove(throneRoomPoint);
            Collections.shuffle(spawnPoints, random);
            for (int j = 0; j < maxStartingUnits; j++) {
                Point spawn = spawnPoints.get(j);

                //Debugging
                switch (j) {
                    case 1:
                        createUnit(players.get(i), spawn, BRUTE);
                        break;
                    case 2:
                        createUnit(players.get(i), spawn, STONE_THROWER);
                        break;
                    default:
                        createUnit(players.get(i), spawn, LABORER);
                        break;
                }
            }
        }
    }

    public void createUnit(Player player, Point spawn, int unitType) {
        switch (unitType) {
            case ARCHER -> units.add(new Archer(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case BRUTE -> units.add(new Brute(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case CROSSBOWMAN -> units.add(new Crossbowman(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case FOOT_SOLDIER -> units.add(new FootSoldier(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case KNIGHT -> units.add(new Knight(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case LABORER -> units.add(new Laborer(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
            case STONE_THROWER -> units.add(new StoneThrower(player, toPixelX(spawn.x), toPixelY(spawn.y), id++, this));
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

    private void adjustPathIfTargetMovedOutOfActionRange(Unit u) {
        ArrayList<Point> currPath = u.getPath();
        Entity target = u.getTargetEntity();
        if (target.getEntityType() != UNIT)
            return;

        Point targetTile = getTargetTile((Unit) target);
        Point unitTile;

        if (currPath != null && !currPath.isEmpty())
            unitTile = u.getPath().get(u.getPath().size() - 1);
        else
            unitTile = new Point(toTileX(u.getHitbox().x), toTileY(u.getHitbox().y));

        if (Math.abs(targetTile.x - unitTile.x) > u.getActionRange() || Math.abs(targetTile.y - unitTile.y) > u.getActionRange()) {
            ArrayList<Point> newPath = getUnitPathToNearestAdjacentTile(u, targetTile.x, targetTile.y, play);

            if (newPath == null && currPath != null && !currPath.isEmpty()) {
                newPath = new ArrayList<>();
                newPath.add(currPath.get(0));
            }
            u.setPath(newPath);
        }
    }

    private void findEnemyToAttack(Unit attacker) {
        ArrayList<Entity> targets = new ArrayList<>();
        targets.addAll(units);
        targets.addAll(play.getBuildingHandler().getBuildings());
        for (Entity target : targets) {
            if (target.isActive() && target.getPlayer().getPlayerID() != attacker.getPlayer().getPlayerID() && attacker.isTargetInRange(target, attacker.getSightRange())) {
                if (attacker.isTargetInRange(target, attacker.getActionRange()) && attacker.isLineOfSightOpen(target)) {
                    attacker.setTargetEntity(target);
                    return;
                }

                Point targetTile = getTargetTile(target);
                ArrayList<Point> path = getUnitPathToNearestAdjacentTile(attacker, targetTile.x, targetTile.y, play);
                if (path != null) {
                    attacker.setPath(path);
                    attacker.setTargetEntity(target);
                    return;
                }
            }
        }
    }

    private Point getTargetTile(Entity target) {
        // Returns the tile the target is currently in, or the tile they are moving into if their path is not empty
        Point targetTile = null;
        if (target instanceof Unit) {
            ArrayList<Point> targetPath = ((Unit) target).getPath();
            if (targetPath != null && !targetPath.isEmpty())
                targetTile = targetPath.get(0);
            else
                targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));
        } else if (target instanceof Building)
            targetTile = new Point(toTileX(target.getHitbox().x), toTileY(target.getHitbox().y));
        return targetTile;
    }

    private void attack(Unit attacker, Entity target) {
        int attackStyle = getAttackStyle(attacker.getSubType());
        if (attackStyle == MELEE) {
            if (attacker.getSubType() == -1 || target.getSubType() == -1)
                return;
            target.hurt(attacker.getDamage());

        } else if (attackStyle == RANGED)
            play.getProjectileHandler().newProjectile(attacker, target);

        // Auto-retaliate
        if (target instanceof Unit) {
            if (target.getSubType() != LABORER && ((Unit) target).getTargetEntity() == null && ((Unit) target).getState() == IDLE)
                ((Unit) target).setTargetEntity(attacker);
        }

        if (target.getHealth() <= 0) {
            if (target instanceof Unit)
                killUnit((Unit) target);
            else if (target instanceof Building)
                play.getBuildingHandler().killBuilding((Building) target);
            attacker.setState(IDLE);
            attacker.setTargetEntity(null);
            if (play.getSelectedEntity() == target)
                play.setSelectedEntity(null);
        }
    }

    private void killUnit(Unit u) {
        u.setActive(false);
        deadUnits.add(u);
    }

    public Unit getUnitAtCoord(int x, int y) {
        for (Unit u : units)
            if (u.isActive() && u.getHitbox().contains(x, y))
                return u;
        return null;
    }

    public Unit getUnitAtTile(int tileX, int tileY) {
        Rectangle tileBounds = new Rectangle(toPixelX(tileX), toPixelY(tileY), TILE_SIZE, TILE_SIZE);
        for (Unit u : units)
            if (u.isActive() && u.getHitbox().intersects(tileBounds))
                return u;
        return null;
    }

    public boolean isTileReserved(int tileX, int tileY, Unit excludedUnit) {
        Point p = new Point(tileX, tileY);
        for (Unit unit : units) {
            if (excludedUnit != null && excludedUnit.getId() == unit.getId())
                continue;

            ArrayList<Point> path = unit.getPath();
            if (unit.isActive() && path != null && !path.isEmpty())
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
