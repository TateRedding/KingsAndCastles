package ui.buttons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageButton extends Button {

    private BufferedImage image;
    private float imageScale;

    public ImageButton(int buttonType, int x, int y, BufferedImage displayImage, float displayImageScale) {
        super(buttonType, x, y);
        this.image = displayImage;
        this.imageScale = displayImageScale;
    }

    public void render(Graphics g) {
        super.render(g);
        int imageHeight = (int) (image.getHeight() * imageScale);
        int imageWidth = (int) (image.getWidth() * imageScale);
        int offset = getButtonOffset(buttonType);
        int yStart = y + (height - imageHeight - offset) / 2;
        int xStart = x + (width - imageWidth) / 2;

        if (mousePressed)
            yStart += offset;

        g.drawImage(image, xStart, yStart, imageWidth, imageHeight, null);
        if (disabled) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(xStart, yStart, imageWidth, imageHeight);
        }

    }

}
