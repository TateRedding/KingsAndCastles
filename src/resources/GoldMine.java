package resources;

import java.io.Serializable;

public class GoldMine extends Resource implements Serializable {

    public GoldMine(int tileX, int tileY, int id) {
        super(tileX, tileY, id, GOLD_MINE);
    }

}
