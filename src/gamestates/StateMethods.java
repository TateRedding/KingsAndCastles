package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public interface StateMethods {

    public void update();

    public void render(Graphics g);

    public void mousePressed(int x, int y, int button);

    public void mouseReleased(int x, int y, int button);

    public void mouseEntered(int x, int y);

    public void mouseExited(int x, int y);

    public void mouseDragged(int x, int y);

    public void mouseMoved(int x, int y);

    public void mouseWheelMoved(int dir, int amt);

    public void keyPressed(KeyEvent e);

}
