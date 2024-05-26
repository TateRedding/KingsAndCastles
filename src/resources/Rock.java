package resources;

import java.io.Serializable;

public class Rock extends ResourceObject implements Serializable {

    public Rock(int tileX, int tileY, int id, int spriteId) {
        super(tileX, tileY, id, ROCK, spriteId);
    }
}
