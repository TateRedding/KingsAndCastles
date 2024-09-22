package handlers;

import entities.buildings.*;
import entities.resources.GoldMine;
import entities.units.Laborer;
import entities.units.Unit;
import gamestates.Play;
import objects.Map;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import static entities.buildings.Building.*;
import static entities.buildings.CastleTurret.TURRET_ATTACK_RANGE_MODIFIER;
import static entities.buildings.Farm.FOOD_PER_FARMER;
import static entities.buildings.ThroneRoom.*;
import static entities.buildings.Village.POPULATION_PER_VILLAGE;
import static main.Game.*;
import static main.Game.toTileY;
import static pathfinding.AStar.isPointOpen;

public class BuildingHandler implements Serializable {

    private Play play;
    private ArrayList<Building> buildings = new ArrayList<>();

    private int id = 0;

    public BuildingHandler(Play play) {
        this.play = play;
        addThroneRooms();
    }


    public void update(boolean foodCycleThisUpdate) {
        for (Building b : buildings) {
            if (foodCycleThisUpdate)
                if ((b.getSubType() == FARM || b.getSubType() == FARM_ROTATED)) {
                    ArrayList<Laborer> farmers = ((Farm) b).getFarmers();
                    if (!farmers.isEmpty()) {
                        Player p = b.getPlayer();
                        p.setFood(p.getFood() + (FOOD_PER_FARMER * farmers.size()));
                    }
                }
            if (b instanceof CastleTurret turret && turret.getOccupyingUnit() != null)
                turret.findAndAttackTarget();
        }
    }

    public void render(Graphics g, int xOffset, int yOffset) {
        for (Building b : buildings) {
            g.drawImage(ImageLoader.buildings[b.getSubType()], b.getHitbox().x - xOffset, b.getHitbox().y - yOffset, null);

            if (b.getHealth() < b.getMaxHealth())
                b.drawHealthBar(g, b.getHealth(), b.getMaxHealth(), xOffset, yOffset);
        }
    }

    private void addThroneRooms() {
        for (int i = 0; i < play.getMap().getNumPlayers(); i++) {
            Point p = play.getMap().getThroneRoomPoints()[i];
            if (p != null) {
                Player currPlayer = play.getPlayers().get(i);
                buildings.add(new ThroneRoom(currPlayer, id++, toPixelX(p.x), toPixelY(p.y), this));
                currPlayer.setLogs(STARTING_LOGS);
                currPlayer.setStone(STARTING_STONE);
                currPlayer.setMaxPopulation(STARTING_POPULATION);
            }
        }
    }

    public void createBuilding(Player player, int x, int y, int buildingType) {
        switch (buildingType) {
            case CASTLE_WALL -> buildings.add(new CastleWall(player, id, x, y, this));
            case CASTLE_TURRET -> {
                Building wall = getBuildingAt(x, y);
                if (wall != null)
                    buildings.remove(wall);
                buildings.add(new CastleTurret(player, id, x, y, this));
            }
            case VILLAGE -> {
                buildings.add(new Village(player, id, x, y, this));
                player.setMaxPopulation(player.getMaxPopulation() + POPULATION_PER_VILLAGE);
            }
            case STORAGE_HUT -> buildings.add(new StorageHut(player, id, x, y, this));
            case REFINERY -> buildings.add(new Refinery(player, id, x, y, this));
            case FARM -> buildings.add(new Farm(player, id, x, y, false, this));
            case FARM_ROTATED -> buildings.add(new Farm(player, id, x, y, true, this));
            case BARRACKS_TIER_1 -> buildings.add(new Barracks(player, id, x, y, 1, this));
            case BARRACKS_TIER_2 -> buildings.add(new Barracks(player, id, x, y, 2, this));
            case BARRACKS_TIER_3 -> buildings.add(new Barracks(player, id, x, y, 3, this));
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
                if (bwi.getSubType() == REFINERY) {
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
                } else if (bwi.getSubType() == THRONE_ROOM || bwi.getSubType() == STORAGE_HUT) {
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

    public void killBuilding(Building b) {
        switch (b.getSubType()) {
            case FARM, FARM_ROTATED:
                for (Laborer l : ((Farm) b).getFarmers())
                    l.reactivate();
                break;
            case VILLAGE:
                Player p = b.getPlayer();
                p.setMaxPopulation(p.getMaxPopulation() - POPULATION_PER_VILLAGE);
                break;
            case STORAGE_HUT, REFINERY:
                ((BuildingWithInventory) b).emptyInventory();
                break;
        }
        b.setActive(false);
        buildings.remove(b);
    }

    public Building getBuildingAt(int x, int y) {
        for (Building b : buildings)
            if (b.getHitbox().contains(x, y))
                return b;
        return null;
    }

    public Point getSpawnTile(Building building) {
        Map map = play.getMap();
        int buildingType = building.getSubType();
        int tileWidth = getBuildingTileWidth(buildingType);
        int tileHeight = getBuildingTileHeight(buildingType);
        int tileXStart = toTileX(building.getX()) - 1;
        int tileYStart = toTileY(building.getY()) - 1;

        ArrayList<Point> spawnPoints = new ArrayList<>();
        int mapHeight = map.getTileData().length;
        int mapWidth = map.getTileData()[0].length;

        for (int y = tileYStart; y < tileYStart + tileHeight + 2; y++)
            for (int x = tileXStart; x < tileXStart + tileWidth + 2; x++)
                if (y >= 0 && y < mapHeight && x >= 0 && x < mapWidth)
                    if (!building.getHitbox().contains(toPixelX(x), toPixelY(y))) {
                        Point currPoint = new Point(x, y);
                        if (isPointOpen(currPoint, play))
                            spawnPoints.add(currPoint);
                    }

        if (spawnPoints.isEmpty())
            return null;

        Random r = new Random(play.getSeed());
        return spawnPoints.get(r.nextInt(spawnPoints.size()));
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public Play getPlay() {
        return play;
    }
}
