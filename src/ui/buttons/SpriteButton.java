package ui.buttons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import utils.ImageLoader;

public class SpriteButton extends Button {

    private BufferedImage sprite;

    public SpriteButton(BufferedImage sprite, int x, int y) {
        super(x, y, getButtonWidth(SPRITE), getButtonHeight(SPRITE));
        this.sprite = sprite;
    }

    public void render(Graphics g) {
        int bgOffset = 6;
        int spriteSize = sprite.getWidth() * 2;
        int tileY = y;
        if (mousePressed)
            tileY += bgOffset;
        g.drawImage(ImageLoader.spriteButton[index], x, y, width, height, null);
        g.drawImage(sprite, x + bgOffset, tileY + bgOffset, spriteSize, spriteSize, null);
        if (disabled) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(x + bgOffset, tileY + bgOffset, spriteSize, spriteSize);
        }

    }

}
