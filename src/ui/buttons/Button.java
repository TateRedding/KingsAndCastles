package ui.buttons;

import utils.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Button {
    // Button types
    public static final int DROP_DOWN = 0;
    public static final int ICON = 1;
    public static final int SPRITE = 2;
    public static final int TEXT_LARGE = 3;
    public static final int TEXT_SMALL_LONG = 4;
    public static final int TEXT_SMALL_SHORT = 5;

    // Icons
    public static final int ICON_EX = 0;
    public static final int ICON_CIRCLE = 1;
    public static final int ICON_SQUARE = 2;
    public static final int ICON_UP = 3;
    public static final int ICON_DOWN = 4;
    public static final int ICON_UNIT = 5;
    public static final int ICON_BUILDING = 6;
    public static final int ICON_RESOURCE = 7;
    public static final int ICON_TERRAIN = 8;
    public static final int ICON_MAP = 9;

    // Drop Down
    public static final int DD_DOWN = 0;
    public static final int DD_UP = 1;

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
        buttonImages = getButtonImages(buttonType);
        this.width = getButtonWidth(buttonType);
        this.height = getButtonHeight(buttonType);
        bounds = new Rectangle(x, y, width, height);
    }

    private static BufferedImage[] getButtonImages(int buttonType) {
        return switch (buttonType) {
            case ICON -> ImageLoader.iconButton;
            case SPRITE -> ImageLoader.spriteButton;
            case TEXT_LARGE -> ImageLoader.largeTextButton;
            case TEXT_SMALL_LONG -> ImageLoader.smallTextButtonLong;
            case TEXT_SMALL_SHORT -> ImageLoader.smallTextButtonShort;
            default -> null;
        };
    }

    public static int getButtonWidth(int buttonType) {
        return switch (buttonType) {
            case DROP_DOWN -> 48;
            case ICON -> 24;
            case SPRITE -> 76;
            case TEXT_LARGE -> 168;
            case TEXT_SMALL_LONG -> 112;
            case TEXT_SMALL_SHORT -> 56;
            default -> 0;
        };
    }

    public static int getButtonHeight(int buttonType) {
        return switch (buttonType) {
            case DROP_DOWN -> 48;
            case ICON, TEXT_SMALL_LONG, TEXT_SMALL_SHORT -> 27;
            case SPRITE -> 83;
            case TEXT_LARGE -> 81;
            default -> 0;
        };
    }

    protected int getButtonOffset(int buttonType) {
        return switch (buttonType) {
            case ICON, TEXT_SMALL_LONG, TEXT_SMALL_SHORT -> 3;
            case SPRITE, TEXT_LARGE -> 7;
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
        if (buttonImages != null && buttonImages[index] != null)
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
}
