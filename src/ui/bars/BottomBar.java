package ui.bars;

import static main.Game.GAME_AREA_HEIGHT;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;
import static ui.buttons.Button.TEXT_SMALL;
import static objects.Tile.GRASS;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import gamestates.GameStates;
import main.Game;
import ui.buttons.TextButton;
import utils.ImageLoader;

public abstract class BottomBar extends UIBar {

    public final static int BOTTOM_BAR_Y = TOP_BAR_HEIGHT + GAME_AREA_HEIGHT;
    public final static int BOTTOM_BAR_HEIGHT = 160;

    protected TextButton menu, save;

    public BottomBar() {
        bounds = new Rectangle(X, BOTTOM_BAR_Y, UI_WIDTH, BOTTOM_BAR_HEIGHT);
        int offset = 16;
        float fontSize = 28f;
        menu = new TextButton(TEXT_SMALL, X + offset, BOTTOM_BAR_Y + offset, fontSize, "Menu");
        save = new TextButton(TEXT_SMALL, menu.getBounds().x, menu.getBounds().y + menu.getBounds().height + offset / 2, fontSize, "Save");

    }

    public void update() {
        menu.update();
        save.update();
    }

    public void render(Graphics g) {
        g.drawImage(ImageLoader.tiles.get(GRASS).get(0), X, BOTTOM_BAR_Y, Game.SCREEN_WIDTH, BOTTOM_BAR_HEIGHT,
                null);
        g.drawImage(ImageLoader.bottomBar, X, BOTTOM_BAR_Y, null);
        menu.render(g);
        save.render(g);
    }

    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y))
                menu.setMousePressed(true);
            else if (save.getBounds().contains(x, y))
                save.setMousePressed(true);
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y) && menu.isMousePressed())
                GameStates.setGameState(GameStates.MENU);
        menu.reset(x, y);
    }

    public void mouseMoved(int x, int y) {
        menu.setMouseOver(false);
        save.setMouseOver(false);
        if (menu.getBounds().contains(x, y))
            menu.setMouseOver(true);
        else if (save.getBounds().contains(x, y))
            save.setMouseOver(true);
    }

    public TextButton getSave() {
        return save;
    }

}
