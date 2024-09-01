package entities.resources;

import java.io.Serializable;

public class CoalMine extends ResourceObject implements Serializable {

    public CoalMine(int tileX, int tileY, int id) {
        super(tileX, tileY, id, COAL, 0);
    }

}
