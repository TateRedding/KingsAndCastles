package entities.buildings;

import objects.Player;

public class ThroneRoom extends BuildingWithInventory {

    public static final int STARTING_LOGS = 50;
    public static final int STARTING_POPULATION = 2;
    public static final int STARTING_STONE = 55;

    public ThroneRoom(Player player, int id, int x, int y) {
        super(player, id, x, y, THRONE_ROOM);
        logs = STARTING_LOGS;
        stone = STARTING_STONE;
    }
}