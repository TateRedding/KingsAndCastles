package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class Refinery extends BuildingWithInventory {

    public static final int R_MAX_IRON = 150;
    public static final int R_MAX_COAL = 100;

    public Refinery(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, REFINERY, buildingHandler);
    }

}