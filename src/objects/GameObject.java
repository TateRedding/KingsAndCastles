package objects;

import java.awt.Rectangle;
import java.io.Serializable;

public abstract class GameObject implements Serializable {

    protected Rectangle hitbox;
    protected int id;

    public GameObject(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
