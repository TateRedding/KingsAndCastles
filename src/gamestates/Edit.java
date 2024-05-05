package gamestates;

import static main.Game.TILE_SIZE;
import static objects.Tile.GRASS;
import static objects.Tile.SAND;
import static objects.Tile.WATER_GRASS;
import static objects.Tile.WATER_SAND;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.bars.EditorBar;
import ui.bars.MapStatBar;
import utils.ImageLoader;

public class Edit extends MapState {

    private static final int CASTLE_ZONE = 5;
    private static final int GOLD_MINE = 6;

    private EditorBar editorBar;
    private MapStatBar mapStatBar;
    private int selectedZone = 0;
    private int selectedType = -1;
    private int lastTileX, lastTileY, lastTileType;
    private boolean leftMouseDown, rightMouseDown;

    public Edit(Game game, Map map) {
        super(game, map);
        this.editorBar = new EditorBar(this);
        this.mapStatBar = new MapStatBar(this);
    }

    @Override
    public void update() {
        editorBar.update();
        mapStatBar.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        editorBar.render(g);
        mapStatBar.render(g);
        miniMap.render(g, xTileOffset, yTileOffset);

        if (inGameArea) {
            drawSelectedType(g);
            if (selectedType == CASTLE_ZONE)
                drawPlayerIndicator(g);
        }
    }

    private void drawSelectedType(Graphics g) {
        if (selectedType != -1)
            g.drawImage(ImageLoader.editorBarButtonSprites.get(selectedType), mouseX, mouseY, null);
        g.drawImage(ImageLoader.select, mouseX, mouseY, null);
    }

    private void drawPlayerIndicator(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(Game.getGameFont(24f));
        String indicator = "P" + (selectedZone + 1);
        int x = mouseX + (TILE_SIZE - g.getFontMetrics().stringWidth(indicator)) / 2;
        int y = mouseY - 2;
        g.drawString(indicator, x, y);
    }

    private void changeTile() {
        if ((lastTileX == tileX && lastTileY == tileY && lastTileType == selectedType)
                || selectedType == tileData[tileY][tileX].getTileType())
            return;
        lastTileX = tileX;
        lastTileY = tileY;
        lastTileType = selectedType;

        updateTiles(selectedType, tileX, tileY);
    }

    private void updateTiles(int tileType, int tileX, int tileY) {
        int prevTileType = map.getTileData()[tileY][tileX].getTileType();
        map.getTileData()[tileY][tileX] = new Tile(tileType, 0);
        map.getTileCounts()[prevTileType]--;
        map.getTileCounts()[tileType]++;

        ArrayList<Point> points = new ArrayList<Point>();
        if (tileType != GRASS)
            points.add(new Point(tileX, tileY));

        ArrayList<Point> surroundingPoints = getSurroundingPoints(tileX, tileY);
        for (Point point : surroundingPoints)
            if (map.getTileData()[point.y][point.x].getTileType() != GRASS)
                points.add(point);

        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            int bitmaskId = calculateBitmaskId(currentPoint);
            tileData[currentPoint.y][currentPoint.x].setBitmaskId(bitmaskId);
        }
    }

    private ArrayList<Point> getSurroundingPoints(int tileX, int tileY) {
        ArrayList<Point> points = new ArrayList<Point>();
        for (int y = tileY - 1; y < tileY + 2; y++)
            for (int x = tileX - 1; x < tileX + 2; x++) {
                if (x == tileX && y == tileY)
                    continue;
                if (y >= 0 && y < map.getTileData().length && x >= 0 && x < map.getTileData()[0].length)
                    points.add(new Point(x, y));
            }
        return points;
    }

    private int calculateBitmaskId(Point point) {
        String binString = "";
        Tile[][] tileData = map.getTileData();
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
                if (y < 0 || y > tileData.length || x < 0 || x > tileData[0].length) {
                    binString = "0" + binString;
                    continue;
                }
                int currTileType = tileData[y][x].getTileType();
                if (tileType == WATER_SAND && currTileType == GRASS)
                    updateTiles(SAND, x, y);
                if (acceptedTypes.contains(currTileType)) {
                    if (y != point.y && x != point.x) {
                        int xDiff = x - point.x;
                        int yDiff = y - point.y;
                        if (acceptedTypes.contains(tileData[point.y][point.x + xDiff].getTileType())
                                && acceptedTypes.contains(tileData[point.y + yDiff][point.x].getTileType()))
                            binString = "1" + binString;
                        else
                            binString = "0" + binString;
                    } else
                        binString = "1" + binString;
                } else
                    binString = "0" + binString;
            }
        return Integer.parseInt(binString, 2);
    }

    private void setCastleZone() {
        Point currPoint = new Point(tileX, tileY);
        ArrayList<ArrayList<Point>> castleZones = map.getCastleZones();
        for (ArrayList<Point> cz : castleZones)
            if (cz.contains(currPoint))
                return;

        castleZones.get(selectedZone).add(currPoint);
        validateCastleZones();
    }

    private void unsetCastleZone() {
        ArrayList<Point> selectedZoneList = map.getCastleZones().get(selectedZone);
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
        for (Point gm : map.getGoldMinePoints())
            if (gm.x == tileX && gm.y == tileY)
                return;
        map.getGoldMinePoints().add(new Point(tileX, tileY));
    }

    private void removeGoldMine() {
        for (int i = 0; i < map.getGoldMinePoints().size(); i++) {
            Point currPoint = map.getGoldMinePoints().get(i);
            if (currPoint.x == tileX && currPoint.y == tileY) {
                map.getGoldMinePoints().remove(i);
                return;
            }
        }
    }

    public void saveMap() {
        map.recalculateResourceSpawnablePoints();
        game.getSaveFileHandler().saveMap(map);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (editorBar.getBounds().contains(x, y))
            editorBar.mousePressed(x, y, button);
        else if (mapStatBar.getBounds().contains(x, y))
            mapStatBar.mousePressed(x, y, button);

        if (button == MouseEvent.BUTTON1)
            leftMouseDown = true;
        else if (button == MouseEvent.BUTTON3)
            rightMouseDown = true;

    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        editorBar.mouseReleased(x, y, button);
        if (mapStatBar.getBounds().contains(x, y))
            mapStatBar.mouseReleased(x, y, button);
        else if (selectedType != -1 && inGameArea)
            if (leftMouseDown)
                switch (selectedType) {
                    case CASTLE_ZONE:
                        setCastleZone();
                        break;
                    case GOLD_MINE:
                        placeGoldMine();
                        break;
                    default:
                        changeTile();
                }
            else if (rightMouseDown)
                if (selectedType == CASTLE_ZONE)
                    unsetCastleZone();
                else if (selectedType == GOLD_MINE)
                    removeGoldMine();

        leftMouseDown = false;
        rightMouseDown = false;
    }

    @Override
    public void mouseDragged(int x, int y) {
        super.mouseDragged(x, y);
        if (inGameArea)
            if (selectedType != -1)
                switch (selectedType) {
                    case CASTLE_ZONE:
                        if (leftMouseDown)
                            setCastleZone();
                        else if (rightMouseDown)
                            unsetCastleZone();
                        break;
                    case GOLD_MINE:
                        // place gold mine, or maybe not for dragging and just break here
                        break;
                    default:
                        changeTile();
                }
            else
                dragScreen(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        if (editorBar.getBounds().contains(x, y))
            editorBar.mouseMoved(x, y);
        else if (mapStatBar.getBounds().contains(x, y))
            mapStatBar.mouseMoved(x, y);
    }

    @Override
    public void mouseWheelMoved(int dir, int amt) {
        super.mouseWheelMoved(dir, amt);
        if (dir == -1) {
            if (selectedZone < map.getNumPlayers() - 1) {
                selectedZone++;
            }
        } else if (selectedZone > 0)
            selectedZone--;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_DELETE)
            selectedType = -1;
    }

    public void setSelectedType(int type) {
        selectedType = type;
    }

}
