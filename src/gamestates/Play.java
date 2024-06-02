package gamestates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import buildings.Building;
import entities.Entity;
import handlers.BuildingHandler;
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

import static entities.Entity.*;
import static main.Game.TILE_SIZE;
import static resources.ResourceObject.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Play extends MapState implements Savable, Serializable {

    // Actions
    public static final int SELECT = 0;
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
    private Entity selectedEntity, hoverEntity;
    private Building selectedBuilding, hoverBuilding;
    private ResourceObject hoverResourceObject;

    private BuildingHandler buildingHandler;
    private EntityHandler entityHandler;
    private ResourceObjectHandler resourceObjectHandler;

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

        this.buildingHandler = new BuildingHandler(this);
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

        this.buildingHandler = new BuildingHandler(this);
        this.entityHandler = new EntityHandler(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);
    }

    @Override
    public void update() {
        super.update();
        buildingHandler.update();
        entityHandler.update();
        if (actionBar != null)
            actionBar.update();
        if (gameStatBar != null)
            gameStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        buildingHandler.render(g, xTileOffset, yTileOffset);
        entityHandler.render(g, xTileOffset, yTileOffset);

        actionBar.render(g);
        gameStatBar.render(g);
        miniMap.render(g, xTileOffset, yTileOffset);

        if (inGameArea)
            renderAction(g, xTileOffset, yTileOffset);
        highlightSelectedObject(g, xTileOffset, yTileOffset);
    }

    private void renderAction(Graphics g, int xOffset, int yOffset) {
        if (action != -1)
            g.drawImage(ImageLoader.actions[action], gameX, gameY, null);

    }

    private void highlightSelectedObject(Graphics g, int xTileOffset, int yTileOffset) {
        Rectangle bounds = null;
        if (selectedBuilding != null)
            bounds = selectedBuilding.getHitbox();
        else if (selectedEntity != null)
            bounds = selectedEntity.getHitbox();
        if (bounds != null) {
            g.setColor(new Color(255, 255, 0, 100));
            g.fillRect(bounds.x - (xTileOffset * TILE_SIZE), bounds.y - (yTileOffset * TILE_SIZE), bounds.width, bounds.height);
        }
    }

    public void determineAction() {
        action = -1;  // Default action

        if (selectedEntity == null) {
            if (hoverEntity != null || hoverBuilding != null)
                action = SELECT;
        } else {
            if (selectedEntity.getPlayer().getPlayerNum() != 1) {
                if (hoverEntity != null || hoverBuilding != null)
                    action = SELECT;
            } else {
                if (selectedEntity.getEntityType() == LABORER && (hoverBuilding != null || hoverEntity != null))
                    action = SELECT;
                else if (hoverEntity == null && hoverBuilding == null && hoverResourceObject == null)
                    action = MOVE;
                else if (selectedEntity.getEntityType() == LABORER) {
                    if (hoverResourceObject != null) {
                        if (hoverResourceObject.getResourceType() == TREE)
                            action = CHOP;
                        else
                            action = MINE;

                    } else if (hoverBuilding != null) {
                        if (hoverBuilding.getBuildingType() == Building.FARM && hoverBuilding.getHealth() == hoverBuilding.getMaxHealth())
                            action = FARM;
                        else if (hoverBuilding.getHealth() < hoverBuilding.getMaxHealth())
                            action = REPAIR;
                    }
                } else {
                    if (hoverEntity != null && hoverEntity.getPlayer().getPlayerNum() != 1) {
                        if (getAttackStyle(selectedEntity.getEntityType()) == MELEE)
                            action = ATTACK_MELEE;
                        else if (getAttackStyle(selectedEntity.getEntityType()) == RANGED)
                            action = ATTACK_RANGED;
                    } else if (hoverBuilding != null && hoverBuilding.getPlayer().getPlayerNum() != 1) {
                        if (getAttackStyle(selectedEntity.getEntityType()) == MELEE)
                            action = ATTACK_MELEE;
                        else if (getAttackStyle(selectedEntity.getEntityType()) == RANGED)
                            action = ATTACK_RANGED;
                    }
                }
            }
        }
    }

    public void gatherResource(Player player, ResourceObject ro) {
        int resourceType = ro.getResourceType();
        int amt = ResourceObject.getAmountPerAction(resourceType);
        switch (resourceType) {
            case GOLD -> player.setGold(player.getGold() + amt);
            case TREE -> player.setWood(player.getWood() + amt);
            case ROCK -> player.setStone(player.getStone() + amt);
            case COAL -> player.setCoal(player.getCoal() + amt);
            case IRON -> player.setIron(player.getIron() + amt);
        }
    }

    public void saveGame() {
        game.getSaveFileHandler().saveGame(this);
    }

    public GameObject getGameObjectAt(int x, int y) {
        Entity e = entityHandler.getEntityAt(x, y);
        if (e != null)
            return e;

        Building b = buildingHandler.getBuildingAt(x, y);
        if (b != null)
            return b;

        return resourceObjectData[(y - TOP_BAR_HEIGHT) / TILE_SIZE][x / TILE_SIZE];
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
                    selectedEntity = hoverEntity;
                    selectedBuilding = hoverBuilding;
                } else if (action == MOVE) {
                    entityHandler.moveTo(selectedEntity, tileX, tileY);
                } else if (action == CHOP || action == MINE) {
                    entityHandler.moveToNearestTile(selectedEntity, tileX, tileY);
                    selectedEntity.setResourceToGather(hoverResourceObject);
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
            hoverBuilding = buildingHandler.getBuildingAt(x, y);
            hoverEntity = entityHandler.getEntityAt(x, y);
            hoverResourceObject = resourceObjectData[tileY][tileX];
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

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }
}
