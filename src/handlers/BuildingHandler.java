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
import static entities.buildings.ThroneRoom.*;
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
            if (p != null) {
                Player currPlayer = play.getPlayers().get(i);
                buildings.add(new ThroneRoom(currPlayer, id++, toPixelX(p.x), toPixelY(p.y)));
                currPlayer.setLogs(STARTING_LOGS);
                currPlayer.setStone(STARTING_STONE);
                currPlayer.setMaxPopulation(STARTING_POPULATION);
            }
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
        adjustResources(buildingType);
    }

    private void adjustResources(int buildingType) {
        int costCoal = getCostCoal(buildingType);
        int costIron = getCostIron(buildingType);
        int costLogs = getCostLogs(buildingType);
        int costStone = getCostStone(buildingType);

        for (Building b : buildings) {
            if (costCoal <= 0 && costIron <= 0 && costLogs <= 0 && costStone <= 0)
                return;
            if (b.hasInventory()) {
                BuildingWithInventory bwi = (BuildingWithInventory) b;
                if (bwi.holdsCoalAndIron()) {
                    if (costCoal > 0 && bwi.getCoal() > 0) {
                        int coalAmt = Math.min(costCoal, bwi.getCoal());
                        bwi.removeCoal(coalAmt);
                        costCoal -= coalAmt;
                    }
                    if (costIron > 0 && bwi.getIron() > 0) {
                        int ironAmt = Math.min(costIron, bwi.getIron());
                        bwi.removeIron(ironAmt);
                        costIron -= ironAmt;
                    }
                }
                if (bwi.holdsLogsAndStone()) {
                    if (costLogs > 0 && bwi.getLogs() > 0) {
                        int logsAmt = Math.min(costLogs, bwi.getLogs());
                        bwi.removeLogs(logsAmt);
                        costLogs -= logsAmt;
                    }
                    if (costStone > 0 && bwi.getStone() > 0) {
                        int stoneAmt = Math.min(costStone, bwi.getStone());
                        bwi.removeStone(stoneAmt);
                        costStone -= stoneAmt;
                    }
                }
            }
        }
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
