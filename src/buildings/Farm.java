package buildings;

import objects.Player;

public class Farm extends Building {
    public Farm(Player player, int id, int x, int y) {
        super(player, id, x, y, FARM);
    }
}
