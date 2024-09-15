package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class CastleWall extends Building {
    public CastleWall(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, CASTLE_WALL, buildingHandler);
    }
}
