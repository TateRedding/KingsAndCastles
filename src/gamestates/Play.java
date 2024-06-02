package gamestates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import entities.Entity;
import entities.Laborer;
import handlers.EntityHandler;
import handlers.ResourceObjectHandler;
import main.Game;
import objects.GameObject;
import objects.Map;
import objects.Player;
import resources.ResourceObject;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import utils.ImageLoader;
import utils.Savable;

import static main.Game.TILE_SIZE;
import static objects.GameObject.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Play extends MapState implements Savable, Serializable {

    // Actions
    static final int SELECT = 0;
    private static final int MOVE = 1;
    private static final int CHOP = 2;
    private static final int MINE = 3;
    private static final int FARM = 4;
    private static final int REPAIR = 5;
    private static final int ATTACK_MELEE = 6;
    private static final int ATTACK_RANGED = 7;

    private ActionBar actionBar;
    private GameStatBar gameStatBar;

    private ArrayList<Player> players = new ArrayList<>();
    private GameObject selectedGameObject, hoverGameObject;

    private ResourceObjectHandler resourceObjectHandler;
    private EntityHandler entityHandler;

    private String name;
    private long seed;
    private int action = -1;

    private boolean paused;

    public Play(Game game, Map map, String name) {
        super(game, map);
        this.name = name;
        this.seed = System.currentTimeMillis();
        this.actionBar = new ActionBar(this);
        int numPlayers = map.getNumPlayers();
        // Player 1 will always be human
        players.add(new Player(this, 1, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        for (int i = 1; i < numPlayers; i++)
            players.add(new Player(this, i + 1, false));

        this.entityHandler = new EntityHandler(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);
    }

    public Play(Game game, Map map, String name, long seed) {
        super(game, map);
        this.name = name;
        this.seed = seed;
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);

        int numPlayers = map.getNumPlayers();
        // Player 1 will always be human, and will always be the displayed & actioned Player
        players.add(new Player(this, 1, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        // In multiplayer, these will be actioned through networking packets
        for (int i = 1; i < numPlayers; i++)
            players.add(new Player(this, i + 1, false));

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

        if (inGameArea)
            renderAction(g, xTileOffset, yTileOffset);
        if (selectedGameObject != null)
            highlightSelectedGameObject(g, xTileOffset, yTileOffset);
    }

    private void renderAction(Graphics g, int xOffset, int yOffset) {
        if (action != -1)
            g.drawImage(ImageLoader.actions[action], gameX, gameY, null);

    }

    private void highlightSelectedGameObject(Graphics g, int xTileOffset, int yTileOffset) {
        g.setColor(new Color(255, 255, 0, 100));
        Rectangle bounds = selectedGameObject.getHitbox();
        g.fillRect(bounds.x - (xTileOffset * TILE_SIZE), bounds.y - (yTileOffset * TILE_SIZE), bounds.width, bounds.height);
    }

    private void determineAction() {
        if (selectedGameObject == null && hoverGameObject == null) {
            // Case 1: Both are null
            action = -1;
        } else if (selectedGameObject == null) {
            // Case 2: Only selectedGameObject is null
            action = SELECT;
        } else if (hoverGameObject == null) {
            // Case 3: Only hoverGameObject is null
            if (selectedGameObject.getPlayerNum() == 1)
                action = MOVE;
            else
                action = -1;
        } else {
            // Case 4: Neither are null
            int selectedType = selectedGameObject.getType();
            int hoverCat = hoverGameObject.getCategory();
            int hoverType = hoverGameObject.getType();

            if (selectedGameObject.getPlayerNum() != 1) {
                action = SELECT;
            } else {
                if (selectedType == LABORER) {
                    if (hoverCat == RESOURCE) {
                        if (hoverType == TREE)
                            action = CHOP;
                        else
                            action = MINE;
                    } else if (hoverCat == BUILDING) {
                        if (hoverGameObject.getPlayerNum() == 1) {
                            // If hovering over a building that that isn't at full health
                            //// repair
                            // If hovering over farm that has room for workers (must be full health)
                            //// farm
                        }
                    } else
                        action = SELECT;
                } else {
                    if (hoverGameObject.getPlayerNum() != 1) {
                        if (hoverCat == ENTITY || hoverCat == BUILDING) {
                            // If melee unit selected
                            //// attack with melee
                            // If ranged unit selected
                            //// attack with ranged
                        }
                        if (hoverCat == BUILDING) {
                            // If it's just a building and selected unit is siege
                            //// attack siege (could just suffice with ranged)
                        }
                    }
                }
            }
        }
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
        if (inGameArea) {
            if (button == MouseEvent.BUTTON1) {
                if (action == SELECT) {
                    selectedGameObject = hoverGameObject;
                } else if (action == MOVE) {
                    entityHandler.moveTo((Entity) selectedGameObject, tileX, tileY);
                }
            }
        }

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

        if (inGameArea) {
            hoverGameObject = getGameObjectAt(x, y);
            determineAction();
        }
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

    public GameObject getSelectedGameObject() {
        return selectedGameObject;
    }
}
