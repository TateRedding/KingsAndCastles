package gamestates;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.*;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import ui.buttons.TextButton;

public class Menu extends State {

    private TextButton newGame, loadGame, editMap, quit;
    private ArrayList<TextButton> buttons = new ArrayList<TextButton>();

    private boolean ctrlHeld;

    public Menu(Game game) {
        super(game);
        initButtons();
    }

    private void initButtons() {
        float fontSize = 46f;
        int numButtons = 3;
        int yOffset = 30;
        int buttonHeight = getButtonHeight(TEXT_LARGE);
        int x = (SCREEN_WIDTH - getButtonWidth(TEXT_LARGE)) / 2;
        int y = (SCREEN_HEIGHT - (buttonHeight * numButtons + yOffset * (numButtons - 1))) / 2;

        newGame = new TextButton(TEXT_LARGE, x, y, fontSize, "New Game");
        loadGame = new TextButton(TEXT_LARGE, x, y += buttonHeight + yOffset, fontSize, "Load Game");
        editMap = new TextButton(TEXT_LARGE, x, y += buttonHeight + yOffset, fontSize, "Edit Map");

        int offset = 48;
        quit = new TextButton(TEXT_SMALL_SHORT, SCREEN_WIDTH - getButtonWidth(TEXT_SMALL_SHORT) - offset, offset, 28f, "Quit");
        buttons.addAll(Arrays.asList(newGame, loadGame, editMap, quit));
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
            if (newGame.getBounds().contains(x, y) && newGame.isMousePressed()) {
                game.setPlayMapSelect(new PlayMapSelect(game));
                GameStates.setGameState(GameStates.PLAY_MAP_SELECT);
            } else if (loadGame.getBounds().contains(x, y) && loadGame.isMousePressed()) {
                game.setLoadGame(new LoadGame(game));
                GameStates.setGameState(GameStates.LOAD_GAME);
            } else if (editMap.getBounds().contains(x, y) && editMap.isMousePressed()) {
                game.setEditMapSelect(new EditMapSelect(game));
                GameStates.setGameState(GameStates.EDIT_MAP_SELECT);
            } else if (quit.getBounds().contains(x, y) && quit.isMousePressed()) {
                System.exit(0);
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

    public void keyPressed(KeyEvent e) {
        if ((e.isControlDown() || e.isMetaDown()) && e.getKeyCode() == KeyEvent.VK_D)
            GameStates.setGameState(GameStates.DEBUG);
    }

}
