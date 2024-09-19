package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class Crossbowman extends CombatUnit {

    public Crossbowman(Player player, float x, float y, int id, UnitHandler unitHandler) {
        super(player, x, y, CROSSBOWMAN, id, unitHandler);
    }
}
