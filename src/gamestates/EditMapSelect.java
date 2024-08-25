package gamestates;

import main.Game;
import objects.Map;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import ui.buttons.TextButton;
import ui.overlays.NewMapForm;
import ui.overlays.Overlay;
import utils.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.*;

public class EditMapSelect extends MapSelect {

    private NewMapForm newMapForm;
    private TextButton newMapButton;

    private boolean showNewMapForm;

    public EditMapSelect(Game game) {
        super(game);

        int formX = (Game.SCREEN_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_SMALL)) / 2;
        int formY = (Game.SCREEN_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_SMALL)) / 2;
        newMapForm = new NewMapForm(formX, formY);

        int buttonX = (SCREEN_WIDTH - getButtonWidth(TEXT_LARGE)) / 2;
        int buttonY = (SCREEN_HEIGHT - getButtonHeight(TEXT_LARGE)) / 2;
        newMapButton = new TextButton(TEXT_LARGE, buttonX, buttonY, 46f, "New Map");
    }

    @Override
    public void update() {
        super.update();
        if (showNewMapForm) {
            newMapForm.update();
            start.setDisabled(!newMapForm.isValid());
        } else {
            start.setDisabled(false);
            if (selectedFile == null)
                newMapButton.update();
        }

        if (showNewMapForm)
            start.update();
    }

    @Override
    public void render(Graphics g) {
        if (showNewMapForm)
            newMapForm.render(g);
        else if (selectedFile == null)
            newMapButton.render(g);

        super.render(g);

        if (showNewMapForm)
            start.render(g);
    }

    private void startNewMap() {
        String name = newMapForm.getMapName();
        if (name.isEmpty())
            return;

        int tileHeight = newMapForm.getMapHeight();
        if (tileHeight < Map.MIN_HEIGHT)
            tileHeight = Map.MIN_HEIGHT;
        else if (tileHeight > Map.MAX_HEIGHT)
            tileHeight = Map.MAX_HEIGHT;

        int tileWidth = newMapForm.getMapWidth();
        if (tileWidth < Map.MIN_WIDTH)
            tileWidth = Map.MIN_WIDTH;
        else if (tileWidth > Map.MAX_WIDTH)
            tileWidth = Map.MAX_WIDTH;

        Map newMap = new Map(name, tileWidth, tileHeight);
        LoadSave.saveMap(newMap);
        game.getSaveFileHandler().getMaps().add(0, newMap);
        game.editMap(newMap);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (showNewMapForm) {
            if (newMapForm.getBounds().contains(x, y))
                newMapForm.mousePressed(x, y, button);
        } else if (selectedFile == null)
            if (button == MouseEvent.BUTTON1 && newMapButton.getBounds().contains(x, y))
                newMapButton.setMousePressed(true);

        if (showNewMapForm && button == MouseEvent.BUTTON1 && start.getBounds().contains(x, y))
            start.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (fileList.isExpanded())
            showNewMapForm = false;
        if (showNewMapForm) {
            if (newMapForm.getBounds().contains(x, y)) {
                ImageButton exButton = newMapForm.getExButton();
                if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                    showNewMapForm = false;
                    exButton.reset(x, y);
                } else {
                    newMapForm.mouseReleased(x, y, button);
                }
            }
        } else if (selectedFile == null)
            if (newMapButton.getBounds().contains(x, y) && newMapButton.isMousePressed()) {
                showNewMapForm = true;
                newMapForm.resetTextBoxes();
                fileList.setExpanded(false);
            }

        if (start.getBounds().contains(x, y) && start.isMousePressed())
            if (showNewMapForm && selectedFile == null)
                startNewMap();
            else if (!showNewMapForm && selectedFile != null) {
                if (selectedFile instanceof Map selectedMap)
                    game.editMap(selectedMap);
            }

        newMapButton.reset(x, y);
        start.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        newMapButton.setMouseOver(false);
        if (showNewMapForm) {
            if (newMapForm.getBounds().contains(x, y))
                newMapForm.mouseMoved(x, y);
        } else if (selectedFile == null)
            if (newMapButton.getBounds().contains(x, y))
                newMapButton.setMouseOver(true);

        if (showNewMapForm && start.getBounds().contains(x, y))
            start.setMouseOver(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (showNewMapForm)
            newMapForm.keyPressed(e);
    }
}