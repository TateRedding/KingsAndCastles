package objects;

import java.io.Serializable;

public class Tile implements Serializable {

	public static final int GRASS = 0;
	public static final int DIRT = 1;
	public static final int SAND = 2;
	public static final int WATER_GRASS = 3;
	public static final int WATER_SAND = 4;

	private int bitmaskId, tileType;

	public Tile(int tileType, int bitmaskId) {
		this.tileType = tileType;
		this.bitmaskId = bitmaskId;
	}

	public int getBitmaskId() {
		return bitmaskId;
	}

	public void setBitmaskId(int bitmaskId) {
		this.bitmaskId = bitmaskId;
	}

	public int getTileType() {
		return tileType;
	}

}
