package resources;

import java.io.Serializable;

public class Rock extends ResourceObject implements Serializable {

    private int spriteId;

    public Rock(int tileX, int tileY, int id, int spriteId) {
        super(tileX, tileY, id, ROCK);
        this.spriteId = spriteId;
    }

    public int getSpriteId() {
        return spriteId;
    }

    public void setSpriteId(int spriteId) {
        this.spriteId = spriteId;
    }
}
