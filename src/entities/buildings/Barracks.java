package entities.buildings;

import handlers.BuildingHandler;
import objects.Player;

public class Barracks extends Building {

    public Barracks(Player player, int id, int x, int y, int tier, BuildingHandler buildingHandler) {
        super(player, id, x, y, getSubTypeByTier(tier), buildingHandler);
    }

    private static int getSubTypeByTier(int tier) {
        return switch (tier) {
            case 1 -> BARRACKS_TIER_1;
            case 2 -> BARRACKS_TIER_2;
            case 3 -> BARRACKS_TIER_3;
            default -> throw new IllegalArgumentException("Invalid tier: " + tier);
        };
    }
}
