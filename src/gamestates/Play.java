package gamestates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import entities.Entity;
import handlers.EntityHandler;
import handlers.ResourceObjectHandler;
import main.Game;
import objects.GameObject;
import objects.Map;
import objects.Player;
import pathfinding.AStar;
import resources.ResourceObject;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import utils.Savable;

import static main.Game.TILE_SIZE;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Play extends MapState implements Savable, Serializable {

    private ActionBar actionBar;
    private GameStatBar gameStatBar;

    private ArrayList<Player> players = new ArrayList<>();

    private ResourceObjectHandler resourceObjectHandler;
    private EntityHandler entityHandler;

    private String name;
    private long seed;

    private boolean paused;

    public Play(Game game, Map map, String name) {
        super(game, map);
        this.name = name;
        this.seed = System.currentTimeMillis();
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);

        int numPlayers = map.getNumPlayers();
        // Player 1 will always be human
        players.add(new Player(this, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        for (int i = 0; i < numPlayers - 1; i++)
            players.add(new Player(this, false));

        this.entityHandler = new EntityHandler(this);
    }

    public Play(Game game, Map map, String name, long seed) {
        super(game, map);
        this.name = name;
        this.seed = seed;
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);

        int numPlayers = map.getNumPlayers();
        // Player 1 will always be human
        players.add(new Player(this, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        for (int i = 0; i < numPlayers - 1; i++)
            players.add(new Player(this, false));

        this.entityHandler = new EntityHandler(this);
    }

    @Override
    public void update() {
        super.update();
        entityHandler.update();
        if (actionBar != null)
            actionBar.update();
        if (gameStatBar != null)
            gameStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        entityHandler.render(g, xTileOffset, yTileOffset);

        actionBar.render(g);
        gameStatBar.render(g);
        miniMap.render(g, xTileOffset, yTileOffset);
    }

    public void saveGame() {
        game.getSaveFileHandler().saveGame(this);
    }

    public GameObject getGameObjectAt(int x, int y) {
        Entity e = entityHandler.getEntityAt(x, y);
        if (e != null)
            return e;

        ResourceObject ro = resourceObjectData[(y - TOP_BAR_HEIGHT) / TILE_SIZE][x / TILE_SIZE];
        if (ro != null)
            return ro;
        return null;
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

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ResourceObjectHandler getResourceObjectHandler() {
        return resourceObjectHandler;
    }

    public long getSeed() {
        return seed;
    }
}
