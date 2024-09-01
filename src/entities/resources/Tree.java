package entities.resources;

import java.io.Serializable;

public class Tree extends ResourceObject implements Serializable {

    public Tree(int x, int y, int id, int spriteId) {
        super(x, y, id, TREE, spriteId);
    }
}
