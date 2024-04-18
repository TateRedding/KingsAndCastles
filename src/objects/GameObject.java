package objects;

import java.awt.Rectangle;

public abstract class GameObject {
	
	protected Rectangle hitbox;
	protected int id;

	public GameObject(int id) {
		this.id = id;
	}

	public Rectangle getHitbox() {
		return hitbox;
	}

	public int getId() {
		return id;
	}

}
