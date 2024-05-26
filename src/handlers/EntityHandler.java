package handlers;

import entities.Entity;
import entities.Laborer;
import gamestates.Play;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class EntityHandler implements Serializable {

    private Play play;
    private ArrayList<Entity> entities = new ArrayList<>();

    public EntityHandler(Play play) {
        this.play = play;
    }

    public void update() {

        for (Entity e : entities)
            e.update();
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities) {
            g.drawImage(Entity.getSprite(e.getEntityType()), e.getHitbox().x - (xOffset * TILE_SIZE), e.getHitbox().y - (yOffset * TILE_SIZE), null);
            // Debugging
            // drawPath(e, g, xOffset, yOffset);
        }
    }

    private void drawPath(Entity e, Graphics g, int xOffset, int yOffset) {
        if (e.getPath() != null && !e.getPath().isEmpty()) {
            g.setColor(new Color(255, 0, 0, 100));
            for (Point p : e.getPath()) {
                g.fillRect((p.x - xOffset) * TILE_SIZE, (p.y - yOffset) * TILE_SIZE + TOP_BAR_HEIGHT, TILE_SIZE, TILE_SIZE);
            }
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
