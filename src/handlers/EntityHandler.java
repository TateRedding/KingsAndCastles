package handlers;

import entities.Entity;
import entities.Laborer;
import gamestates.Play;
import main.Game;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class EntityHandler implements Serializable {

    private Play play;

    private ArrayList<Entity> entities = new ArrayList<>();

    public EntityHandler(Play play) {
        this.play = play;

        entities.add(new Laborer(play.getPlayers().get(0), 0, TOP_BAR_HEIGHT, 0));
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Entity e : entities)
            g.drawImage(Entity.getSprite(e.getEntityType()), e.getHitbox().x - (xOffset * Game.TILE_SIZE), e.getHitbox().y - (yOffset * Game.TILE_SIZE), null);
    }
}
