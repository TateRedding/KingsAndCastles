package entities.buildings;

import objects.Player;

public class CastleTurret extends Building {
    public CastleTurret(Player player, int id, int x, int y) {
        super(player, id, x, y, CASTLE_TURRET);
    }
}
