package inputs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import gamestates.GameStates;
import gamestates.State;
import main.Game;

public class MouseInputs implements MouseListener, MouseMotionListener, MouseWheelListener {

    private Game game;

    public MouseInputs(Game game) {
        this.game = game;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mousePressed(e.getX(), e.getY(), e.getButton());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseReleased(e.getX(), e.getY(), e.getButton());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseEntered(e.getX(), e.getY());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseExited(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseDragged(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseMoved(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        State currState = game.getCurrentGameState();
        currState.mouseWheelMoved(e.getWheelRotation(), e.getScrollAmount());
    }

}
