package ui;

import static ui.buttons.Button.EX;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import ui.buttons.ExButton;
import utils.ImageLoader;

public abstract class Overlay {

	protected int x, y, width, height;
	protected Rectangle bounds;
	protected ExButton xButton;

	public Overlay(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = ImageLoader.overlayBg.getWidth();
		this.height = ImageLoader.overlayBg.getHeight();
		this.bounds = new Rectangle(x, y, width, height);
		int offset = 10;
		
		this.xButton = new ExButton(x + width - getButtonWidth(EX) -  offset, y + offset);
	}

	public void update() {
		xButton.update();
	}

	public void render(Graphics g) {
		g.drawImage(ImageLoader.overlayBg, x, y, null);
		xButton.render(g);
	}

	public void mousePressed(int x, int y, int button) {
		if (button == MouseEvent.BUTTON1)
			if (xButton.getBounds().contains(x, y))
				xButton.setMousePressed(true);
	}

	public void mouseMoved(int x, int y) {
		xButton.setMouseOver(false);
		if (xButton.getBounds().contains(x, y))
			xButton.setMouseOver(true);
	}

	public Rectangle getBounds() {
		return bounds;
	}
	
	public ExButton getXButton() {
		return xButton;
	}

}
