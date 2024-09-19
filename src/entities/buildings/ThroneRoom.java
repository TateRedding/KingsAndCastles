package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class ThroneRoom extends BuildingWithInventory {

    public static final int STARTING_LOGS = 500;
    public static final int STARTING_POPULATION = 2;
    public static final int STARTING_STONE = 550;

    public ThroneRoom(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, THRONE_ROOM, buildingHandler);
        logs = STARTING_LOGS;
        stone = STARTING_STONE;
    }
}