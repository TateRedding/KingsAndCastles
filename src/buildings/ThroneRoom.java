package buildings;

import objects.Player;

public class ThroneRoom extends Building {
    public ThroneRoom(Player player, int id, int x, int y) {
        super(player, id, x, y, THRONE_ROOM);
    }
}
