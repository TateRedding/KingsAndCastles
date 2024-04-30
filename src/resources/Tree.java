package resources;

import java.io.Serializable;

public class Tree extends Resource implements Serializable {

    private int bitmaskId;

    public Tree(int x, int y, int id) {
        super(x, y, id, TREE);
        this.bitmaskId = 0;
    }

    public Tree(int x, int y, int id, int bitmaskId) {
        super(x, y, id, TREE);
        this.bitmaskId = bitmaskId;
    }

    public int getBitmaskId() {
        return bitmaskId;
    }

    public void setBitmaskId(int bitmaskId) {
        this.bitmaskId = bitmaskId;
    }

}
