package gamestates;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.DropDownMenu;
import ui.buttons.Button;
import ui.buttons.TextButton;
import ui.overlays.DeleteConfirm;
import ui.overlays.Overlay;
import utils.Savable;

import java.awt.*;
import java.awt.event.MouseEvent;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.*;

public abstract class FileSelect extends State {

    protected TextButton menu, start, delete;
    protected DropDownMenu fileList;
    protected DeleteConfirm deleteConfirm;
    protected Savable selectedFile;

    protected int dropDownYOffset = 48;
    protected int dropDownX = (Game.SCREEN_WIDTH - DropDownMenu.DD_WIDTH) / 2;
    protected int previewScale = 2;
    protected int previewWidth = Map.MAX_WIDTH * previewScale;
    protected int previewX = (SCREEN_WIDTH - previewWidth) / 2;
    protected int previewHeight = Map.MAX_HEIGHT * previewScale;
    protected int previewY = dropDownYOffset * 2 + DropDownMenu.DD_TOP_HEIGHT;
    protected boolean deleting;

    public FileSelect(Game game) {
        super(game);
        initButtons();

        int dcX = (SCREEN_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_SMALL)) / 2;
        int dcY = (SCREEN_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_SMALL)) / 2;
        deleteConfirm = new DeleteConfirm("", dcX, dcY);
    }

    protected void initButtons() {
        int offset = 48;
        menu = new TextButton(TEXT_SMALL_SHORT, offset, offset, 28f, "Menu");

        int previewXEnd = previewX + previewWidth;
        int startX = previewXEnd + ((Game.SCREEN_WIDTH - previewXEnd) - getButtonWidth(TEXT_LARGE)) / 2;
        int startY = (Game.SCREEN_HEIGHT - Button.getButtonHeight(TEXT_LARGE)) / 2;
        start = new TextButton(TEXT_LARGE, startX, startY, 46f, "Start");

        int deleteX = previewXEnd + ((Game.SCREEN_WIDTH - previewXEnd) - getButtonWidth(TEXT_SMALL_SHORT)) / 2;
        int deleteY = previewY + previewHeight - getButtonHeight(TEXT_SMALL_SHORT);
        delete = new TextButton(TEXT_SMALL_SHORT, deleteX, deleteY, 24f, "Delete");
    }

    @Override
    public void update() {
        menu.update();
        fileList.update();
        if (selectedFile != null) {
            start.update();
            delete.update();
            if (deleting)
                deleteConfirm.update();
        }
    }

    @Override
    public void render(Graphics g) {
        menu.render(g);
        fileList.render(g);
        if (selectedFile != null) {
            start.render(g);
            delete.render(g);
            if (deleting)
                deleteConfirm.render(g);
        }
    }

    public String[][] getFileData() {
        if (selectedFile instanceof Map selectedMap) {
            Tile[][] tileData = selectedMap.getTileData();
            return new String[][]{
                    {"Map Size", tileData[0].length + " x " + tileData[1].length},
                    {"Water Tiles", "Calculate Me"}, {"Gold Mines", "" + selectedMap.getGoldMinePoints().size()},
                    {"Castle Zones", selectedMap.getCastleZones().get(0).size() + " Tiles"}
            };
        } else if (selectedFile instanceof Play selectedGame) {
            return new String[][]{
                    {"Populate", "Me!"}
            };
        }
        return null;
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (fileList.getBounds().contains(x, y))
            fileList.mousePressed(x, y, button);
        if (deleting && deleteConfirm.getBounds().contains(x, y))
            deleteConfirm.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (menu.getBounds().contains(x, y))
                menu.setMousePressed(true);
            else if (selectedFile != null) {
                if (start.getBounds().contains(x, y))
                    start.setMousePressed(true);
                else if (delete.getBounds().contains(x, y))
                    delete.setMousePressed(true);
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        fileList.mouseReleased(x, y, button);
        if (deleting)
            deleteConfirm.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (menu.getBounds().contains(x, y) && menu.isMousePressed())
                GameStates.setGameState(GameStates.MENU);
            else if (selectedFile != null) {
                if (delete.getBounds().contains(x, y) && delete.isMousePressed()) {
                    deleting = true;
                    deleteConfirm.setDeleteName(selectedFile.getName());
                }
            }
        delete.reset(x, y);
        menu.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        fileList.mouseMoved(x, y);
        delete.setMouseOver(false);
        menu.setMouseOver(false);
        start.setMouseOver(false);
        if (deleting)
            deleteConfirm.mouseMoved(x, y);
        if (menu.getBounds().contains(x, y))
            menu.setMouseOver(true);
        else if (selectedFile != null) {
            if (start.getBounds().contains(x, y))
                start.setMouseOver(true);
            else if (delete.getBounds().contains(x, y))
                delete.setMouseOver(true);

        }
    }

    @Override
    public void mouseWheelMoved(int dir, int amt) {
        fileList.mouseWheelMoved(dir, amt);
    }

}
