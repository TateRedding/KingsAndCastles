package gamestates;

import main.Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;

public abstract class State implements StateMethods, Serializable {

    protected Game game;

    public State() {

    }

    public State(Game game) {
        this.game = game;
    }

    public void update() {

    }

    public void render(Graphics g) {

    }

    public void mousePressed(int x, int y, int button) {

    }

    public void mouseEntered(int x, int y) {

    }

    public void mouseExited(int x, int y) {

    }

    public void mouseDragged(int x, int y) {

    }

    public void mouseReleased(int x, int y, int button) {

    }

    public void mouseMoved(int x, int y) {

    }

    public void mouseWheelMoved(int dir, int amt) {

    }

    public void keyPressed(KeyEvent e) {

    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

}
