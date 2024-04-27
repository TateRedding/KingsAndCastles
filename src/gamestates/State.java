package gamestates;

import java.awt.Graphics;
import java.io.Serializable;

import main.Game;

public abstract class State implements StateMethods, Serializable {

    protected Game game;

    public State() {

    }

    public State(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {

    }

    @Override
    public void mousePressed(int x, int y, int button) {

    }

    @Override
    public void mouseReleased(int x, int y, int button) {

    }

    @Override
    public void mouseMoved(int x, int y) {

    }

}
