package objects;

import java.awt.Rectangle;
import java.io.Serializable;

public abstract class GameObject implements Serializable {

    // Categories
    public static final int BUILDING = 0;
    public static final int ENTITY = 1;
    public static final int RESOURCE = 2;

    // Resource Types
    public static final int GOLD = 0;
    public static final int TREE = 1;
    public static final int ROCK = 2;
    public static final int COAL = 3;
    public static final int IRON = 4;

    // Entity Types
    public static final int LABORER = 5;

    protected Rectangle hitbox;
    protected int playerNum, id, category, type;

    public GameObject(int id, int category, int type) {
        this.playerNum = 0;
        this.id = id;
        this.category = category;
        this.type = type;
    }

    public GameObject(int playerNum, int id, int category, int type) {
        this.playerNum = playerNum;
        this.id = id;
        this.category = category;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getCategory() {
        return category;
    }

    public int getId() {
        return id;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
