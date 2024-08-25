package ui.bars;

import ui.buttons.Button;

import static main.Game.SCREEN_WIDTH;

import java.awt.Rectangle;

public abstract class UIBar {

    // Resource Icons
    public static final int ICON_POP = 10;
    public static final int ICON_GOLD = 11;
    public static final int ICON_FOOD = 12;
    public static final int ICON_LOG = 13;
    public static final int ICON_STONE = 14;
    public static final int ICON_IRON = 15;
    public static final int ICON_COAL = Button.ICON_RESOURCE;

    public final static int X = 0;
    public final static int UI_WIDTH = SCREEN_WIDTH;

    protected Rectangle bounds;

    public UIBar() {

    }

    public Rectangle getBounds() {
        return bounds;
    }

}
