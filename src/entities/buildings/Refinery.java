package entities.buildings;

import objects.Player;

public class Refinery extends Building {

    public static final int R_MAX_IRON = 150;
    public static final int R_MAX_COAL = 100;

    private int iron, coal;

    public Refinery(Player player, int id, int x, int y) {
        super(player, id, x, y, REFINERY);
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
}
