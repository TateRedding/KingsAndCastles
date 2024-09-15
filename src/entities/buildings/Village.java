package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class Village extends Building {
    public Village(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, VILLAGE, buildingHandler);
    }
}
