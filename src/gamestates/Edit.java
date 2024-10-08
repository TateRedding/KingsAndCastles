package gamestates;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.bars.EditorBar;
import ui.bars.MapStatBar;
import utils.ImageLoader;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;

import static entities.buildings.Building.THRONE_ROOM;
import static entities.resources.ResourceObject.GOLD;
import static gamestates.Play.CA_SELECT;
import static main.Game.*;
import static objects.Tile.*;

public class Edit extends MapState {

    public static final int SQUARE = 0;
    public static final int CIRCLE = 1;

    public static final int CASTLE_ZONE = 5;
    public static final int GOLD_MINE_TILE = 6;
    public static final int THRONE_ROOM_TILE = 7;

    private EditorBar editorBar;
    private MapStatBar mapStatBar;
    private ArrayList<ArrayList<ArrayList<Point>>> brushPoints = new ArrayList<>();

    private int maxBrushSize = 10;
    private int brushSize = 1;
    private int brushShape = SQUARE;
    private int selectedPlayer = 0;
    private int selectedType = -1;
    private int lastTileX, lastTileY;

    public Edit(Game game, Map map) {
        super(game, map);
        this.editorBar = new EditorBar(this);
        this.mapStatBar = new MapStatBar(this);
        initBrushPoints();
    }

    private void initBrushPoints() {
        ArrayList<ArrayList<Point>> allSquarePoints = new ArrayList<>();
        ArrayList<ArrayList<Point>> allCirclePoints = new ArrayList<>();

        for (int size = 1; size <= maxBrushSize; size++) {
            ArrayList<Point> squareBrushPoints = new ArrayList<>();
            ArrayList<Point> circleBrushPoints = new ArrayList<>();
            int negDist = (size - 1) / 2 * -1;
            int startPos = negDist * TILE_SIZE;
            int diameter = size * TILE_SIZE;
            Rectangle squareBounds = new Rectangle(startPos, startPos, diameter, diameter);
            Ellipse2D circleBounds = new Ellipse2D.Float(startPos, startPos, diameter, diameter);

            int yMax = squareBounds.y + squareBounds.height;
            int xMax = squareBounds.x + squareBounds.width;
            for (int y = squareBounds.y; y < yMax; y += TILE_SIZE)
                for (int x = squareBounds.x; x < xMax; x += TILE_SIZE) {
                    int pY = (int) ((float) y / TILE_SIZE);
                    int pX = (int) ((float) x / TILE_SIZE);
                    squareBrushPoints.add(new Point(pX, pY));
                    if (circleBounds.contains(y + TILE_SIZE / 2, x + TILE_SIZE / 2))
                        circleBrushPoints.add(new Point(pX, pY));

                }
            allSquarePoints.add(squareBrushPoints);
            allCirclePoints.add(circleBrushPoints);
        }
        brushPoints.add(allSquarePoints);
        brushPoints.add(allCirclePoints);
    }

    @Override
    public void update() {
        super.update();
        editorBar.update();
        mapStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (inGameArea) {
            drawSelectedType(g);
            if (selectedType == CASTLE_ZONE || selectedType == THRONE_ROOM_TILE)
                drawPlayerIndicator(g);
        }

        drawGoldMines(g, mapXOffset, mapYOffset);
        drawThroneRooms(g, mapXOffset, mapYOffset);
        editorBar.render(g);
        mapStatBar.render(g);
        miniMap.render(g, mapXOffset, mapYOffset);
    }

    private void drawSelectedType(Graphics g) {
        if (selectedType >= 0)
            if (selectedType < CASTLE_ZONE)
                for (Point p : brushPoints.get(brushShape).get(brushSize - 1)) {
                    int x = mouseX + (p.x * TILE_SIZE);
                    int y = mouseY + (p.y * TILE_SIZE);
                    g.drawImage(ImageLoader.tiles.get(selectedType).get(ImageLoader.tiles.get(selectedType).size() - 1), x, y, null);
                }
            else
                g.drawImage(ImageLoader.editorBarButtonSprites.get(selectedType), mouseX, mouseY, null);
        g.drawImage(ImageLoader.actions[CA_SELECT], mouseX, mouseY, null);
    }

    private void drawPlayerIndicator(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(Game.getGameFont(24f));
        String indicator = "P" + (selectedPlayer + 1);
        int x = mouseX + (TILE_SIZE - g.getFontMetrics().stringWidth(indicator)) / 2;
        int y = mouseY - 2;
        g.drawString(indicator, x, y);
    }

    private void drawGoldMines(Graphics g, int mapXOffset, int mapYOffset) {
        for (Point p : map.getGoldMinePoints())
            g.drawImage(ImageLoader.resourceObjects[GOLD][0], toPixelX(p.x) - mapXOffset, toPixelY(p.y) - mapYOffset, null);
    }

    private void drawThroneRooms(Graphics g, int mapXOffset, int mapYOffset) {
        for (Point p : map.getThroneRoomPoints())
            if (p != null)
                g.drawImage(ImageLoader.buildings[THRONE_ROOM], toPixelX(p.x) - mapXOffset, toPixelY(p.y) - mapYOffset, null);
    }

    private void changeTile(int mouseEvent) {
        if (mouseEvent == MouseEvent.MOUSE_DRAGGED && lastTileX == tileX && lastTileY == tileY)
            return;
        lastTileX = tileX;
        lastTileY = tileY;
        updateTiles(selectedType, tileX, tileY);
    }

    private void updateTiles(int tileType, int tileX, int tileY) {
        ArrayList<Point> changedTiles = new ArrayList<>();
        for (Point p : brushPoints.get(brushShape).get(brushSize - 1)) {
            int currTileX = tileX + p.x;
            int currTileY = tileY + p.y;
            if (isTileInRange(currTileX, currTileY)) {
                Tile currTile = tileData[currTileY][currTileX];
                int prevTileType = currTile.getTileType();
                if (prevTileType == tileType || ((tileType == WATER_GRASS || tileType == WATER_SAND) && map.getGoldMinePoints().contains(new Point(currTileX, currTileY))))
                    continue;
                tileData[currTileY][currTileX] = new Tile(tileType, 0);
                map.getTileCounts()[prevTileType]--;
                map.getTileCounts()[tileType]++;
                changedTiles.add(new Point(currTileX, currTileY));
            }
        }

        ArrayList<Point> tilesToUpdate = new ArrayList<>();
        for (Point changedPoint : changedTiles) {
            if (!tilesToUpdate.contains(changedPoint) && tileType != GRASS)
                tilesToUpdate.add(new Point(changedPoint.x, changedPoint.y));

            ArrayList<Point> surroundingPoints = getSurroundingPoints(changedPoint.x, changedPoint.y);
            for (Point sp : surroundingPoints) {
                int spTileType = tileData[sp.y][sp.x].getTileType();
                if (!tilesToUpdate.contains(sp) && spTileType != GRASS)
                    tilesToUpdate.add(sp);

                if (tileType == WATER_SAND && (spTileType == GRASS || spTileType == DIRT) && !changedTiles.contains(sp)) {
                    tileData[sp.y][sp.x] = new Tile(SAND, 0);
                    map.getTileCounts()[spTileType]--;
                    map.getTileCounts()[SAND]++;
                    if (!tilesToUpdate.contains(sp))
                        tilesToUpdate.add(sp);
                    ArrayList<Point> sandSurroundingPoints = getSurroundingPoints(sp.x, sp.y);
                    for (Point sandSP : sandSurroundingPoints)
                        if (!tilesToUpdate.contains(sandSP) && tileData[sandSP.y][sandSP.x].getTileType() != GRASS)
                            tilesToUpdate.add(sandSP);
                }
            }
        }
        for (Point currentPoint : tilesToUpdate) {
            int bitmaskId = calculateBitmaskId(currentPoint);
            tileData[currentPoint.y][currentPoint.x].setBitmaskId(bitmaskId);
        }
    }

    private boolean isTileInRange(int tileX, int tileY) {
        return !(tileX < 0 || tileX >= tileData[0].length || tileY < 0 || tileY >= tileData.length);
    }

    private ArrayList<Point> getSurroundingPoints(int tileX, int tileY) {
        ArrayList<Point> points = new ArrayList<Point>();
        for (int y = tileY - 1; y < tileY + 2; y++)
            for (int x = tileX - 1; x < tileX + 2; x++) {
                if (x == tileX && y == tileY)
                    continue;
                if (isTileInRange(x, y))
                    points.add(new Point(x, y));
            }
        return points;
    }

    private int calculateBitmaskId(Point point) {
        StringBuilder binaryStringBuilder = new StringBuilder();
        ArrayList<Integer> acceptedTypes = new ArrayList<Integer>();
        int tileType = tileData[point.y][point.x].getTileType();
        if (tileType == SAND)
            acceptedTypes.addAll(Arrays.asList(SAND, WATER_SAND));
        else if (tileType == WATER_GRASS || tileType == WATER_SAND)
            acceptedTypes.addAll(Arrays.asList(WATER_GRASS, WATER_SAND));
        else
            acceptedTypes.add(tileType);

        for (int y = point.y - 1; y < point.y + 2; y++)
            for (int x = point.x - 1; x < point.x + 2; x++) {
                if (x == point.x && y == point.y)
                    continue;
                if (!isTileInRange(x, y)) {
                    binaryStringBuilder.insert(0, "0");
                    continue;
                }
                int currTileType = tileData[y][x].getTileType();
                if (acceptedTypes.contains(currTileType)) {
                    if (y != point.y && x != point.x) {
                        int xDiff = x - point.x;
                        int yDiff = y - point.y;
                        if (acceptedTypes.contains(tileData[point.y][point.x + xDiff].getTileType())
                                && acceptedTypes.contains(tileData[point.y + yDiff][point.x].getTileType()))
                            binaryStringBuilder.insert(0, "1");
                        else
                            binaryStringBuilder.insert(0, "0");
                    } else
                        binaryStringBuilder.insert(0, "1");
                } else
                    binaryStringBuilder.insert(0, "0");
            }
        return Integer.parseInt(binaryStringBuilder.toString(), 2);
    }

    private void setCastleZone() {
        Point currPoint = new Point(tileX, tileY);
        ArrayList<ArrayList<Point>> castleZones = map.getCastleZones();
        for (ArrayList<Point> cz : castleZones)
            if (cz.contains(currPoint))
                return;

        castleZones.get(selectedPlayer).add(currPoint);
        validateCastleZones();
    }

    private void unsetCastleZone() {
        ArrayList<Point> selectedZoneList = map.getCastleZones().get(selectedPlayer);
        for (int i = 0; i < selectedZoneList.size(); i++) {
            Point currPoint = selectedZoneList.get(i);
            if (tileX == currPoint.x && tileY == currPoint.y) {
                selectedZoneList.remove(i);
                validateCastleZones();
                return;
            }
        }
    }

    private void validateCastleZones() {
        ArrayList<ArrayList<Point>> zones = map.getCastleZones();
        boolean valid = true;
        int p1Size = zones.get(0).size();
        for (int i = 1; i < zones.size(); i++)
            if (p1Size != zones.get(1).size()) {
                valid = false;
                break;
            }
        editorBar.setShowCastleZoneWarning(!valid);
        editorBar.getSave().setDisabled(!valid);

    }

    private void placeGoldMine() {
        Tile currTile = tileData[tileY][tileX];
        int tileType = currTile.getTileType();
        if (tileType == WATER_GRASS || tileType == WATER_SAND)
            return;
        Point goldMinePoint = new Point(tileX, tileY);
        if (map.getGoldMinePoints().contains(goldMinePoint))
            return;
        map.getGoldMinePoints().add(goldMinePoint);
    }

    private void removeGoldMine() {
        Point goldMinePoint = new Point(tileX, tileY);
        if (map.getGoldMinePoints().contains(goldMinePoint))
            map.getGoldMinePoints().remove(new Point(tileX, tileY));
    }

    private void placeThroneRoom() {
        Tile currTile = tileData[tileY][tileX];
        int tileType = currTile.getTileType();
        if (tileType == WATER_GRASS || tileType == WATER_SAND)
            return;
        Point throneRoomPoint = new Point(tileX, tileY);
        if (map.getCastleZones().get(selectedPlayer).contains(throneRoomPoint))
            map.getThroneRoomPoints()[selectedPlayer] = throneRoomPoint;
    }

    private void removeThroneRoom() {
        Point throneRoomPoint = new Point(tileX, tileY);
        if (map.getThroneRoomPoints()[selectedPlayer].equals(throneRoomPoint))
            map.getThroneRoomPoints()[selectedPlayer] = null;
    }

    public void saveMap() {
        game.getSaveFileHandler().saveMap(map);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (editorBar.getBounds().contains(x, y))
            editorBar.mousePressed(x, y, button);
        else if (mapStatBar.getBounds().contains(x, y))
            mapStatBar.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        editorBar.mouseReleased(x, y, button);
        mapStatBar.mouseReleased(x, y, button);

        if (selectedType != -1 && inGameArea)
            if (leftMouseDown)
                switch (selectedType) {
                    case CASTLE_ZONE:
                        setCastleZone();
                        break;
                    case GOLD_MINE_TILE:
                        placeGoldMine();
                        break;
                    case THRONE_ROOM_TILE:
                        placeThroneRoom();
                        break;
                    default:
                        changeTile(MouseEvent.MOUSE_RELEASED);
                }
            else if (rightMouseDown)
                if (selectedType == CASTLE_ZONE)
                    unsetCastleZone();
                else if (selectedType == GOLD_MINE_TILE)
                    removeGoldMine();
                else if (selectedType == THRONE_ROOM_TILE)
                    removeThroneRoom();

        leftMouseDown = false;
        rightMouseDown = false;
    }

    @Override
    public void mouseDragged(int x, int y) {
        super.mouseDragged(x, y);

        if (inGameArea) {
            if (leftMouseDown) {
                if (selectedType == CASTLE_ZONE)
                    setCastleZone();
                else if (selectedType != -1 && selectedType != GOLD_MINE_TILE)
                    changeTile(MouseEvent.MOUSE_DRAGGED);
                else if (!isMouseDownInMiniMap)
                    dragScreen(x, y);
            } else if (rightMouseDown && (!isMouseDownInMiniMap || selectedType == CASTLE_ZONE))
                if (selectedType == CASTLE_ZONE)
                    unsetCastleZone();
                else
                    dragScreen(x, y);
        }
    }


    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        editorBar.mouseMoved(x, y);
        mapStatBar.mouseMoved(x, y);
    }

    @Override
    public void mouseWheelMoved(int dir, int amt) {
        super.mouseWheelMoved(dir, amt);
        if (dir == -1) {
            if (selectedType == CASTLE_ZONE || selectedType == THRONE_ROOM_TILE) {
                if (selectedPlayer < map.getNumPlayers() - 1)
                    selectedPlayer++;
            } else if (selectedType != GOLD_MINE_TILE && brushSize < maxBrushSize)
                brushSize++;
        } else {
            if (selectedType == CASTLE_ZONE || selectedType == THRONE_ROOM_TILE) {
                if (selectedPlayer > 0)
                    selectedPlayer--;
            } else if (selectedType != GOLD_MINE_TILE && brushSize > 1)
                brushSize--;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_DELETE)
            selectedType = -1;
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    public void setBrushShape(int brushShape) {
        this.brushShape = brushShape;
    }

    public int getMaxBrushSize() {
        return maxBrushSize;
    }

    public int getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(int type) {
        selectedType = type;
    }

}
