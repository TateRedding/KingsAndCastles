package gamestates;

import main.Game;
import ui.buttons.TextButton;
import utils.LoadSave;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import static ui.buttons.Button.TEXT_LARGE;
import static ui.buttons.Button.getButtonHeight;

public class Debug extends State {

    private TextButton menu, clearMaps, clearGames;
    private ArrayList<TextButton> buttons = new ArrayList<TextButton>();

    public Debug(Game game) {
        super(game);
        initButtons();
    }

    private void initButtons() {
        float fontSize = 46f;
        int yOffset = 16;
        int buttonHeight = getButtonHeight(TEXT_LARGE);
        int x = 32;
        int y = 32;

        menu = new TextButton(TEXT_LARGE, x, y, fontSize, "Menu");
        clearMaps = new TextButton(TEXT_LARGE, x, y += buttonHeight + yOffset, fontSize, "Clear Maps");
        clearGames = new TextButton(TEXT_LARGE, x, y += buttonHeight + yOffset, fontSize, "Clear Games");

        buttons.addAll(Arrays.asList(menu, clearMaps, clearGames));
    }

    @Override
    public void update() {
        for (TextButton tb : buttons)
            tb.update();
    }

    @Override
    public void render(Graphics g) {
        for (TextButton tb : buttons)
            tb.render(g);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            for (TextButton tb : buttons)
                if (tb.getBounds().contains(x, y))
                    tb.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y) && menu.isMousePressed())
                GameStates.setGameState(GameStates.MENU);
            else if (clearMaps.getBounds().contains(x, y) && clearMaps.isMousePressed()) {
                LoadSave.clearMaps();
                game.getSaveFileHandler().loadMaps();
            } else if (clearGames.getBounds().contains(x, y) && clearGames.isMousePressed()) {
                LoadSave.clearGames();
                game.getSaveFileHandler().loadGames();
            }


        for (TextButton tb : buttons)
            tb.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        for (TextButton tb : buttons)
            tb.setMouseOver(false);

        for (TextButton tb : buttons)
            if (tb.getBounds().contains(x, y))
                tb.setMouseOver(true);
    }
}
