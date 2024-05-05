package resources;

import java.io.Serializable;

public class CoalMine extends ResourceObjects implements Serializable {

    public CoalMine(int tileX, int tileY, int id) {
        super(tileX, tileY, id, COAL_MINE);
    }

}
