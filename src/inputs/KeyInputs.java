package inputs;

import gamestates.State;
import main.Game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
        State currState = game.getCurrentGameState();
        currState.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}
