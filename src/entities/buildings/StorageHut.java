package entities.buildings;

import objects.Player;

public class StorageHut extends BuildingWithInventory {

    public static final int SH_MAX_LOGS = 400;
    public static final int SH_MAX_STONE = 250;

    public StorageHut(Player player, int id, int x, int y) {
        super(player, id, x, y, STORAGE_HUT);
    }

}
