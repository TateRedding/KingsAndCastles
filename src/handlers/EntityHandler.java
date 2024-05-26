package handlers;

import entities.Entity;
import entities.Laborer;
import gamestates.Play;
import pathfinding.AStar;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class EntityHandler implements Serializable {

    private Play play;
    private ArrayList<Entity> entities = new ArrayList<>();

    // Testing
    private boolean testSetupCompleted = false;

    public EntityHandler(Play play) {
        this.play = play;
    }

    public void update() {

        for (Entity e : entities)
            e.update();

        // Testing
        if (!testSetupCompleted) {
            Laborer testLaborer = new Laborer(play.getPlayers().get(0), 0, TOP_BAR_HEIGHT, 0);
            /*
            int tlX = testLaborer.getHitbox().x / TILE_SIZE * TILE_SIZE;
            int tlY = (testLaborer.getHitbox().y - TOP_BAR_HEIGHT) / TILE_SIZE * TILE_SIZE;
            testLaborer.setPath(AStar.pathFind(new Point(tlX, tlY), new Point(9, 9), this.play));
            */
            entities.add(testLaborer);
            testSetupCompleted = true;
        }
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities) {
            g.drawImage(Entity.getSprite(e.getEntityType()), e.getHitbox().x - (xOffset * TILE_SIZE), e.getHitbox().y - (yOffset * TILE_SIZE), null);
            // Testing
            /*
            if (e.getPath() != null && !e.getPath().isEmpty()) {
                g.setColor(new Color(255, 0, 0, 100));
                for (Point p : e.getPath()) {
                    g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE + TOP_BAR_HEIGHT, TILE_SIZE, TILE_SIZE);
                }
            }
            */
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
}
