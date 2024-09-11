package handlers;

import entities.buildings.*;
import entities.resources.GoldMine;
import gamestates.Play;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.buildings.Building.*;
import static main.Game.toPixelX;
import static main.Game.toPixelY;

public class BuildingHandler implements Serializable {

    private Play play;
    private ArrayList<Building> buildings = new ArrayList<>();

    private int id = 0;

    public BuildingHandler(Play play) {
        this.play = play;
        addThroneRooms();
    }


    public void update() {

    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Building b : buildings)
            g.drawImage(ImageLoader.buildings[b.getSubType()], b.getHitbox().x - xOffset, b.getHitbox().y - yOffset, null);
    }

    private void addThroneRooms() {
        for (int i = 0; i < play.getMap().getNumPlayers(); i++) {
            Point p = play.getMap().getThroneRoomPoints()[i];
            if (p != null)
                buildings.add(new ThroneRoom(play.getPlayers().get(i), id++, toPixelX(p.x), toPixelY(p.y)));
        }
    }

    public void createBuilding(Player player, int x, int y, int buildingType) {
        switch (buildingType) {
            case CASTLE_WALL -> buildings.add(new CastleWall(player, id, x, y));
            case CASTLE_TURRET -> buildings.add(new CastleTurret(player, id, x, y));
            case VILLAGE -> buildings.add(new Village(player, id, x, y));
            case STORAGE_HUT -> buildings.add(new StorageHut(player, id, x, y));
            case REFINERY -> buildings.add(new Refinery(player, id, x, y));
            case FARM -> buildings.add(new Farm(player, id, x, y, false));
            case FARM_ROTATED -> buildings.add(new Farm(player, id, x, y, true));
            case BARRACKS_TIER_1 -> buildings.add(new Barracks(player, id, x, y, 1));
            case BARRACKS_TIER_2 -> buildings.add(new Barracks(player, id, x, y, 2));
            case BARRACKS_TIER_3 -> buildings.add(new Barracks(player, id, x, y, 3));
        }
        player.buildBuilding(buildingType);
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
