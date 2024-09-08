package entities.buildings;

import objects.Player;

public class Refinery extends Building {

    public static final int R_MAX_IRON = 20;
    public static final int R_MAX_COAL = 100;

    private int iron, coal;

    public Refinery(Player player, int id, int x, int y) {
        super(player, id, x, y, REFINERY);
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getCoal() {
        return coal;
    }

    public void setCoal(int coal) {
        this.coal = coal;
    }
}
