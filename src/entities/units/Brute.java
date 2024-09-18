package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class Brute extends CombatUnit {
    public Brute(Player player, int x, int y, int id, UnitHandler unitHandler) {
        super(player, x, y, BRUTE, id, unitHandler);
    }
}
