package entities;

import objects.Player;
import resources.ResourceObject;

public class Laborer extends Entity {

    // Laborer Specific Animations
    public static final int CHOP = 2;
    public static final int MINE = 3;

    public Laborer(Player player, int x, int y, int id) {
        super(player, x, y, LABORER, id);
    }

    private int getNumberofFrames(int animation) {
        return switch (animation) {
            case IDLE, WALK -> 4;
            case CHOP, MINE -> 5;
            default -> 1;
        };
    }

    private int getMaxAnimationTicks(int animation) {
        return switch (animation) {
            case IDLE -> 25;
            case WALK, CHOP, MINE -> 15;
            default -> 20;
        };
    }

    public void update() {
        super.update();
        animationTick++;
        if (animationTick >= getMaxAnimationTicks(animation)) {
            animationTick = 0;
            animationFrame++;
            if (animationFrame >= getNumberofFrames(animation))
                animationFrame = 0;
        }

        if (resourceToGather != null) {
            if (animation == IDLE) {
                animation = (resourceToGather.getResourceType() == ResourceObject.TREE) ? CHOP : MINE;
                animationFrame = 0;
            }
        } else if (animation == CHOP || animation == MINE)
            animation = IDLE;
    }
}
