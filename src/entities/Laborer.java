package entities;

import handlers.EntityHandler;
import objects.Player;
import resources.ResourceObject;

public class Laborer extends Entity {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    public Laborer(Player player, int x, int y, int id, EntityHandler entityHandler) {
        super(player, x, y, LABORER, id, entityHandler);
    }

    public static int getNumberOfFrames(int state) {
        return switch (state) {
            case IDLE, WALKING -> 4;
            case CHOPPING, MINING -> 5;
            default -> 1;
        };
    }

    public static int getMaxAnimationTick(int state) {
        return switch (state) {
            case IDLE -> 25;
            case WALKING, CHOPPING, MINING -> 15;
            default -> 20;
        };
    }

    public static int getActionFrameIndex(int state) {
        return switch (state) {
            case CHOPPING, MINING -> 3;
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

        if (resourceToGather != null) {
            if (state == IDLE)
                setState((resourceToGather.getResourceType() == ResourceObject.TREE) ? CHOPPING : MINING);
        } else if (state == CHOPPING || state == MINING)
            setState(IDLE);
    }
}
