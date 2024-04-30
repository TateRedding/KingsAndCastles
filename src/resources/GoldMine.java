package resources;

import java.io.Serializable;

public class GoldMine extends Resource implements Serializable {

    public GoldMine(int x, int y, int id) {
        super(x, y, id, GOLD_MINE);
    }

}
