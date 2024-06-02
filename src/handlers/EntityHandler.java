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
import java.util.Random;

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
        for (Entity e : entities)
            e.update();
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities) {
            g.drawImage(Entity.getSprite(e.getType()), e.getHitbox().x - (xOffset * TILE_SIZE), e.getHitbox().y - (yOffset * TILE_SIZE), null);
            // Debugging
            drawPath(e, g, xOffset, yOffset);
            drawHitbox(e, g, xOffset, yOffset);
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
                entities.add(new Laborer(players.get(i), spawn.x * TILE_SIZE, spawn.y * TILE_SIZE + TOP_BAR_HEIGHT, id++));
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

    public Entity getEntityAt(int x, int y) {
        for (Entity e : entities)
            if (e.getHitbox().contains(x, y))
                return e;
        return null;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public GameObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(GameObject targetObject) {
        this.targetObject = targetObject;
    }
}
