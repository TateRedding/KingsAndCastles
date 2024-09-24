package entities;

import entities.buildings.Building;
import entities.units.Unit;
import gamestates.Play;
import objects.Player;
import pathfinding.AStar;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.units.Unit.IDLE;
import static main.Game.*;

public abstract class Entity implements Serializable {

    // Entity Types
    public static final int BUILDING = 0;
    public static final int PROJECTILE = 1;
    public static final int RESOURCE = 1;
    public static final int UNIT = 2;

    public static final int HEALTH_BAR_MAX_WIDTH = TILE_SIZE / 4 * 3;

    protected Player player;
    protected Rectangle hitbox;
    protected float x, y;
    protected int id;
    protected int entityType, subType;

    protected boolean active = true;

    public Entity(Player player, int entityType, int subType, float x, float y, int id) {
        this.player = player;
        this.entityType = entityType;
        this.subType = subType;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public void drawHealthBar(Graphics g, int current, int max, int xOffset, int yOffset) {
        int xStart = (hitbox.x + (hitbox.width - HEALTH_BAR_MAX_WIDTH) / 2) - xOffset;
        int yStart = (hitbox.y + 3) - yOffset;

        int fillWidth = (int) (((float) current / (float) max) * HEALTH_BAR_MAX_WIDTH);

        g.setColor(new Color(64, 27, 0));
        g.drawRect(xStart - 3, yStart - 3, HEALTH_BAR_MAX_WIDTH + 6, 8);
        g.drawRect(xStart - 1, yStart - 1, HEALTH_BAR_MAX_WIDTH + 2, 4);

        g.setColor(new Color(255, 201, 128));
        g.drawRect(xStart - 2, yStart - 2, HEALTH_BAR_MAX_WIDTH + 4, 6);

        g.setColor(new Color(136, 33, 42));
        g.drawRect(xStart, yStart, fillWidth, 1);

        g.setColor(new Color(189, 79, 79));
        g.drawRect(xStart, yStart + 1, fillWidth, 1);

    }

    public boolean isTargetInRange(Entity target, int tileRange) {
        float startX = x - tileRange * TILE_SIZE;
        float startY = y - tileRange * TILE_SIZE;
        float size = (tileRange * 2 + 1) * TILE_SIZE;
        Ellipse2D range = new Ellipse2D.Double(startX, startY, size, size);
        Rectangle targetBounds = target.getHitbox();

        int numTilesX = targetBounds.width / TILE_SIZE;
        int numTilesY = targetBounds.height / TILE_SIZE;
        int halfTileSize = TILE_SIZE / 2;

        for (int tilesX = 0; tilesX < numTilesX; tilesX++)
            for (int tilesY = 0; tilesY < numTilesY; tilesY++) {
                int middleX = targetBounds.x + (tilesX * TILE_SIZE) + halfTileSize;
                int middleY = targetBounds.y + (tilesY * TILE_SIZE) + halfTileSize;
                if (range.contains(middleX, middleY))
                    return true;
            }
        return false;
    }

    public void updateHitbox() {
        hitbox.x = (int) x;
        hitbox.y = (int) y;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSubType() {
        return subType;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getEntityType() {
        return entityType;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }
}
