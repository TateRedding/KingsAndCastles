package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import buildings.*;
import entities.Entity;
import handlers.BuildingHandler;
import handlers.EntityHandler;
import handlers.ResourceObjectHandler;
import main.Game;
import objects.*;
import resources.ResourceObject;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import ui.overlays.BuildingSelection;
import ui.overlays.Overlay;
import utils.ImageLoader;
import utils.Savable;

import static buildings.Building.*;
import static entities.Entity.*;
import static main.Game.*;
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
    private long activePlayerID;

    private int clickAction = -1;

    private boolean canBuild = false;
    private int selectedBuildingType = -1;
    private int buildingID = 1;

    private int indicatorAnimationFrame = 0;
    private int indicatorAnimationTick = 0;
    private int indicatorAnimationTickMax = 15;

    private BuildingSelection buildingSelection;
    private boolean showBuildingSelection;

    private boolean paused;

    public Play(Game game, Map map, String name, long playerID) {
        super(game, map);
        this.name = name;
        this.seed = System.currentTimeMillis();
        this.activePlayerID = playerID;
        this.actionBar = new ActionBar(this);
        int numPlayers = map.getNumPlayers();

        // Add the human player to the array
        // In the future, when accounts are implemented, the playerID will be generated and used here
        // In testing, or with 'guest accounts' this can be the nano-time
        players.add(new Player(this, playerID, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        // AI players can use the nano-time as a player ID
        for (int i = 1; i < numPlayers; i++)
            players.add(new Player(this, System.nanoTime(), false));

        this.buildingHandler = new BuildingHandler(this);
        this.entityHandler = new EntityHandler(this);
        this.gameStatBar = new GameStatBar(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);

        int xStart = (GAME_AREA_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_LARGE)) / 2;
        int yStart = TOP_BAR_HEIGHT + (GAME_AREA_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_LARGE)) / 2;
        this.buildingSelection = new BuildingSelection(xStart, yStart, this);
    }

    public Play(Game game, Map map, String name, long seed, int playerID) {
        super(game, map);
        this.name = name;
        this.seed = seed;
        this.activePlayerID = playerID;
        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);

        int numPlayers = map.getNumPlayers();

        // Add the human player to the array
        // In the future, when accounts are implemented, the playerID will be generated and used here
        // In testing, or with 'guest accounts' this can be the nano-time
        players.add(new Player(this, playerID, true));

        // Until multiplayer is introduced, the rest of the players will always be AI
        // AI players can use the nano-time as a player ID
        for (int i = 1; i < numPlayers; i++)
            players.add(new Player(this, System.nanoTime(), false));

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

        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.update();

    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        // Debugging
        drawChunkBorders(g);

        buildingHandler.render(g, mapXOffset, mapYOffset);
        entityHandler.render(g, mapXOffset, mapYOffset);
        resourceObjectHandler.render(g, mapXOffset, mapYOffset);

        highlightSelectedObject(g, mapXOffset, mapYOffset);

        actionBar.render(g);
        gameStatBar.render(g);

        miniMap.render(g, mapXOffset, mapYOffset);

        if (inGameArea)
            if (selectedBuildingType == -1)
                renderAction(g, mapXOffset, mapYOffset);
            else
                renderSelectedBuilding(g, mapXOffset, mapYOffset);

        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.render(g);
    }

    private void renderAction(Graphics g, int xOffset, int yOffset) {
        if (clickAction != -1) {
            int x = gameX;
            int y = gameY;
            if (clickAction == SELECT && hoverGO.getGameObjectType() == ENTITY) {
                x = hoverGO.getHitbox().x;
                y = hoverGO.getHitbox().y;
            }
            g.drawImage(ImageLoader.actions[clickAction], x - xOffset, y - yOffset, null);
        }

    }

    private void renderSelectedBuilding(Graphics g, int xOffset, int yOffset) {
        ArrayList<Point> tiles = getBuildingTiles(gameX, gameY);
        g.drawImage(ImageLoader.buildings[selectedBuildingType], toPixelX(tiles.get(0).x) - xOffset, toPixelY(tiles.get(0).y) - yOffset, null);

        for (Point p : tiles) {
            int currPixelX = toPixelX(p.x);
            int currPixelY = toPixelY(p.y);
            boolean canBuildOnTile = canBuildHere(currPixelX, currPixelY, false);
            int xStart = currPixelX - xOffset;
            int yStart = currPixelY - yOffset;
            if (canBuildOnTile)
                g.drawImage(ImageLoader.buildIndicators[0], xStart, yStart, null);
            else
                g.drawImage(ImageLoader.buildIndicators[1], xStart, yStart, null);
        }
    }

    private void highlightSelectedObject(Graphics g, int xOffset, int yOffset) {
        Rectangle bounds = null;
        if (selectedSGO != null)
            bounds = selectedSGO.getHitbox();
        if (bounds != null)
            g.drawImage(ImageLoader.selectIndicator[indicatorAnimationFrame], bounds.x - xOffset, bounds.y - yOffset, null);
    }

    private void drawChunkBorders(Graphics g) {
        Chunk[][] chunks = map.getChunks();
        for (int y = 0; y < chunks.length; y++)
            for (int x = 0; x < chunks[y].length; x++) {
                Rectangle bounds = chunks[y][x].getBounds();
                g.setColor(new Color(255, 255, 0));
                g.drawRect(bounds.x - mapXOffset, bounds.y - mapYOffset, bounds.width, bounds.height);
            }
    }

    public void determineAction() {
        clickAction = -1;  // Default action
        int sgoType = (selectedSGO != null) ? selectedSGO.getGameObjectType() : -1;
        int hoverType = (hoverGO != null) ? hoverGO.getGameObjectType() : -1;

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
            if (selectedSGO.getPlayer().getPlayerID() != activePlayerID) {
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
            if (player.getPlayerID() != activePlayerID) {
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

        return resourceObjectData[toTileY(y)][toTileX(x)];
    }

    private boolean canBuildHere(int x, int y, boolean checkAllBuildingTiles) {
        if (selectedBuildingType == CASTLE_TURRET) {
            Building b = buildingHandler.getBuildingAt(gameX, gameY);
            return b != null && b.getBuildingType() == CASTLE_WALL;
        } else {
            int tileX = toTileX(x);
            int tileY = toTileY(y);
            ArrayList<Point> tiles = new ArrayList<Point>();
            if (checkAllBuildingTiles)
                tiles = getBuildingTiles(x, y);
            else
                tiles.add(new Point(tileX, tileY));

            for (Point p : tiles) {
                if (selectedBuildingType == THRONE_ROOM || selectedBuildingType == CASTLE_WALL)
                    if (!map.getCastleZones().get(0).contains(p))
                        return false;
                if (!isTileBuildable(p))
                    return false;
            }
        }
        return true;
    }

    private ArrayList<Point> getBuildingTiles(int x, int y) {
        int buildingTileWidth = getBuildingTileWidth(selectedBuildingType);
        int buildingTileHeight = getBuildingTileHeight(selectedBuildingType);
        int maxTileX = map.getTileData()[0].length - buildingTileWidth;
        int maxTileY = map.getTileData().length - buildingTileHeight;
        int tileX = toTileX(gameX);
        if (tileX > maxTileX)
            tileX = maxTileX;
        int tileY = toTileY(gameY);
        if (tileY > maxTileY)
            tileY = maxTileY;

        ArrayList<Point> tiles = new ArrayList<>();
        for (int currY = tileY; currY < tileY + buildingTileHeight; currY++)
            for (int currX = tileX; currX < tileX + buildingTileWidth; currX++)
                tiles.add(new Point(currX, currY));

        return tiles;
    }

    private boolean isTileBuildable(Point tile) {
        if (getGameObjectAt(toPixelX(tile.x), toPixelY(tile.y), true) != null)
            return false;
        int tileType = map.getTileData()[tile.y][tile.x].getTileType();
        return (tileType != WATER_GRASS && tileType != WATER_SAND);
    }

    public boolean canAffordBuilding(int buildingType) {
        Player player = getPlayerByID(activePlayerID);
        if (player != null) {
            if (!(player.getCoal() >= Building.getCostCoal(buildingType)))
                return false;

            if (!(player.getGold() >= Building.getCostGold(buildingType)))
                return false;

            if (!(player.getIron() >= Building.getCostIron(buildingType)))
                return false;

            if (!(player.getStone() >= Building.getCostStone(buildingType)))
                return false;

            return player.getLogs() >= Building.getCostLogs(buildingType);
        }
        return false;
    }

    private void buildBuilding() {
        Player player = getPlayerByID(activePlayerID);
        if (player != null) {

            int maxX = toPixelX(map.getTileData()[0].length - getBuildingTileWidth(selectedBuildingType));
            int maxY = toPixelY(map.getTileData().length - getBuildingTileHeight(selectedBuildingType));
            int tileX = gameX;
            if (tileX > maxX)
                tileX = maxX;
            int tileY = gameY;
            if (tileY > maxY)
                tileY = maxY;

            Building building = createBuilding(player, selectedBuildingType, buildingID++, tileX, tileY);
            if (building != null) {
                buildingHandler.getBuildings().add(building);
                player.buildBuilding(selectedBuildingType);
            }
        }
    }

    private Player getPlayerByID(long playerID) {
        for (Player p : players)
            if (p.getPlayerID() == playerID)
                return p;
        return null;
    }

    ;

    private Building createBuilding(Player player, int buildingType, int id, int x, int y) {
        return switch (buildingType) {
            case Building.THRONE_ROOM -> new ThroneRoom(player, id, x, y);
            case Building.CASTLE_WALL -> new CastleWall(player, id, x, y);
            case Building.CASTLE_TURRET -> new CastleTurret(player, id, x, y);
            case Building.VILLAGE -> new Village(player, id, x, y);
            case Building.STORAGE_HUT -> new StorageHut(player, id, x, y);
            case Building.REFINERY -> new Refinery(player, id, x, y);
            case Building.FARM -> new Farm(player, id, x, y, false);
            case Building.FARM_ROTATED -> new Farm(player, id, x, y, true);
            case Building.BARRACKS_TIER_1 -> new Barracks(player, id, x, y, 1);
            case Building.BARRACKS_TIER_2 -> new Barracks(player, id, x, y, 2);
            case Building.BARRACKS_TIER_3 -> new Barracks(player, id, x, y, 3);
            default -> null;
        };
    }


    @Override
    public void mousePressed(int x, int y, int button) {
        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.mousePressed(x, y, button);
        else if (!showBuildingSelection)
            super.mousePressed(x, y, button);

        if (actionBar.getBounds().contains(x, y))
            actionBar.mousePressed(x, y, button);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.mouseReleased(x, y, button);
        else if (!showBuildingSelection) {
            super.mouseReleased(x, y, button);
            if (inGameArea) {
                if (button == MouseEvent.BUTTON1) {
                    if (selectedBuildingType != -1 && canBuild && canAffordBuilding(selectedBuildingType)) {
                        buildBuilding();
                        selectedBuildingType = -1;
                    }

                    if (clickAction == SELECT) {
                        selectedSGO = (SelectableGameObject) hoverGO;
                        clickAction = -1;
                        selectedBuildingType = -1;
                    } else if (clickAction != -1) {
                        Entity selectedEntity = (Entity) selectedSGO;
                        if (clickAction == MOVE) {
                            entityHandler.setPathToTile(selectedEntity, tileX, tileY);
                            selectedEntity.setResourceToGather(null);
                            selectedEntity.setEntityToAttack(null);
                        } else if (clickAction == CHOP || clickAction == MINE) {
                            ResourceObject hoverResourceObject = (ResourceObject) hoverGO;
                            boolean isInRangeAndReachable = (selectedEntity.isTargetInRange(hoverResourceObject, selectedEntity.getActionRange()) && (selectedEntity.isLineOfSightOpen(hoverResourceObject)));
                            ArrayList<Point> path = null;
                            if (!isInRangeAndReachable) {
                                path = entityHandler.getPathToNearestAdjacentTile(selectedEntity, tileX, tileY);
                                if (path != null)
                                    selectedEntity.setPath(path);
                            }
                            if (isInRangeAndReachable || path != null)
                                selectedEntity.setResourceToGather(hoverResourceObject);
                        } else if (clickAction == ATTACK_MELEE) {
                            Entity hoverEntity = (Entity) hoverGO;
                            boolean isInRangeAndReachable = (selectedEntity.isTargetInRange(hoverEntity, selectedEntity.getActionRange()) && (selectedEntity.isLineOfSightOpen(hoverEntity)));
                            ArrayList<Point> path = null;
                            if (!isInRangeAndReachable) {
                                path = entityHandler.getPathToNearestAdjacentTile(selectedEntity, tileX, tileY);
                                if (path != null)
                                    selectedEntity.setPath(path);
                            }
                            if (isInRangeAndReachable || path != null)
                                selectedEntity.setEntityToAttack(hoverEntity);
                        }
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
        if (showBuildingSelection && buildingSelection != null)
            return;
        super.mouseDragged(x, y);
        if (inGameArea) {
            if (selectedBuildingType == -1 && selectedSGO == null)
                dragScreen(x, y);

            int mouseDownTileX = toTileX(mouseDownX);
            int mouseDownTileY = toTileY(mouseDownY);
            int currTileX = toTileX(x);
            int currTileY = toTileY(y);
            if (mouseDownTileX != currTileX || mouseDownTileY != currTileY)
                clickAction = -1;
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.mouseMoved(x, y);
        else if (!showBuildingSelection) {
            super.mouseMoved(x, y);
            if (inGameArea) {
                if (selectedBuildingType == -1) {
                    hoverGO = getGameObjectAt(x + mapXOffset, y + mapYOffset, false);
                    determineAction();
                } else
                    canBuild = canBuildHere(gameX, gameY, true);
            }
        }

        if (actionBar.getBounds().contains(x, y))
            actionBar.mouseMoved(x, y);
        else if (gameStatBar.getBounds().contains(x, y))
            gameStatBar.mouseMoved(x, y);
    }

    @Override
    public void mouseWheelMoved(int dir, int amt) {
        super.mouseWheelMoved(dir, amt);
        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.mouseWheelMoved(dir, amt);
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

    public BuildingSelection getBuildingSelection() {
        return buildingSelection;
    }

    public void setBuildingSelection(BuildingSelection buildingSelection) {
        this.buildingSelection = buildingSelection;
    }

    public void setClickAction(int clickAction) {
        this.clickAction = clickAction;
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

    public int getSelectedBuildingType() {
        return selectedBuildingType;
    }

    public void setSelectedBuildingType(int selectedBuildingType) {
        this.selectedBuildingType = selectedBuildingType;
        if (selectedBuildingType != -1)
            clickAction = -1;
    }

    public SelectableGameObject getSelectedSGO() {
        return selectedSGO;
    }

    public void setSelectedSGO(SelectableGameObject selectedSGO) {
        this.selectedSGO = selectedSGO;
    }

    public boolean isShowBuildingSelection() {
        return showBuildingSelection;
    }

    public void setShowBuildingSelection(boolean showBuildingSelection) {
        this.showBuildingSelection = showBuildingSelection;
    }
}
