package entities;

import handlers.EntityHandler;
import objects.Player;
import resources.ResourceObject;

public class Laborer extends Entity {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    private boolean gathering;

    public Laborer(Player player, int x, int y, int id, EntityHandler entityHandler) {
        super(player, x, y, LABORER, id, entityHandler);
    }

    private int getNumberofFrames(int state) {
        return switch (state) {
            case IDLE, WALKING -> 4;
            case CHOPPING, MINING -> 5;
            default -> 1;
        };
    }

    private int getMaxAnimationTicks(int state) {
        return switch (state) {
            case IDLE -> 25;
            case WALKING, CHOPPING, MINING -> 15;
            default -> 20;
        };
    }

    public void update() {
        super.update();
        animationTick++;
        if (animationTick >= getMaxAnimationTicks(state)) {
            animationTick = 0;
            animationFrame++;
            if (animationFrame >= getNumberofFrames(state))
                animationFrame = 0;
        }

        if (resourceToGather != null) {
            if (state == IDLE) {
                state = (resourceToGather.getResourceType() == ResourceObject.TREE) ? CHOPPING : MINING;
                animationFrame = 0;
            }
        } else if (state == CHOPPING || state == MINING)
            state = IDLE;
    }
}
