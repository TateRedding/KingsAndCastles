package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class Knight extends CombatUnit {
    public Knight(Player player, float x, float y, int id, UnitHandler unitHandler) {
        super(player, x, y, KNIGHT, id, unitHandler);
    }
}
