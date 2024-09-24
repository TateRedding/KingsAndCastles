package gamestates;

import entities.buildings.Building;
import entities.buildings.CastleTurret;
import entities.buildings.Farm;
import entities.resources.ResourceObject;
import entities.units.Laborer;
import entities.units.Unit;
import handlers.BuildingHandler;
import handlers.ProjectileHandler;
import handlers.ResourceObjectHandler;
import handlers.UnitHandler;
import main.Game;
import objects.Chunk;
import entities.Entity;
import objects.Map;
import objects.Player;
import ui.bars.ActionBar;
import ui.bars.GameStatBar;
import ui.overlays.BuildingSelection;
import ui.overlays.Overlay;
import utils.ImageLoader;
import utils.Savable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import static entities.buildings.Building.BUILDING;
import static entities.buildings.Building.RESOURCE;
import static entities.buildings.Building.UNIT;
import static entities.buildings.Building.*;
import static entities.buildings.Farm.MAX_FARMERS;
import static entities.resources.ResourceObject.TREE;
import static entities.units.Unit.*;
import static main.Game.*;
import static pathfinding.AStar.*;
import static ui.bars.TopBar.TOP_BAR_HEIGHT;

public class Play extends MapState implements Savable, Serializable {

    // Click actions
    public static final int CA_SELECT = 0;
    private static final int CA_MOVE = 1;
    private static final int CA_CHOP = 2;
    private static final int CA_MINE = 3;
    public static final int CA_FARM = 4;
    private static final int CA_EMPTY_INVENTORY = 5;
    private static final int CA_REPAIR = 6;
    private static final int CA_ATTACK_MELEE = 7;
    private static final int CA_ATTACK_RANGED = 8;
    private static final int CA_DEFEND = 9;

    public static final int FOOD_CYCLE_MS = 30000;
    private long lastFoodCycle = System.currentTimeMillis();
    private boolean foodCycleThisUpdate;

    private ActionBar actionBar;
    private GameStatBar gameStatBar;

    private ArrayList<Player> players = new ArrayList<>();
    private Entity selectedEntity, hoverEntity;

    private BuildingHandler buildingHandler;
    private UnitHandler unitHandler;
    private ProjectileHandler projectileHandler;
    private ResourceObjectHandler resourceObjectHandler;
    private ResourceObject[][] resourceObjectData;

    private String name;
    private long seed;
    private long activePlayerID;

    private int clickAction = -1;

    private boolean canBuild = false;
    private int selectedBuildingType = -1;

    private int indicatorAnimationFrame = 0;
    private int indicatorAnimationTick = 0;
    private int indicatorAnimationTickMax = 15;

    private BuildingSelection buildingSelection;
    private boolean showBuildingSelection;

    private boolean paused;

    public Play(Game game, Map map, String name, long playerID) {
        this(game, map, name, System.currentTimeMillis(), playerID);
    }

    public Play(Game game, Map map, String name, long seed, long playerID) {
        super(game, map);
        this.name = name;
        this.seed = seed;
        this.activePlayerID = playerID;

        initPlayers(map, playerID);
        initComponents(map);

        int xStart = (GAME_AREA_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_LARGE)) / 2;
        int yStart = TOP_BAR_HEIGHT + (GAME_AREA_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_LARGE)) / 2;
        this.buildingSelection = new BuildingSelection(xStart, yStart, this);
    }

    private void initComponents(Map map) {
        this.resourceObjectData = new ResourceObject[map.getTileData().length][map.getTileData()[0].length];

        this.buildingHandler = new BuildingHandler(this);
        this.unitHandler = new UnitHandler(this);
        this.projectileHandler = new ProjectileHandler(this);
        this.resourceObjectHandler = new ResourceObjectHandler(this);

        this.actionBar = new ActionBar(this);
        this.gameStatBar = new GameStatBar(this);
    }

    private void initPlayers(Map map, long playerID) {
        int numPlayers = map.getNumPlayers();

        // Add the human player
        players.add(new Player(this, playerID, true));

        // Add AI players
        for (int i = 1; i < numPlayers; i++) {
            players.add(new Player(this, System.nanoTime(), false));
        }

    }


    @Override
    public void update() {
        super.update();
        if (System.currentTimeMillis() - lastFoodCycle >= FOOD_CYCLE_MS) {
            foodCycleThisUpdate = true;
            lastFoodCycle = System.currentTimeMillis();
        }
        if (!paused) {
            buildingHandler.update(foodCycleThisUpdate);
            projectileHandler.update();
            unitHandler.update(foodCycleThisUpdate);
        }

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

        foodCycleThisUpdate = false;
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        buildingHandler.render(g, mapXOffset, mapYOffset);
        resourceObjectHandler.render(g, mapXOffset, mapYOffset);
        projectileHandler.render(g, mapXOffset, mapYOffset);
        unitHandler.render(g, mapXOffset, mapYOffset);

        highlightSelectedObject(g, mapXOffset, mapYOffset);

        // Debugging
        if (Debug.config.get(Debug.DebugToggle.SHOW_CHUNK_BORDERS))
            drawChunkBorders(g);
        if (Debug.config.get(Debug.DebugToggle.SHOW_TILE_COORDINATES))
            drawTileCoords(g, mapXOffset, mapYOffset);

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
            if (clickAction == CA_SELECT && hoverEntity.getEntityType() == UNIT) {
                x = hoverEntity.getHitbox().x;
                y = hoverEntity.getHitbox().y;
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
        if (selectedEntity != null)
            bounds = selectedEntity.getHitbox();
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
        clickAction = -1;
        int sgoType = (selectedEntity != null) ? selectedEntity.getEntityType() : -1;
        int hoverEntityType = (hoverEntity != null) ? hoverEntity.getEntityType() : -1;

        if (sgoType == hoverEntityType && selectedEntity != null && hoverEntity != null && selectedEntity.getId() == hoverEntity.getId())
            return;

        if (sgoType == BUILDING || sgoType == -1) {
            if (hoverEntityType == UNIT || hoverEntityType == BUILDING)
                clickAction = CA_SELECT;
            return;
        }

        if (sgoType == UNIT) {
            if (selectedEntity.getPlayer().getPlayerID() != activePlayerID) {
                if (hoverEntityType == UNIT || hoverEntityType == BUILDING)
                    clickAction = CA_SELECT;
            } else
                determineOwnedUnitAction(hoverEntityType, (Unit) selectedEntity);
        }
    }

    private void determineOwnedUnitAction(int hoverEntityType, Unit selectedUnit) {
        int unitType = selectedUnit.getSubType();

        if (unitType == LABORER && (hoverEntityType == UNIT))
            clickAction = CA_SELECT;
        else if (hoverEntityType == -1)
            clickAction = CA_MOVE;
        else if (unitType == LABORER)
            determineLaborerAction();
        else
            determineCombatAction(hoverEntityType, selectedUnit);
    }

    private void determineLaborerAction() {
        if (hoverEntity.getEntityType() == RESOURCE)
            clickAction = (hoverEntity.getSubType() == TREE) ? CA_CHOP : CA_MINE;
        else if (hoverEntity instanceof Building hoverBuilding) {
            int buildingType = hoverEntity.getSubType();
            if (hoverBuilding.getHealth() < hoverBuilding.getMaxHealth())
                clickAction = CA_REPAIR;
            else if (buildingType == FARM)
                clickAction = CA_FARM;
            else if (buildingType == STORAGE_HUT || buildingType == REFINERY)
                clickAction = CA_EMPTY_INVENTORY;
        }
    }

    private void determineCombatAction(int hoverEntityType, Unit selectedUnit) {
        if (hoverEntityType != UNIT && hoverEntityType != BUILDING)
            return;

        Player player = hoverEntity.getPlayer();
        int selectedUnitAttackStyle = getAttackStyle(selectedUnit.getSubType());

        if (selectedUnitAttackStyle == RANGED
                && player.getPlayerID() == activePlayerID
                && hoverEntityType == BUILDING
                && hoverEntity.getSubType() == CASTLE_TURRET
                && ((CastleTurret) hoverEntity).getOccupyingUnit() == null) {
            clickAction = CA_DEFEND;
            return;
        }

        if (player.getPlayerID() != activePlayerID)
            clickAction = (selectedUnitAttackStyle == MELEE) ? CA_ATTACK_MELEE : CA_ATTACK_RANGED;
    }


    public void saveGame() {
        game.getSaveFileHandler().saveGame(this);
    }

    public Entity getEntityAtCoordinate(int x, int y) {
        Building b = buildingHandler.getBuildingAt(x, y);
        if (b != null)
            return b;

        Unit u = unitHandler.getUnitAtCoord(x, y);
        if (u != null)
            return u;

        return resourceObjectData[toTileY(y)][toTileX(x)];
    }

    public Entity getEntityAtTile(int tileX, int tileY) {
        Building b = buildingHandler.getBuildingAt(toPixelX(tileX), toPixelY(tileY));
        if (b != null)
            return b;

        Unit u = unitHandler.getUnitAtTile(tileX, tileY);
        if (u != null)
            return u;

        return resourceObjectData[tileY][tileX];
    }

    public boolean isTileBlockedOrReserved(int tileX, int tileY, Unit excludedUnit) {
        return getEntityAtTile(tileX, tileY) != null || unitHandler.isTileReserved(tileX, tileY, excludedUnit);
    }

    private boolean canBuildHere(int x, int y, boolean checkAllBuildingTiles) {
        if (selectedBuildingType == CASTLE_TURRET) {
            Building b = buildingHandler.getBuildingAt(gameX, gameY);
            return b != null && b.getSubType() == CASTLE_WALL;
        }

        int tileX = toTileX(x);
        int tileY = toTileY(y);
        ArrayList<Point> tiles;
        if (checkAllBuildingTiles)
            tiles = getBuildingTiles(x, y);
        else {
            tiles = new ArrayList<>();
            tiles.add(new Point(tileX, tileY));
        }

        for (Point p : tiles)
            if (isRestrictedBuilding(p) || isTileBlockedOrReserved(p.x, p.y, null))
                return false;

        return true;
    }

    private boolean isRestrictedBuilding(Point p) {
        return (selectedBuildingType == THRONE_ROOM || selectedBuildingType == CASTLE_WALL)
                && !map.getCastleZones().get(0).contains(p);
    }

    private ArrayList<Point> getBuildingTiles(int x, int y) {
        int width = getBuildingTileWidth(selectedBuildingType);
        int height = getBuildingTileHeight(selectedBuildingType);
        int maxX = map.getTileData()[0].length - width;
        int maxY = map.getTileData().length - height;

        int tileX = Math.min(toTileX(x), maxX);
        int tileY = Math.min(toTileY(y), maxY);

        ArrayList<Point> tiles = new ArrayList<>();
        for (int currY = tileY; currY < tileY + height; currY++) {
            for (int currX = tileX; currX < tileX + width; currX++) {
                tiles.add(new Point(currX, currY));
            }
        }
        return tiles;
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
        if (player == null) return;

        int maxX = toPixelX(map.getTileData()[0].length - getBuildingTileWidth(selectedBuildingType));
        int maxY = toPixelY(map.getTileData().length - getBuildingTileHeight(selectedBuildingType));

        int tileX = Math.min(gameX, maxX);
        int tileY = Math.min(gameY, maxY);

        buildingHandler.createBuilding(player, tileX, tileY, selectedBuildingType);
    }

    public Player getPlayerByID(long playerID) {
        for (Player p : players)
            if (p.getPlayerID() == playerID)
                return p;
        return null;
    }

    public void spawnUnit(int unitType) {
        if (selectedEntity.getEntityType() != BUILDING)
            return;

        Player activePlayer = getPlayerByID(activePlayerID);
        if (activePlayer != null) {
            if (activePlayer.getPopulation() >= activePlayer.getMaxPopulation())
                System.out.println("Not enough villages!");

            Point spawnPoint = buildingHandler.getSpawnTile((Building) selectedEntity);

            if (spawnPoint == null) {
                System.out.println("No spawnable tiles!");
                return;
            }

            unitHandler.createUnit(activePlayer, spawnPoint, unitType);
        }
    }

    private void setSelectedUnitTargetToHoverEntity(Unit selectedUnit) {
        int actionRange = (hoverEntity instanceof CastleTurret ? 1 : selectedUnit.getActionRange());

        boolean isInRangeAndReachable = selectedUnit.isTargetInRange(hoverEntity, actionRange) && selectedUnit.isLineOfSightOpen(hoverEntity);
        ArrayList<Point> path = null;
        if (!isInRangeAndReachable) {
            path = getUnitPathToNearestAdjacentTile(selectedUnit, tileX, tileY, this);
            if (path != null)
                selectedUnit.setPath(path);
        }
        if (isInRangeAndReachable || path != null)
            selectedUnit.setTargetEntity(hoverEntity);
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
        actionBar.mouseReleased(x, y, button);
        gameStatBar.mouseReleased(x, y, button);

        if (button == MouseEvent.BUTTON1) {
            if (showBuildingSelection && buildingSelection != null)
                buildingSelection.mouseReleased(x, y, button);
            else if (!showBuildingSelection) {
                super.mouseReleased(x, y, button);
                if (inGameArea && !paused) {

                    if (selectedBuildingType != -1 && canBuild && canAffordBuilding(selectedBuildingType)) {
                        buildBuilding();
                        selectedBuildingType = -1;
                    }

                    if (clickAction == CA_SELECT) {
                        setSelectedEntity(hoverEntity);
                        clickAction = -1;
                        selectedBuildingType = -1;
                    } else if (clickAction != -1) {
                        Unit selectedUnit = (Unit) selectedEntity;
                        if (clickAction == CA_MOVE) {
                            ArrayList<Point> path = getUnitPathToTile(selectedUnit, tileX, tileY, this);
                            if (path != null && !path.isEmpty()) {
                                selectedUnit.setPath(path);
                                selectedUnit.setTargetEntity(null);
                            }
                        } else if (canAttackOnClick() || canDefendOnClick() || canFarmOnClick() || canGatherOnClick())
                            setSelectedUnitTargetToHoverEntity(selectedUnit);
                        else if (hoverEntity.getEntityType() == BUILDING && clickAction == CA_EMPTY_INVENTORY) {
                            if (((Laborer) selectedUnit).canDepositResources((Building) hoverEntity))
                                setSelectedUnitTargetToHoverEntity(selectedUnit);
                            else
                                System.out.println("Nothing to deposit, or not enough space!");
                        }

                    }
                }
            }
        }
        leftMouseDown = false;
        rightMouseDown = false;
    }

    private boolean canAttackOnClick() {
        int hoverEntityType = hoverEntity.getEntityType();
        return (hoverEntityType == UNIT || hoverEntityType == BUILDING) && (clickAction == CA_ATTACK_MELEE || clickAction == CA_ATTACK_RANGED);
    }

    private boolean canDefendOnClick() {
        return (hoverEntity.getEntityType() == BUILDING && hoverEntity.getSubType() == CASTLE_TURRET && clickAction == CA_DEFEND);
    }

    private boolean canFarmOnClick() {
        return hoverEntity.getEntityType() == BUILDING && (hoverEntity.getSubType() == FARM || hoverEntity.getSubType() == FARM_ROTATED)
                && clickAction == CA_FARM && ((Farm) hoverEntity).getFarmers().size() < MAX_FARMERS;
    }

    private boolean canGatherOnClick() {
        return hoverEntity.getEntityType() == RESOURCE && (clickAction == CA_CHOP || clickAction == CA_MINE)
                && !((Laborer) selectedEntity).isInventoryFull(hoverEntity.getSubType());
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (showBuildingSelection && buildingSelection != null) return;

        super.mouseDragged(x, y);

        if (inGameArea) {
            if ((selectedBuildingType == -1 && selectedEntity == null || rightMouseDown && !leftMouseDown) && !isMouseDownInMiniMap)
                dragScreen(x, y);

            if (toTileX(mouseDownX) != toTileX(x) || toTileY(mouseDownY) != toTileY(y))
                clickAction = -1;
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        actionBar.mouseMoved(x, y);
        gameStatBar.mouseMoved(x, y);

        if (showBuildingSelection && buildingSelection != null)
            buildingSelection.mouseMoved(x, y);
        else if (!showBuildingSelection) {
            super.mouseMoved(x, y);
            if (inGameArea) {
                if (selectedBuildingType == -1) {
                    hoverEntity = getEntityAtCoordinate(x + mapXOffset, y + mapYOffset);
                    determineAction();
                } else
                    canBuild = canBuildHere(gameX, gameY, true);
            }
        }
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
            setSelectedEntity(null);
            selectedBuildingType = -1;
        }
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public long getActivePlayerID() {
        return activePlayerID;
    }

    public BuildingHandler getBuildingHandler() {
        return buildingHandler;
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

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ProjectileHandler getProjectileHandler() {
        return projectileHandler;
    }

    public ResourceObject[][] getResourceObjectData() {
        return resourceObjectData;
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

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Entity selectedEntity) {
        this.selectedEntity = selectedEntity;
        actionBar.setSelectedEntity(selectedEntity);
    }

    public boolean isShowBuildingSelection() {
        return showBuildingSelection;
    }

    public void setShowBuildingSelection(boolean showBuildingSelection) {
        this.showBuildingSelection = showBuildingSelection;
    }

    public UnitHandler getUnitHandler() {
        return unitHandler;
    }
}
