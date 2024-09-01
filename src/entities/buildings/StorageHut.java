package entities.buildings;

import objects.Player;

public class StorageHut extends Building {
    public StorageHut(Player player, int id, int x, int y) {
        super(player, id, x, y, STORAGE_HUT);
    }
}
