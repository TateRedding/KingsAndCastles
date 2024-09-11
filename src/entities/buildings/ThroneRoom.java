package entities.buildings;

import objects.Player;

public class ThroneRoom extends Building {

    public static final int STARTING_LOGS = 50;
    public static final int STARTING_POPULATION = 2;
    public static final int STARTING_STONE = 55;

    private int logs, stone;

    public ThroneRoom(Player player, int id, int x, int y) {
        super(player, id, x, y, THRONE_ROOM);
        this.logs = STARTING_LOGS;
        this.stone = STARTING_STONE;
    }

    public void removeLogs(int amount) {
        this.logs -= amount;
        player.setLogs(player.getLogs() - amount);
    }

    public void removeStone(int amount) {
        this.stone -= amount;
        player.setStone(player.getStone() - amount);
    }

    public int getLogs() {
        return logs;
    }

    public int getStone() {
        return stone;
    }
}
