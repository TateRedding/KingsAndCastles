package resources;

import static utils.Constants.Resources.getHitboxHeight;
import static utils.Constants.Resources.getHitboxWidth;
import static utils.Constants.Resources.getStartingTotal;

import java.awt.Rectangle;

import objects.GameObject;

public abstract class Resource extends GameObject {
	
	protected int x, y, resourceType;
	protected int currentAmount, totalAmount;

	public Resource(int id, int resourceType, int x, int y) {
		super(id);
		this.resourceType = resourceType;
		this.x = x;
		this.y = y;
		this.totalAmount = getStartingTotal(resourceType);
		this.currentAmount = totalAmount;

		hitbox = new Rectangle((int) x, (int) y, getHitboxWidth(resourceType), getHitboxHeight(resourceType));
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getId() {
		return id;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getCurrentAmount() {
		return currentAmount;
	}

	public void setCurrentAmount(int currentAmount) {
		this.currentAmount = currentAmount;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

}
