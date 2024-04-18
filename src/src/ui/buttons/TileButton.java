package src.ui.buttons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import utils.ImageLoader;

public class TileButton extends Button {
	
	private BufferedImage tileImage;
	
	public TileButton(BufferedImage tileImage, int x, int y) {
		super(x, y, getButtonWidth(TILE), getButtonHeight(TILE));
		this.tileImage = tileImage;
	}
	
	public void render(Graphics g) {
		int bgOffset = 6;
		int spriteSize = tileImage.getWidth() * 2;
		int tileY = y;
		if (mousePressed)
			tileY += bgOffset;
		g.drawImage(ImageLoader.tileButton[index], x, y, width, height, null);
		g.drawImage(tileImage, x + bgOffset, tileY + bgOffset, spriteSize, spriteSize, null);
		if (disabled) {
			g.setColor(new Color(255, 255, 255, 100));
			g.fillRect(x + bgOffset, tileY + bgOffset, spriteSize, spriteSize);
		}
			
	}

}
