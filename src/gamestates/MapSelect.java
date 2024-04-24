package gamestates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import main.Game;
import objects.Map;
import objects.Tile;
import ui.DropDownMenu;
import ui.buttons.Button;
import ui.buttons.ExButton;
import ui.buttons.TextButton;
import ui.overlays.DeleteConfirm;
import utils.ImageLoader;
import utils.LoadSave;
import utils.RenderText;

import javax.imageio.ImageIO;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.*;

public abstract class MapSelect extends State implements StateMethods {

    protected DropDownMenu dropDownMenu;
    protected Map selectedMap;
    protected DeleteConfirm deleteConfirm;
    protected String[][] selectedMapData;
    protected TextButton start, delete;


    protected int dropDownMenuYOffset = 48;
    private int previewScale = 2;
    private int previewWidth = Map.MAX_WIDTH * previewScale;
    private int previewX = (SCREEN_WIDTH - previewWidth) / 2;
    private int previewHeight = Map.MAX_HEIGHT * previewScale;
    private int previewY = dropDownMenuYOffset * 2 + ImageLoader.dropDownTop.getHeight();
    protected boolean deleting;

    public MapSelect(Game game) {
        super(game);
        initDropDownMenu();
        initButtons();

        int dcX = (SCREEN_WIDTH - ImageLoader.overlayBg.getWidth()) / 2;
        int dcY = (SCREEN_HEIGHT - ImageLoader.overlayBg.getHeight()) / 2;
        deleteConfirm = new DeleteConfirm("", dcX, dcY);
    }

    public void initDropDownMenu() {
        ArrayList<Map> maps = game.getMapHandler().getMaps();
        String[] options = new String[maps.size()];
        for (int i = 0; i < maps.size(); i++)
            options[i] = maps.get(i).getName();
        int ddX = (Game.SCREEN_WIDTH - ImageLoader.dropDownTop.getWidth()) / 2;
        dropDownMenu = new DropDownMenu("Select Saved Map", options, 5, ddX, dropDownMenuYOffset);
    }

    protected void initButtons() {
        int previewXEnd = previewX + previewWidth;
        int startX = previewXEnd + ((Game.SCREEN_WIDTH - previewXEnd) - getButtonWidth(TEXT_LARGE)) / 2;
        int startY = (Game.SCREEN_HEIGHT - Button.getButtonHeight(TEXT_LARGE)) / 2;
        start = new TextButton(TEXT_LARGE, "Start", 46f, startX, startY);

        int deleteX = previewXEnd + ((Game.SCREEN_WIDTH - previewXEnd) - getButtonWidth(TEXT_SMALL)) / 2;
        int deleteY = previewY + previewHeight - getButtonHeight(TEXT_SMALL);
        delete = new TextButton(TEXT_SMALL, "Delete", 24f, deleteX, deleteY);
    }


    @Override
    public void update() {
        dropDownMenu.update();
        if (selectedMap != null) {
            start.update();
            delete.update();
            if (deleting)
                deleteConfirm.update();
        }
    }

    @Override
    public void render(Graphics g) {
        if (selectedMap != null) {
            renderSelectedMapData(g);
            start.render(g);
            delete.render(g);
            if (deleting)
                deleteConfirm.render(g);
        }
        dropDownMenu.render(g);
    }

    protected void renderSelectedMapData(Graphics g) {
        g.setColor(new Color(0, 0, 0, 75));
        g.fillRect(previewX, previewY, previewWidth, previewHeight);

        BufferedImage previewImage = null;
        String path = LoadSave.mapPath + File.separator + selectedMap.getName() + LoadSave.previewImageSuffix;
        File previewImageFile = new File(path);
        if (previewImageFile.exists()) {
            try {
                previewImage = ImageIO.read(previewImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (previewImage != null) {
            int previewImageWidth = previewImage.getWidth() * previewScale;
            int previewImageHeight = previewImage.getHeight() * previewScale;
            int previewImageX = previewX + (previewWidth - previewImageWidth) / 2;
            int previewImageY = previewY + (previewHeight - previewImageHeight) / 2;
            g.drawImage(previewImage, previewImageX, previewImageY, previewImageWidth, previewImageHeight, null);
        } else {
            String[] text = {"No preview", "available"};
            g.setColor(Color.BLACK);
            g.setFont(Game.getGameFont(74f));
            RenderText.renderText(g, text, RenderText.CENTER, RenderText.CENTER, previewX, previewY, previewWidth, previewHeight);
        }

        if (selectedMapData != null) {
            int yOffset = 48;
            int yStart = previewY + previewHeight + yOffset;
            int areaHeight = (SCREEN_HEIGHT - yStart - yOffset);
            Font leftFont = Game.getGameFont(46f);
            Font rightFont = Game.getGameFont(36f);
            g.setColor(Color.BLACK);
            RenderText.renderTextBoxed(g, selectedMapData, leftFont, rightFont, previewX, yStart, previewWidth,
                    areaHeight);
        }
    }

    private void deleteSelectedMap() {
        LoadSave.deleteMapFile(selectedMap);
        for (int i = 0; i < game.getMapHandler().getMaps().size(); i++) {
            Map currMap = game.getMapHandler().getMaps().get(i);
            if (currMap.getName().equals(selectedMap.getName())) {
                game.getMapHandler().getMaps().remove(i);
                break;
            }
        }
        reset();
    }

    public String[][] getMapData() {
        if (selectedMap == null)
            return null;
        Tile[][] tileData = selectedMap.getTileData();
        return new String[][]{
                {"Map Size", tileData[0].length + " x " + tileData[1].length},
                {"Water Tiles", "Calculate Me"}, {"Gold Mines", "" + selectedMap.getGoldMinePoints().size()},
                {"Castle Zones", selectedMap.getCastleZones().get(0).size() + " Tiles"}
        };
    }

    protected void reset() {
        selectedMap = null;
        selectedMapData = null;
        deleting = false;
        deleteConfirm.setDeleteName("");
        initDropDownMenu();
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        if (dropDownMenu.getBounds().contains(x, y))
            dropDownMenu.mousePressed(x, y, button);
        if (selectedMap != null && button == MouseEvent.BUTTON1)
            if (start.getBounds().contains(x, y))
                start.setMousePressed(true);
            else if (delete.getBounds().contains(x, y))
                delete.setMousePressed(true);
        if (deleting && deleteConfirm.getBounds().contains(x, y))
            deleteConfirm.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        dropDownMenu.mouseReleased(x, y, button);
        if (deleting)
            deleteConfirm.mouseReleased(x, y, button);
        if (dropDownMenu.getBounds().contains(x, y)) {
            int selectedIndex = dropDownMenu.getSelectedIndex();
            if (selectedIndex != -1) {
                selectedMap = game.getMapHandler().getMaps().get(selectedIndex);
                selectedMapData = getMapData();
            } else {
                selectedMap = null;
                dropDownMenu.resetIndicies();
            }
        }
        if (deleting && deleteConfirm.getBounds().contains(x, y)) {
            ExButton exButton = deleteConfirm.getExButton();
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                deleting = false;
                exButton.reset(x, y);
            } else {
                deleteConfirm.mouseReleased(x, y, button);
                int choice = deleteConfirm.getChoice();
                if (choice != -1) {
                    deleting = false;
                    if (choice == DeleteConfirm.YES)
                        deleteSelectedMap();
                }
            }
        }
        if (selectedMap != null && button == MouseEvent.BUTTON1)
            if (delete.getBounds().contains(x, y) && delete.isMousePressed()) {
                deleting = true;
                deleteConfirm.setDeleteName(selectedMap.getName());
            }
        delete.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        start.setMouseOver(false);
        delete.setMouseOver(false);
        dropDownMenu.mouseMoved(x, y);
        deleteConfirm.mouseMoved(x, y);
        if (selectedMap != null)
            if (start.getBounds().contains(x, y))
                start.setMouseOver(true);
            else if (delete.getBounds().contains(x, y))
                delete.setMouseOver(true);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        dropDownMenu.mouseWheelMoved(e);
    }

}
