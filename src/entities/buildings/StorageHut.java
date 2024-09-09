package entities.buildings;

import objects.Player;

public class StorageHut extends Building {

    public static final int SH_MAX_LOGS = 400;
    public static final int SH_MAX_STONE = 250;

    private int logs, stone;

    public StorageHut(Player player, int id, int x, int y) {
        super(player, id, x, y, STORAGE_HUT);
    }

    public void addLogs(int amount) {
        logs += amount;
        player.setLogs(player.getLogs() + amount);
    }

    public void removeLogs(int amount) {
        addLogs(amount * -1);
    }

    public void addStone(int amount) {
        stone += amount;
        player.setStone(player.getStone() + amount);
    }

    public void removeStone(int amount) {
        addStone(amount * -1);
    }

    public int getLogs() {
        return logs;
    }

    public int getStone() {
        return stone;
    }
}
