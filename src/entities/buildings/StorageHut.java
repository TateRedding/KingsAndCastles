package entities.buildings;

import objects.Player;

public class StorageHut extends Building {

    public static final int SH_MAX_LOGS = 20;
    public static final int SH_MAX_STONE = 250;

    private int logs, stone;

    public StorageHut(Player player, int id, int x, int y) {
        super(player, id, x, y, STORAGE_HUT);
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
