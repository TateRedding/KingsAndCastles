package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;

import main.Game;
import objects.Map;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;

public class Play extends MapState implements StateMethods {

    private ActionBar actionBar;
    private GameStatBar gameStatBar;

    private String name;

    public Play(Game game, Map map, String name) {
        super(game, map);
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
        this.name = name;
    }

    @Override
    public void update() {
        actionBar.update();
        gameStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        actionBar.render(g);
        gameStatBar.render(g);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (actionBar.getBounds().contains(x, y))
            actionBar.mousePressed(x, y, button);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (actionBar.getBounds().contains(x, y))
            actionBar.mouseReleased(x, y, button);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mouseReleased(x, y, button);

    }

    @Override
    public void mouseDragged(int x, int y) {

    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        if (actionBar.getBounds().contains(x, y))
            actionBar.mouseMoved(x, y);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mouseMoved(x, y);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
    }

    public void keyPressed(KeyEvent e) {

    }

    public String getName() {
        return this.name;
    }

}
