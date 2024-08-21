package objects;

import java.awt.Rectangle;
import java.io.Serializable;

public abstract class GameObject implements Serializable {

    public static final int BUILDING = 0;
    public static final int ENTITY = 1;
    public static final int RESOURCE = 2;

    protected Rectangle hitbox;
    protected int id;
    protected int type;

    public GameObject(int type, int id) {
        this.type = type;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getType() {
        return type;
    }
}
