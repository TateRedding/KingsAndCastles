package ui.buttons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageButton extends Button {

    private BufferedImage displayImage;
    private float imageScale;

    public ImageButton(int buttonType, int x, int y, BufferedImage displayImage, float displayImageScale) {
        super(buttonType, x, y);
        this.displayImage = displayImage;
        this.imageScale = displayImageScale;
    }

    public void render(Graphics g) {
        super.render(g);
        int imageHeight = (int) (displayImage.getHeight() * imageScale);
        int imageWidth = (int) (displayImage.getWidth() * imageScale);
        int offset = getButtonOffset(buttonType);
        int yStart = y + (height - imageHeight - offset) / 2;
        int xStart = x + (width - imageWidth) / 2;

        if (mousePressed)
            yStart += offset;

        g.drawImage(displayImage, xStart, yStart, imageWidth, imageHeight, null);
        if (disabled) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(xStart, yStart, imageWidth, imageHeight);
        }

    }

    public void setDisplayImage(BufferedImage displayImage) {
        this.displayImage = displayImage;
    }
}
