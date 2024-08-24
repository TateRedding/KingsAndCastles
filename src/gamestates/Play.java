package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import buildings.Building;
import buildings.Farm;
import entities.Entity;
import handlers.BuildingHandler;
import handlers.EntityHandler;
import handlers.ResourceObjectHandler;
import main.Game;
import objects.*;
import resources.ResourceObject;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import utils.ImageLoader;
import utils.Savable;

import static entities.Entity.*;
import static main.Game.TILE_SIZE;
import static objects.Tile.WATER_GRASS;
import static objects.Tile.WATER_SAND;
import static resources.ResourceObject.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Play extends MapState implements Savable, Serializable {

    // Click actions
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
    private SelectableGameObject selectedSGO;
    private GameObject hoverGO;

    private BuildingHandler buildingHandler;
    private EntityHandler entityHandler;
    private ResourceObjectHandler resourceObjectHandler;

    private String name;
    private long seed;
    private int clickAction = -1;

    private boolean canBuildOnMouseTile = false;
    private int selectedBuildingType = -1;
    private int buildingID = 1;

    private int indicatorAnimationFrame = 0;
    private int indicatorAnimationTick = 0;
    private int indicatorAnimationTickMax = 15;

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

        indicatorAnimationTick++;
        if (indicatorAnimationTick > indicatorAnimationTickMax) {
            indicatorAnimationTick = 0;
            indicatorAnimationFrame++;
            if (indicatorAnimationFrame >= ImageLoader.selectIndicator.length)
                indicatorAnimationFrame = 0;
        }

    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        // Debugging
        drawChunkBorders(g);

        buildingHandler.render(g, xTileOffset, yTileOffset);
        entityHandler.render(g, xTileOffset, yTileOffset);
        resourceObjectHandler.render(g, xTileOffset, yTileOffset);

        highlightSelectedObject(g, xTileOffset, yTileOffset);

        actionBar.render(g);
        gameStatBar.render(g);

        miniMap.render(g, xTileOffset, yTileOffset);

        if (inGameArea)
            if (selectedBuildingType == -1)
                renderAction(g, xTileOffset, yTileOffset);
            else
                renderSelectedBuilding(g);
    }

    private void renderAction(Graphics g, int xOffset, int yOffset) {
        if (clickAction != -1) {
            int x = gameX;
            int y = gameY;
            if (clickAction == SELECT && hoverGO.getType() == ENTITY) {
                x = hoverGO.getHitbox().x;
                y = hoverGO.getHitbox().y;
            }
            g.drawImage(ImageLoader.actions[clickAction], x - (xOffset * TILE_SIZE), y - (yOffset * TILE_SIZE), null);
        }

    }

    private void renderSelectedBuilding(Graphics g) {
        if (canBuildOnMouseTile)
            g.setColor(new Color(0, 255, 0, 50));
        else
            g.setColor(new Color(255, 0, 0, 50));
        g.drawImage(ImageLoader.buildings[selectedBuildingType], mouseX, mouseY, null);
        g.fillRect(mouseX, mouseY, TILE_SIZE, TILE_SIZE);
    }

    private void highlightSelectedObject(Graphics g, int xTileOffset, int yTileOffset) {
        Rectangle bounds = null;
        if (selectedSGO != null)
            bounds = selectedSGO.getHitbox();
        if (bounds != null)
            g.drawImage(ImageLoader.selectIndicator[indicatorAnimationFrame], bounds.x - (xTileOffset * TILE_SIZE), bounds.y - (yTileOffset * TILE_SIZE), null);

    }

    private void drawChunkBorders(Graphics g) {
        Chunk[][] chunks = map.getChunks();
        for (int y = 0; y < chunks.length; y++)
            for (int x = 0; x < chunks[y].length; x++) {
                Rectangle bounds = chunks[y][x].getBounds();
                g.setColor(new Color(255, 255, 0));
                g.drawRect(bounds.x - (xTileOffset * TILE_SIZE), bounds.y - (yTileOffset * TILE_SIZE), bounds.width, bounds.height);
            }
    }

    public void determineAction() {
        clickAction = -1;  // Default action
        int sgoType = (selectedSGO != null) ? selectedSGO.getType() : -1;
        int hoverType = (hoverGO != null) ? hoverGO.getType() : -1;

        if (sgoType == hoverType && selectedSGO != null && hoverGO != null && selectedSGO.getId() == hoverGO.getId())
            return;

        if (sgoType == BUILDING || sgoType == -1) {
            if (hoverType == ENTITY || hoverType == BUILDING) {
                clickAction = SELECT;
            }
            return;
        }

        if (sgoType == ENTITY) {
            Entity selectedEntity = (Entity) selectedSGO;
            if (selectedSGO.getPlayer().getPlayerNum() != 1) {
                if (hoverType == ENTITY || hoverType == BUILDING) {
                    clickAction = SELECT;
                }
            } else {
                handlePlayerEntityAction(hoverType, selectedEntity);
            }
        }
    }

    private void handlePlayerEntityAction(int hoverType, Entity selectedEntity) {
        int entityType = selectedEntity.getEntityType();

        if (entityType == LABORER && (hoverType == ENTITY || hoverType == BUILDING)) {
            clickAction = SELECT;
        } else if (hoverType == -1) {
            clickAction = MOVE;
        } else if (entityType == LABORER) {
            handleLaborerAction(hoverType);
        } else {
            handleCombatAction(hoverType, selectedEntity);
        }
    }

    private void handleLaborerAction(int hoverType) {
        if (hoverType == RESOURCE) {
            ResourceObject resourceObject = (ResourceObject) hoverGO;
            clickAction = (resourceObject.getResourceType() == TREE) ? CHOP : MINE;
        } else if (hoverType == BUILDING) {
            Building hoverBuilding = (Building) hoverGO;
            if (hoverBuilding.getBuildingType() == Building.FARM && hoverBuilding.getHealth() == hoverBuilding.getMaxHealth()) {
                clickAction = FARM;
            } else if (hoverBuilding.getHealth() < hoverBuilding.getMaxHealth()) {
                clickAction = REPAIR;
            }
        }
    }

    private void handleCombatAction(int hoverType, Entity selectedEntity) {
        if (hoverType == ENTITY || hoverType == BUILDING) {
            Player player = ((SelectableGameObject) hoverGO).getPlayer();
            if (player.getPlayerNum() != 1) {
                int attackStyle = getAttackStyle(selectedEntity.getEntityType());
                clickAction = (attackStyle == MELEE) ? ATTACK_MELEE : ATTACK_RANGED;
            }
        }
    }

    public void saveGame() {
        game.getSaveFileHandler().saveGame(this);
    }

    public GameObject getGameObjectAt(int x, int y, boolean checkEntireTile) {
        Entity e = entityHandler.getEntityAtCoord(x, y, checkEntireTile);
        if (e != null)
            return e;

        Building b = buildingHandler.getBuildingAt(x, y);
        if (b != null)
            return b;

        return resourceObjectData[(y - TOP_BAR_HEIGHT) / TILE_SIZE][x / TILE_SIZE];
    }

    private boolean isTileBuildable() {
        if (getGameObjectAt(gameX, gameY, true) != null)
            return false;

        int tileX = gameX / TILE_SIZE;
        int tileY = (gameY - TOP_BAR_HEIGHT) / TILE_SIZE;
        int tileType = map.getTileData()[tileY][tileX].getTileType();
        return (tileType != WATER_GRASS && tileType != WATER_SAND);
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
                if (selectedBuildingType != -1 && canBuildOnMouseTile) {
                    switch (selectedBuildingType) {
                        case Building.FARM:
                            buildingHandler.getBuildings().add(new Farm(players.get(1), buildingID++, gameX, gameY));
                    }
                }

                if (clickAction == SELECT) {
                    selectedSGO = (SelectableGameObject) hoverGO;
                    clickAction = -1;
                    selectedBuildingType = -1;
                } else {
                    Entity selectedEntity = (Entity) selectedSGO;
                    if (clickAction == MOVE) {
                        entityHandler.setPathToTile(selectedEntity, tileX, tileY);
                        selectedEntity.setResourceToGather(null);
                        selectedEntity.setEntityToAttack(null);
                    } else if (clickAction == CHOP || clickAction == MINE) {
                        ResourceObject hoverResourceObject = (ResourceObject) hoverGO;
                        boolean isInRangeAndReachable = selectedEntity.isTargetInRangeAndReachable(hoverResourceObject);
                        ArrayList<Point> path = null;
                        if (!isInRangeAndReachable) {
                            path = entityHandler.getPathToNearestTile(selectedEntity, tileX, tileY);
                            if (path != null)
                                selectedEntity.setPath(path);
                        }
                        if (isInRangeAndReachable || path != null)
                            selectedEntity.setResourceToGather(hoverResourceObject);
                    }
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
            if (selectedBuildingType == -1) {
                hoverGO = getGameObjectAt(x + (xTileOffset * TILE_SIZE), y + (yTileOffset * TILE_SIZE), false);
                determineAction();
            } else
                canBuildOnMouseTile = isTileBuildable();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            selectedSGO = null;
            selectedBuildingType = -1;
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

    public void setSelectedBuildingType(int selectedBuildingType) {
        this.selectedBuildingType = selectedBuildingType;
    }

    public SelectableGameObject getSelectedSGO() {
        return selectedSGO;
    }
}
