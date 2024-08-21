package objects;

public abstract class SelectableGameObject extends GameObject {

    protected Player player;

    protected int health, maxHealth;

    public SelectableGameObject(Player player, int type, int id) {
        super(type, id);
        this.player = player;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public Player getPlayer() {
        return player;
    }
}
