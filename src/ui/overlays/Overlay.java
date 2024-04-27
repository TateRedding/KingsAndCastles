package ui.overlays;

import static ui.buttons.Button.EX;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import ui.buttons.ExButton;
import utils.ImageLoader;

public abstract class Overlay {

    public static final int OVERLAY_HEIGHT = 288;
    public static final int OVERLAY_WIDTH = 480;

    protected int x, y;
    protected Rectangle bounds;
    protected ExButton exButton;

    public Overlay(int x, int y) {
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        int offset = 10;

        this.exButton = new ExButton(x + OVERLAY_WIDTH - getButtonWidth(EX) - offset, y + offset);
    }

    public void update() {
        exButton.update();
    }

    public void render(Graphics g) {
        g.drawImage(ImageLoader.overlayBg, x, y, null);
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

    public ExButton getExButton() {
        return exButton;
    }

}
