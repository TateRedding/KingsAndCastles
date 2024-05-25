package entities;

import objects.Player;

public class Laborer extends Entity {
    public Laborer(Player player, int x, int y, int id) {
        super(player, x, y, LABORER, id);
    }
}
