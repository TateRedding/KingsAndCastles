package ui.overlays;

import ui.buttons.ImageButton;
import utils.ImageLoader;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static ui.buttons.Button.*;

public abstract class Overlay {

    public static final int OVERLAY_SMALL = 0;
    public static final int OVERLAY_LARGE = 1;

    protected int x, y;
    protected int size;
    protected Rectangle bounds;
    protected ImageButton exButton;

    public Overlay(int size, int x, int y) {
        this.size = size;
        this.x = x;
        this.y = y;
        int width = getOverlayWidth(size);
        this.bounds = new Rectangle(x, y, width, getOverlayHeight(size));
        int offset = 10;
        this.exButton = new ImageButton(ICON, x + width - getButtonWidth(ICON) - offset, y + offset, ImageLoader.icons[ICON_EX], 1.0f);
    }

    public static int getOverlayHeight(int size) {
        return switch (size) {
            case OVERLAY_SMALL -> 288;
            case OVERLAY_LARGE -> 576;
            default -> 0;
        };
    }

    public static int getOverlayWidth(int size) {
        return switch (size) {
            case OVERLAY_SMALL -> 480;
            case OVERLAY_LARGE -> 800;
            default -> 0;
        };
    }

    public static BufferedImage getBackgroundImage(int size) {
        return switch (size) {
            case OVERLAY_SMALL -> ImageLoader.overlayBgSmall;
            case OVERLAY_LARGE -> ImageLoader.overlayBgLarge;
            default -> null;
        };
    }

    public void update() {
        exButton.update();
    }

    public void render(Graphics g) {
        BufferedImage bg = getBackgroundImage(size);
        if (bg != null)
            g.drawImage(bg, x, y, null);
        exButton.render(g);
    }

    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (exButton.getBounds().contains(x, y))
                exButton.setMousePressed(true);
    }

    public void mouseMoved(int x, int y) {
        exButton.setMouseOver(false);
        if (exButton.getBounds().contains(x, y))
            exButton.setMouseOver(true);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public ImageButton getExButton() {
        return exButton;
    }

}
