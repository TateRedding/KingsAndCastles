package gamestates;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import main.Game;
import objects.Map;
import ui.DropDownMenu;
import ui.buttons.TextButton;
import utils.ImageLoader;

import static ui.buttons.Button.TEXT_SMALL;

public class LoadGame extends State implements StateMethods {

    private TextButton menu;
    private DropDownMenu gameList;

    public LoadGame(Game game) {
        super(game);
        int offset = 48;
        menu = new TextButton(TEXT_SMALL, "Menu", 28f, offset, offset);
        initDropDownMenu();
    }

    public void initDropDownMenu() {
        ArrayList<Play> games = game.getSaveFileHandler().getGames();
        String[] options = new String[games.size()];
        for (int i = 0; i < games.size(); i++)
            options[i] = games.get(i).getName();
        int ddX = (Game.SCREEN_WIDTH - DropDownMenu.DD_WIDTH) / 2;
        int yOffset = 48;
        gameList = new DropDownMenu("Select Game", options, 5, ddX, yOffset);
    }

    @Override
    public void update() {
        menu.update();
        gameList.update();
    }

    @Override
    public void render(Graphics g) {
        menu.render(g);
        gameList.render(g);
    }

    private void reset() {
        initDropDownMenu();
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        gameList.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y))
                menu.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        gameList.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y) && menu.isMousePressed()) {
                GameStates.setGameState(GameStates.MENU);
                reset();
            }
        menu.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        gameList.mouseMoved(x, y);
        menu.setMouseOver(false);
        if (menu.getBounds().contains(x, y))
            menu.setMouseOver(true);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        gameList.mouseWheelMoved(e);
    }

}
