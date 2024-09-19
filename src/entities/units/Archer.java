package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class Archer extends CombatUnit {
    public Archer(Player player, float x, float y, int id, UnitHandler unitHandler) {
        super(player, x, y, ARCHER, id, unitHandler);
    }
}
