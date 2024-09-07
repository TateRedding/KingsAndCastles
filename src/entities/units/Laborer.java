package entities.units;

import entities.resources.ResourceObject;
import handlers.UnitHandler;
import objects.Player;

import static entities.resources.ResourceObject.*;

public class Laborer extends Unit {

    // Laborer Specific States
    public static final int CHOPPING = 3;
    public static final int MINING = 4;

    // Inventory Maximums
    public static final int MAX_COAL = 50;
    public static final int MAX_IRON = 50;
    public static final int MAX_LOGS = 250;
    public static final int MAX_STONE = 100;

    private int coal, iron, logs, stone;

    public Laborer(Player player, int x, int y, int id, UnitHandler unitHandler) {
        super(player, x, y, LABORER, id, unitHandler);
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

        if (targetEntity != null && targetEntity.getEntityType() == RESOURCE) {
            if (state == IDLE)
                setState((targetEntity.getSubType() == ResourceObject.TREE) ? CHOPPING : MINING);
        } else if (state == CHOPPING || state == MINING)
            setState(IDLE);
    }

    public int getCoal() {
        return coal;
    }

    public void setCoal(int coal) {
        this.coal = coal;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getLogs() {
        return logs;
    }

    public void setLogs(int logs) {
        this.logs = logs;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }
}
