package handlers;

import buildings.Building;
import entities.Entity;
import gamestates.Play;
import utils.ImageLoader;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static main.Game.TILE_SIZE;

public class BuildingHandler implements Serializable {

    private Play play;
    private ArrayList<Building> buildings = new ArrayList<>();

    public BuildingHandler(Play play) {
        this.play = play;
    }

    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Building b : buildings)
            g.drawImage(ImageLoader.buildings[b.getBuildingType()], b.getHitbox().x - (xOffset * TILE_SIZE), b.getHitbox().y - (yOffset * TILE_SIZE), null);
    }

    public Building getBuildingAt(int x, int y) {
        for (Building b : buildings)
            if (b.getHitbox().contains(x, y))
                return b;
        return null;
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }
}