package entities.buildings;

import entities.units.Laborer;
import handlers.BuildingHandler;
import objects.Player;

import java.awt.*;
import java.util.ArrayList;

import static main.Game.*;

public class Farm extends Building {

    public static final int MAX_FARMERS = 2;
    public static final int FOOD_PER_FARMER = 5;

    private ArrayList<Laborer> farmers = new ArrayList<>();

    public Farm(Player player, int id, int x, int y, boolean rotated, BuildingHandler buildingHandler) {
        super(player, id, x, y, rotated ? FARM_ROTATED : FARM, buildingHandler);
    }

    public void releaseFarmer() {
        Point spawn = ((BuildingHandler) combatEntityHandler).getSpawnTile(this);
        if (spawn != null) {
            int lastIdx = farmers.size() - 1;
            Laborer l = farmers.get(lastIdx);
            farmers.remove(lastIdx);
            l.reactivate(toPixelX(spawn.x), toPixelY(spawn.y));
        }
    }

    public ArrayList<Laborer> getFarmers() {
        return farmers;
    }
}
