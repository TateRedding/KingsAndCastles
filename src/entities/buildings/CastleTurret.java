package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class CastleTurret extends Building {
    public CastleTurret(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, CASTLE_TURRET, buildingHandler);
    }
}
