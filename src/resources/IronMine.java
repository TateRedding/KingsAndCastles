package resources;

import java.io.Serializable;

public class IronMine extends ResourceObjects implements Serializable {

    public IronMine(int tileX, int tileY, int id) {
        super(tileX, tileY, id, IRON_MINE);
    }

}
