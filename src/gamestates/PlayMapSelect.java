package gamestates;

import main.Game;
import objects.Map;
import ui.buttons.Button;
import ui.buttons.ImageButton;
import ui.overlays.NameGame;
import ui.overlays.Overlay;
import utils.LoadSave;
import utils.RenderText;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class PlayMapSelect extends MapSelect {

    private NameGame nameGame;

    private boolean naming;

    public PlayMapSelect(Game game) {
        super(game);
        int ngX = (Game.SCREEN_WIDTH - Overlay.getOverlayWidth(Overlay.OVERLAY_SMALL)) / 2;
        int ngY = (Game.SCREEN_HEIGHT - Overlay.getOverlayHeight(Overlay.OVERLAY_SMALL)) / 2;
        nameGame = new NameGame(ngX, ngY);
    }

    @Override
    public void update() {
        if (deleting)
            naming = false;
        if (naming) {
            nameGame.update();
        }
        super.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (selectedFile == null) {
            g.setFont(Game.getGameFont(72f));
            g.setColor(Color.BLACK);
            RenderText.renderText(g, "Select a map from the list above.", RenderText.CENTER, RenderText.CENTER, 0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        }
        if (naming)
            nameGame.render(g);
    }

    private void startNewGame(String name) {
        // The use of nanotime() is a placeholder until/if accounts are ever created
        if (selectedFile instanceof Map selectedMap) {
            Play newGame = new Play(game, selectedMap, name, System.nanoTime());
            LoadSave.saveGame(newGame);
            game.startGame(newGame);
            game.getSaveFileHandler().getGames().add(0, newGame);
            nameGame.resetChoice();
        }
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (naming && nameGame.getBounds().contains(x, y))
            nameGame.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (start.getBounds().contains(x, y) && start.isMousePressed()) {
                deleting = false;
                naming = true;
                nameGame.setName("");
            } else if (naming && nameGame.getBounds().contains(x, y)) {
                ImageButton exButton = nameGame.getExButton();
                if (exButton.getBounds().contains(x, y) && exButton.isMousePressed()) {
                    naming = false;
                    exButton.reset(x, y);
                } else {
                    nameGame.mouseReleased(x, y, button);
                    int choice = nameGame.getChoice();
                    if (choice != -1) {
                        naming = false;
                        if (choice == NameGame.GO)
                            startNewGame(nameGame.getName());
                    }
                }
                nameGame.resetChoice();
            }
        }
        start.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        if (naming) {
            nameGame.mouseMoved(x, y);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (naming)
            nameGame.keyPressed(e);
    }
}