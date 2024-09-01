package units;

import handlers.UnitHandler;
import objects.Player;

public class Brute extends Unit {

    // Brute Specific States
    public static final int ATTACKING = 3;

    public Brute(Player player, int x, int y, int id, UnitHandler unitHandler) {
        super(player, x, y, BRUTE, id, unitHandler);
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
        animationTick++;
        if (animationTick >= getMaxAnimationTick(state)) {
            animationTick = 0;
            animationFrame++;
            if (animationFrame >= getNumberOfFrames(state))
                animationFrame = 0;
        }

        if (targetEntity != null && targetEntity.getEntityType() == UNIT) {
            if (state == IDLE)
                setState(ATTACKING);
        } else if (state == ATTACKING)
            setState(IDLE);
    }
}