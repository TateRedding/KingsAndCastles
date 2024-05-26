package resources;

import java.io.Serializable;

public class GoldMine extends ResourceObject implements Serializable {

    public GoldMine(int tileX, int tileY, int id) {
        super(tileX, tileY, id, GOLD, 0);
    }

}
