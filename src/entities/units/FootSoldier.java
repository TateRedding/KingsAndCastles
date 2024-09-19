package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class FootSoldier extends CombatUnit {
    public FootSoldier(Player player, float x, float y, int id, UnitHandler unitHandler) {
        super(player, x, y, FOOT_SOLDIER, id, unitHandler);
    }
}
