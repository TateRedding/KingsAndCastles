package entities.buildings;

import objects.Player;

public abstract class BuildingWithInventory extends Building {

    protected int coal, iron, logs, stone;
    protected boolean holdsLogsAndStone, holdsCoalAndIron;

    public BuildingWithInventory(Player player, int id, int x, int y, int buildingType) {
        super(player, id, x, y, buildingType);
        hasInventory = true;
        holdsCoalAndIron = buildingType == REFINERY;
        holdsLogsAndStone = buildingType == STORAGE_HUT || buildingType == THRONE_ROOM;
    }

    public void addCoal(int amount) {
        if (holdsCoalAndIron) {
            coal += amount;
            player.setCoal(player.getCoal() + amount);
        }
    }

    public void removeCoal(int amount) {
        if (holdsCoalAndIron)
            addCoal(amount * -1);
    }

    public void addIron(int amount) {
        if (holdsCoalAndIron) {
            iron += amount;
            player.setIron(player.getIron() + amount);
        }
    }

    public void removeIron(int amount) {
        if (holdsCoalAndIron)
            addIron(amount * -1);
    }

    public void addLogs(int amount) {
        if (holdsLogsAndStone) {
            logs += amount;
            player.setLogs(player.getLogs() + amount);
        }
    }

    public void removeLogs(int amount) {
        if (holdsLogsAndStone)
            addLogs(amount * -1);
    }

    public void addStone(int amount) {
        if (holdsLogsAndStone) {
            stone += amount;
            player.setStone(player.getStone() + amount);
        }
    }

    public void removeStone(int amount) {
        if (holdsLogsAndStone)
            addStone(amount * -1);
    }

    public int getCoal() {
        if (holdsCoalAndIron)
            return coal;
        else
            return 0;
    }

    public int getIron() {
        if (holdsCoalAndIron)
            return iron;
        else
            return 0;
    }

    public int getLogs() {
        if (holdsLogsAndStone)
            return logs;
        else
            return 0;
    }

    public int getStone() {
        if (holdsLogsAndStone)
            return stone;
        else
            return 0;
    }

    public boolean holdsLogsAndStone() {
        return holdsLogsAndStone;
    }

    public boolean holdsCoalAndIron() {
        return holdsCoalAndIron;
    }
}
