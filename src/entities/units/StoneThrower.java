package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class StoneThrower extends CombatUnit {
    public StoneThrower(Player player, float x, float y, int id, UnitHandler unitHandler) {
        super(player, x, y, STONE_THROWER, id, unitHandler);
    }
}
