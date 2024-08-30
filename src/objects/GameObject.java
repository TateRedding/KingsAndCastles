package objects;

import java.awt.*;
import java.io.Serializable;

import static main.Game.TILE_SIZE;
import static resources.ResourceObject.AMOUNT_BAR_MAX_WIDTH;

public abstract class GameObject implements Serializable {

    public static final int BUILDING = 0;
    public static final int ENTITY = 1;
    public static final int RESOURCE = 2;

    protected Rectangle hitbox;
    protected int id;
    protected int gameObjectType;

    public GameObject(int type, int id) {
        this.gameObjectType = type;
        this.id = id;
    }

    public void drawHealthBar(Graphics g, int current, int max, int xOffset, int yOffset) {
        int xStart = (hitbox.x + (hitbox.width - AMOUNT_BAR_MAX_WIDTH) / 2) - xOffset * TILE_SIZE;
        int yStart = (hitbox.y + 3) - yOffset * TILE_SIZE;

        int fillWidth = (int) (((float) current / (float) max) * AMOUNT_BAR_MAX_WIDTH);

        g.setColor(new Color(64, 27, 0));
        g.drawRect(xStart - 3, yStart - 3, AMOUNT_BAR_MAX_WIDTH + 6, 8);
        g.drawRect(xStart - 1, yStart - 1, AMOUNT_BAR_MAX_WIDTH + 2, 4);

        g.setColor(new Color(255, 201, 128));
        g.drawRect(xStart - 2, yStart - 2, AMOUNT_BAR_MAX_WIDTH + 4, 6);

        g.setColor(new Color(136, 33, 42));
        g.drawRect(xStart, yStart, fillWidth, 1);

        g.setColor(new Color(189, 79, 79));
        g.drawRect(xStart, yStart + 1, fillWidth, 1);

    }

    public int getId() {
        return id;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getGameObjectType() {
        return gameObjectType;
    }
}
