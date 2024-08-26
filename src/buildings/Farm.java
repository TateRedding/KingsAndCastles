package buildings;

import objects.Player;

public class Farm extends Building {
    public Farm(Player player, int id, int x, int y, boolean rotated) {
        super(player, id, x, y, rotated ? FARM_ROTATED : FARM);
    }
}
