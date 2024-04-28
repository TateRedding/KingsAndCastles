package gamestates;

import main.Game;
import objects.Map;
import ui.DropDownMenu;
import ui.buttons.ExButton;
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

public class LoadGame extends FileSelect {

    private String[][] selectedGameData;

    public LoadGame(Game game) {
        super(game);
        initDropDownMenu();
    }

    public void initDropDownMenu() {
        ArrayList<Play> games = game.getSaveFileHandler().getGames();
        String[] options = new String[games.size()];
        for (int i = 0; i < games.size(); i++)
            options[i] = games.get(i).getName();
        fileList = new DropDownMenu("Select Game", options, 5, dropDownX, dropDownYOffset);
    }

    @Override
    public void render(Graphics g) {
        if (selectedFile != null)
            renderSelectedGameData(g);

        super.render(g);
    }

    protected void renderSelectedGameData(Graphics g) {
        g.setColor(new Color(0, 0, 0, 75));
        g.fillRect(previewX, previewY, previewWidth, previewHeight);

        BufferedImage previewImage = null;
        // Can get the map image and paint over the data from the Play object
        /*
        String path = LoadSave.mapPath + File.separator + selectedFile.getName() + LoadSave.previewImageSuffix;
        File previewImageFile = new File(path);
        if (previewImageFile.exists()) {
            try {
                previewImage = ImageIO.read(previewImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
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

        if (selectedGameData != null) {
            int yOffset = 48;
            int yStart = previewY + previewHeight + yOffset;
            int areaHeight = (SCREEN_HEIGHT - yStart - yOffset);
            Font leftFont = Game.getGameFont(46f);
            Font rightFont = Game.getGameFont(36f);
            g.setColor(Color.BLACK);
            RenderText.renderTextBoxed(g, selectedGameData, leftFont, rightFont, previewX, yStart, previewWidth,
                    areaHeight);
        }
    }

    private void deleteSelectedGame() {
        if (selectedFile instanceof Play) {
            LoadSave.deleteGameFile((Play) selectedFile);
            for (int i = 0; i < game.getSaveFileHandler().getGames().size(); i++) {
                Play currGame = game.getSaveFileHandler().getGames().get(i);
                if (currGame.getName().equals(selectedFile.getName())) {
                    game.getSaveFileHandler().getGames().remove(i);
                    break;
                }
            }
            selectedFile = null;
            selectedGameData = null;
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
                selectedFile = game.getSaveFileHandler().getGames().get(selectedIndex);
                selectedGameData = getFileData();
            } else {
                selectedFile = null;
                fileList.resetIndicies();
            }
        } else if (deleteConfirm.getBounds().contains(x, y)) {
            ExButton exButton = deleteConfirm.getExButton();
            if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                deleting = false;
                exButton.reset(x, y);
            } else {
                int choice = deleteConfirm.getChoice();
                if (choice != -1) {
                    deleting = false;
                    if (choice == DeleteConfirm.YES)
                        deleteSelectedGame();
                }
            }
        } else if (start.getBounds().contains(x, y) && start.isMousePressed())
            if (selectedFile instanceof Play selectedGame)
                game.startGame(selectedGame);
        start.reset(x, y);
        deleteConfirm.resetChoice();
    }
}