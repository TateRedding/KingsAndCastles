package gamestates;

import main.Game;
import objects.Map;
import ui.DropDownMenu;
import ui.buttons.Button;
import ui.overlays.DeleteConfirm;
import utils.LoadSave;
import utils.RenderText;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static main.Game.SCREEN_HEIGHT;

public abstract class MapSelect extends FileSelect {

    protected String[][] selectedMapData;

    public MapSelect(Game game) {
        super(game);
        initDropDownMenu();
    }

    public void initDropDownMenu() {
        ArrayList<Map> maps = game.getSaveFileHandler().getMaps();
        String[] options = new String[maps.size()];
        for (int i = 0; i < maps.size(); i++)
            options[i] = maps.get(i).getName();
        fileList = new DropDownMenu("Select Map", options, 5, dropDownX, dropDownYOffset);
    }

    @Override
    public void render(Graphics g) {
        if (selectedFile != null)
            renderSelectedMapData(g);

        super.render(g);
    }

    protected void renderSelectedMapData(Graphics g) {
        g.setColor(new Color(0, 0, 0, 75));
        g.fillRect(previewX, previewY, previewWidth, previewHeight);

        BufferedImage previewImage = null;
        String path = LoadSave.mapPath + File.separator + selectedFile.getName() + LoadSave.previewImageSuffix;
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
        if (selectedFile instanceof Map) {
            LoadSave.deleteMapFile((Map) selectedFile);
            for (int i = 0; i < game.getSaveFileHandler().getMaps().size(); i++) {
                Map currMap = game.getSaveFileHandler().getMaps().get(i);
                if (currMap.getName().equals(selectedFile.getName())) {
                    game.getSaveFileHandler().getMaps().remove(i);
                    break;
                }
            }
            selectedFile = null;
            selectedMapData = null;
            deleteConfirm.resetChoice();
            initDropDownMenu();
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (fileList.getBounds().contains(x, y)) {
            int selectedIndex = fileList.getSelectedIndex();
            if (selectedIndex != -1) {
                selectedFile = game.getSaveFileHandler().getMaps().get(selectedIndex);
                selectedMapData = getFileData();
            } else {
                selectedFile = null;
                fileList.resetIndicies();
            }
        } else if (deleteConfirm.getBounds().contains(x, y)) {
            Button exButton = deleteConfirm.getExButton();
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                deleting = false;
                exButton.reset(x, y);
            } else {
                int choice = deleteConfirm.getChoice();
                if (choice != -1) {
                    deleting = false;
                    if (choice == DeleteConfirm.YES)
                        deleteSelectedMap();
                }
            }
        }
        deleteConfirm.resetChoice();
    }
}