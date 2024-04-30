package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;

import handlers.ResourceHandler;
import main.Game;
import objects.Map;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import utils.Savable;

public class Play extends MapState implements Savable, Serializable {

    private ActionBar actionBar;
    private GameStatBar gameStatBar;

    private ResourceHandler resourceHandler;

    private String name;

    public Play(Game game, Map map, String name) {
        super(game, map);
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
        this.name = name;
        this.resourceHandler = new ResourceHandler(this);
    }

    @Override
    public void update() {
        resourceHandler.update();

        actionBar.update();
        gameStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        resourceHandler.render(g, xTileOffset, yTileOffset);

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
        super.mouseDragged(x, y);
        if (inGameArea)
            dragScreen(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        if (actionBar.getBounds().contains(x, y))
            actionBar.mouseMoved(x, y);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mouseMoved(x, y);
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public GameStatBar getGameStatBar() {
        return gameStatBar;
    }

    public void setGameStatBar(GameStatBar gameStatBar) {
        this.gameStatBar = gameStatBar;
    }

    public String getName() {
        return name;
    }

}
