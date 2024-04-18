package ui.buttons;

import java.awt.Graphics;

import utils.ImageLoader;

public class ExButton extends Button {
	public ExButton(int x, int y) {
		super(x, y, getButtonWidth(EX), getButtonHeight(EX));
		this.x = x;
		this.y = y;
	}
	
	public void render(Graphics g) {
		g.drawImage(ImageLoader.xButton[index], x, y, width, height, null);
	}

}
