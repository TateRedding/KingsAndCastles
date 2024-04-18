package src.inputs;

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
		switch (GameStates.gameState) {
		case EDIT:
			game.getEdit().mouseEntered(e.getX(), e.getY());
			break;
		case PLAY:
			game.getPlay().mouseEntered(e.getX(), e.getY());
			break;
		default:
			break;
		}

	}

	@Override
	public void mouseExited(MouseEvent e) {
		switch (GameStates.gameState) {
		case EDIT:
			game.getEdit().mouseExited(e.getX(), e.getY());
			break;
		case PLAY:
			game.getPlay().mouseExited(e.getX(), e.getY());
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		switch (GameStates.gameState) {
		case EDIT:
			game.getEdit().mouseDragged(e.getX(), e.getY());
			break;
		case PLAY:
			game.getPlay().mouseDragged(e.getX(), e.getY());
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		State currState = game.getCurrentGameState();
		currState.mouseMoved(e.getX(), e.getY());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		switch (GameStates.gameState) {
		case EDIT:
			game.getEdit().mouseWheelMoved(e);
			break;
		case EDIT_MAP_SELECT:
			game.getEditMapSelect().mouseWheelMoved(e);
			break;
		case PLAY:
			game.getPlay().mouseWheelMoved(e);
			break;
		case PLAY_MAP_SELECT:
			game.getPlayMapSelect().mouseWheelMoved(e);
			break;
		default:
			break;
		}
		
	}

}
