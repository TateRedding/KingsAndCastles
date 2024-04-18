package src.gamestates;

import java.awt.Graphics;

public interface StateMethods {

	public void update();

	public void render(Graphics g);

	public void mousePressed(int x, int y, int button);

	public void mouseReleased(int x, int y, int button);

	public void mouseMoved(int x, int y);

}
