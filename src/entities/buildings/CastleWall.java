package entities.buildings;

import objects.Player;

public class CastleWall extends Building {
    public CastleWall(Player player, int id, int x, int y) {
        super(player, id, x, y, CASTLE_WALL);
    }
}
