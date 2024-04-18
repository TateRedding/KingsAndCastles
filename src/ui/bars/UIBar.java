package ui.bars;

import static main.Game.SCREEN_WIDTH;

import java.awt.Rectangle;

public abstract class UIBar {

	public final static int X = 0;
	public final static int UI_WIDTH = SCREEN_WIDTH;

	protected Rectangle bounds;

	public UIBar() {
		
	}

	public Rectangle getBounds() {
		return bounds;
	}
	
}
