package entities.buildings;

import objects.Player;

public class Village extends Building {
    public Village(Player player, int id, int x, int y) {
        super(player, id, x, y, VILLAGE);
    }
}
