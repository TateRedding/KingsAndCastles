package entities.units;

import handlers.UnitHandler;
import objects.Player;

public class CombatUnit extends Unit {
    // Combat Unit Specific States
    public static final int ATTACKING = 3;

    public CombatUnit(Player player, float x, float y, int unitType, int id, UnitHandler unitHandler) {
        super(player, x, y, unitType, id, unitHandler);
    }

    public static int getNumberOfFrames(int state) {
        return switch (state) {
            case IDLE, WALKING, ATTACKING -> 4;
            default -> 1;
        };
    }

    public static int getMaxAnimationTick(int state) {
        return switch (state) {
            case IDLE -> 25;
            case WALKING, ATTACKING -> 15;
            default -> 20;
        };
    }

    public static int getActionFrameIndex(int state) {
        return switch (state) {
            case ATTACKING -> 3;
            default -> getNumberOfFrames(state);
        };
    }

    public void update() {
        super.update();
        if (targetEntity != null && (targetEntity.getEntityType() == UNIT || targetEntity.getEntityType() == BUILDING)) {
            if (state == IDLE)
                setState(ATTACKING);
        } else if (state == ATTACKING)
            setState(IDLE);
    }
}
