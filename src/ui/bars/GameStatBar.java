package ui.bars;

import java.awt.Graphics;

import gamestates.Play;

public class GameStatBar extends TopBar {
	
	private Play play;
	
	public GameStatBar(Play play) {
		this.play = play;
	}
	
	public void update() {

	}
	
	@Override
	public void render(Graphics g) {
		super.render(g);
	}
	
	public void mousePressed(int x, int y, int button) {

	}

	public void mouseReleased(int x, int y, int button) {

	}

	public void mouseMoved(int x, int y) {
		
	}
	
	public Play getPlay() {
		return play;
	}

}
