package ui.buttons;

import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button {

    public static final int TEXT_SMALL = 0;
    public static final int TEXT_LARGE = 1;
    public static final int SPRITE = 2;
    public static final int EX = 3;
    public static final int DD_DOWN = 4;
    public static final int DD_UP = 5;

    protected BufferedImage[] buttonImages;
    protected Rectangle bounds;

    protected int buttonType;
    protected int index;
    protected int x, y, width, height;

    protected boolean mouseOver, mousePressed, disabled;

    public Button(int buttonType, int x, int y) {
        this.x = x;
        this.y = y;
        this.buttonType = buttonType;
        initImagesAndBounds();
    }

    private void initImagesAndBounds() {
        buttonImages = getButtonImages(buttonType);
        if (buttonImages != null) {
            this.width = buttonImages[0].getWidth();
            this.height = buttonImages[0].getHeight();
            bounds = new Rectangle(x, y, width, height);
        }
    }

    private static BufferedImage[] getButtonImages(int buttonType) {
        return switch (buttonType) {
            case TEXT_SMALL -> ImageLoader.smallTextButton;
            case TEXT_LARGE -> ImageLoader.largeTextButton;
            case SPRITE -> ImageLoader.spriteButton;
            case EX -> ImageLoader.exButton;
            case DD_DOWN -> ImageLoader.ddDownButton;
            case DD_UP -> ImageLoader.ddUpButton;
            default -> null;
        };
    }

    public static int getButtonWidth(int buttonType) {
        return switch (buttonType) {
            case TEXT_SMALL -> 56;
            case TEXT_LARGE -> 168;
            case SPRITE -> 76;
            case EX -> 24;
            case DD_DOWN, DD_UP -> 48;
            default -> 0;
        };
    }

    public static int getButtonHeight(int buttonType) {
        return switch (buttonType) {
            case TEXT_SMALL, EX -> 27;
            case TEXT_LARGE -> 81;
            case SPRITE -> 83;
            case DD_DOWN, DD_UP -> 48;
            default -> 0;
        };
    }

    protected int getButtonOffset(int buttonType) {
        return switch (buttonType) {
            case TEXT_SMALL, EX -> 3;
            case TEXT_LARGE, SPRITE -> 7;
            default -> 0;
        };
    }

    public void update() {
        if (disabled)
            index = 3;
        else {
            index = 0;
            if (mouseOver)
                index = 1;
            if (mousePressed)
                index = 2;
        }
    }

    public void render(Graphics g) {
        g.drawImage(buttonImages[index], x, y, null);
    }

    public void reset(int x, int y) {
        mousePressed = false;
        if (!bounds.contains(x, y))
            mouseOver = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setMouseOver(boolean mouseOver) {
        if (!mouseOver || !disabled)
            this.mouseOver = mouseOver;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public void setMousePressed(boolean mousePressed) {
        if (!mousePressed || !disabled)
            this.mousePressed = mousePressed;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setButtonType(int buttonType) {
        this.buttonType = buttonType;
        initImagesAndBounds();
    }
}
