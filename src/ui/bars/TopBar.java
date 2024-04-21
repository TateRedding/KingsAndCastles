package ui.bars;

import static objects.Tile.GRASS;

import java.awt.Graphics;
import java.awt.Rectangle;

import main.Game;
import utils.ImageLoader;

public abstract class TopBar extends UIBar {

    public final static int TOP_BAR_Y = 0;
    public final static int TOP_BAR_HEIGHT = 128;

    public TopBar() {
        bounds = new Rectangle(X, TOP_BAR_Y, UI_WIDTH, TOP_BAR_HEIGHT);
    }

    public void render(Graphics g) {
        g.drawImage(ImageLoader.tiles.get(GRASS).get(0), X, TOP_BAR_Y, Game.SCREEN_WIDTH, TOP_BAR_HEIGHT, null);
        g.drawImage(ImageLoader.topBar, X, TOP_BAR_Y, null);
    }

}
