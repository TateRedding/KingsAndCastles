package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import gamestates.GameStates;
import main.Game;

public class KeyInputs implements KeyListener {

    private Game game;

    public KeyInputs(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (GameStates.gameState) {
            case EDIT:
                game.getEdit().keyPressed(e);
                break;
            case EDIT_MAP_SELECT:
                game.getEditMapSelect().keyPressed(e);
                break;
            case PLAY:
                game.getPlay().keyPressed(e);
                break;
            case PLAY_MAP_SELECT:
                game.getPlayMapSelect().keyPressed(e);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}
