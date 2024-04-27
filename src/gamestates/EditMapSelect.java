package gamestates;

import static main.Game.SCREEN_HEIGHT;
import static main.Game.SCREEN_WIDTH;
import static ui.buttons.Button.TEXT_LARGE;
import static ui.buttons.Button.getButtonHeight;
import static ui.buttons.Button.getButtonWidth;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.Game;
import objects.Map;
import ui.overlays.NewMapForm;
import ui.buttons.ExButton;
import ui.buttons.TextButton;
import ui.overlays.Overlay;

public class EditMapSelect extends MapSelect implements StateMethods {

    private NewMapForm newMapForm;
    private TextButton newMapButton;

    private boolean showNewMapForm;

    public EditMapSelect(Game game) {
        super(game);

        int formX = (Game.SCREEN_WIDTH - Overlay.OVERLAY_WIDTH) / 2;
        int formY = (Game.SCREEN_HEIGHT - Overlay.OVERLAY_HEIGHT) / 2;
        newMapForm = new NewMapForm(formX, formY);

        int xStart = (SCREEN_WIDTH - getButtonWidth(TEXT_LARGE)) / 2;
        int yStart = (SCREEN_HEIGHT - getButtonHeight(TEXT_LARGE)) / 2;
        newMapButton = new TextButton(TEXT_LARGE, "New Map", 46f, xStart, yStart);
    }

    @Override
    public void update() {
        super.update();
        if (showNewMapForm) {
            newMapForm.update();
            start.setDisabled(!newMapForm.isValid());
        } else {
            start.setDisabled(false);
            if (selectedMap == null)
                newMapButton.update();
        }

        if (showNewMapForm)
            start.update();
    }

    @Override
    public void render(Graphics g) {
        if (showNewMapForm)
            newMapForm.render(g);
        else if (selectedMap == null)
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
        startMapEditor(newMap);
    }

    private void startMapEditor(Map map) {
        game.editMap(map);
        game.getSaveFileHandler().getMaps().add(0, map);
        GameStates.setGameState(GameStates.EDIT);
        super.reset();
        showNewMapForm = false;
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (showNewMapForm) {
            if (newMapForm.getBounds().contains(x, y))
                newMapForm.mousePressed(x, y, button);
        } else if (selectedMap == null)
            if (button == MouseEvent.BUTTON1 && newMapButton.getBounds().contains(x, y))
                newMapButton.setMousePressed(true);

        if (showNewMapForm && button == MouseEvent.BUTTON1 && start.getBounds().contains(x, y))
            start.setMousePressed(true);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (mapList.isExpanded())
            showNewMapForm = false;
        if (showNewMapForm) {
            if (newMapForm.getBounds().contains(x, y)) {
                ExButton exButton = newMapForm.getExButton();
                if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                    showNewMapForm = false;
                    exButton.reset(x, y);
                } else {
                    newMapForm.mouseReleased(x, y, button);
                }
            }
        } else if (selectedMap == null)
            if (newMapButton.getBounds().contains(x, y) && newMapButton.isMousePressed()) {
                showNewMapForm = true;
                newMapForm.resetTextBoxes();
                mapList.setExpanded(false);
            }

        if (start.getBounds().contains(x, y) && start.isMousePressed())
            if (showNewMapForm && selectedMap == null)
                startNewMap();
            else if (!showNewMapForm && selectedMap != null)
                startMapEditor(selectedMap);

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
        } else if (selectedMap == null)
            if (newMapButton.getBounds().contains(x, y))
                newMapButton.setMouseOver(true);

        if (showNewMapForm && start.getBounds().contains(x, y))
            start.setMouseOver(true);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
    }

    public void keyPressed(KeyEvent e) {
        if (showNewMapForm)
            newMapForm.keyPressed(e);
    }

}
