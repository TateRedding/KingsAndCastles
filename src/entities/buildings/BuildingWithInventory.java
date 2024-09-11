package entities.buildings;

import objects.Player;

public abstract class BuildingWithInventory extends Building {

    protected int coal, iron, logs, stone;

    public BuildingWithInventory(Player player, int id, int x, int y, int buildingType) {
        super(player, id, x, y, buildingType);
        hasInventory = true;
    }

    public void addCoal(int amount) {
        coal += amount;
        player.setCoal(player.getCoal() + amount);
    }

    public void removeCoal(int amount) {
        addCoal(amount * -1);
    }

    public void addIron(int amount) {
        iron += amount;
        player.setIron(player.getIron() + amount);
    }

    public void removeIron(int amount) {
        addIron(amount * -1);
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

    public int getCoal() {
        return coal;
    }

    public int getIron() {
        return iron;

    }

    public int getLogs() {
        return logs;
    }

    public int getStone() {
        return stone;
    }
}
