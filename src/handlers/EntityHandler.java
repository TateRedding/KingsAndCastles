package handlers;

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

import static entities.Entity.LABORER;
import static main.Game.TILE_SIZE;
import static objects.Tile.WATER_GRASS;
import static objects.Tile.WATER_SAND;
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
            e.update();

            if (e.getActionTick() >= e.getActionTickMax()) {
                if (e.getEntityType() == LABORER) {
                    play.getResourceObjectHandler().gatherResource(e.getPlayer(), e.getResourceToGather(), e);
                } else {
                    attack(e, e.getEntityToAttack());
                }
                e.setActionTick(0);
            }
        }
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities) {
            g.drawImage(Entity.getSprite(e.getEntityType(), e.getState(), e.getDirection(), e.getAnimationFrame()), e.getHitbox().x - (xOffset * TILE_SIZE), e.getHitbox().y - (yOffset * TILE_SIZE), null);
            // Debugging
            drawPath(e, g, xOffset, yOffset);
//            drawHitbox(e, g, xOffset, yOffset);
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
            for (int j = 0; j < maxStartingEntities; j++) {
                Point spawn = spawnPoints.get(j);
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

    private void attack(Entity attacker, Entity target) {
        target.setHealth(target.getHealth() - attacker.getDamage());
        if (target.getHealth() <= 0) {
            target.setAlive(false);
            for (Entity e : entities)
                if (e.getId() == target.getId()) {
                    entities.remove(e);
                    return;
                }
        }
    }

    public void moveTo(Entity e, int tileX, int tileY) {
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

    public void moveToNearestTile(Entity e, int tileX, int tileY) {
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
                int pixelX = x * TILE_SIZE;
                int pixelY = y * TILE_SIZE + TOP_BAR_HEIGHT;
                int tileType = play.getMap().getTileData()[y][x].getTileType();
                if (play.getGameObjectAt(pixelX, pixelY) == null && tileType != WATER_SAND && tileType != WATER_GRASS) {
                    Point target = new Point(x, y);
                    double xDist = start.getX() - target.getX();
                    double yDist = start.getY() - target.getY();
                    double cSquared = (xDist * xDist) + (yDist * yDist);

                    boolean isCardinal = (x == tileX || y == tileY);
                    double distance = Math.sqrt(cSquared);
                    if (!isCardinal)
                        distance *= 2;

                    openTiles.put(distance, target);
                }
            }

        if (openTiles.isEmpty())
            return;

        ArrayList<Double> sorted = new ArrayList<>(openTiles.keySet());
        Collections.sort(sorted);

        for (int i = 0; i < openTiles.size(); i++) {
            Point target = openTiles.get(sorted.get(i));
            ArrayList<Point> path = AStar.pathFind(start, target, play);
            if (path != null) {
                if (e.getPath() != null && !e.getPath().isEmpty())
                    path.add(0, e.getPath().get(0));
                e.setPath(path);
                return;
            }
        }
    }

    public Entity getEntityAtCoord(int x, int y, boolean checkEntireTile) {
        if (checkEntireTile) {
            Rectangle tileBounds = new Rectangle(x / TILE_SIZE * TILE_SIZE, y / TILE_SIZE * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            for (Entity e : entities)
                if (e.getHitbox().intersects(tileBounds))
                    return e;
        } else
            for (Entity e : entities)
                if (e.getHitbox().contains(x, y))
                    return e;
        return null;
    }


    public boolean isEntityInTile(int tileX, int tileY) {
        for (Entity e : entities) {
            Rectangle tileBounds = new Rectangle(tileX * TILE_SIZE, tileY * TILE_SIZE + TOP_BAR_HEIGHT, TILE_SIZE, TILE_SIZE);
            if (e.getHitbox().intersects(tileBounds))
                return true;
        }
        return false;
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
