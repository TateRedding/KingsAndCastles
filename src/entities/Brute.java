package entities;

import handlers.EntityHandler;
import objects.Player;

public class Brute extends Entity {

    // Brute Specific States
    public static final int ATTACKING = 3;

    public Brute(Player player, int x, int y, int id, EntityHandler entityHandler) {
        super(player, x, y, BRUTE, id, entityHandler);
    }

    private int getNumberOfFrames(int state) {
        return switch (state) {
            case IDLE, WALKING, ATTACKING -> 4;
            default -> 1;
        };
    }

    private int getMaxAnimationTicks(int state) {
        return switch (state) {
            case IDLE -> 25;
            case WALKING, ATTACKING -> 15;
            default -> 20;
        };
    }

    public void update() {
        super.update();
        animationTick++;
        if (animationTick >= getMaxAnimationTicks(state)) {
            animationTick = 0;
            animationFrame++;
            if (animationFrame >= getNumberOfFrames(state))
                animationFrame = 0;
        }

        if (entityToAttack != null) {
            if (state == IDLE)
                setState(ATTACKING);
        } else if (state == ATTACKING)
            setState(IDLE);
    }
}
