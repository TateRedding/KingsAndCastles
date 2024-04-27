package gamestates;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.Game;
import utils.RenderText;

public class PlayMapSelect extends MapSelect implements StateMethods {

    private boolean naming;

    public PlayMapSelect(Game game) {
        super(game);
    }

    @Override
    public void update() {
        if (deleting)
            naming = false;
        super.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (selectedMap == null) {
            g.setFont(Game.getGameFont(72f));
            RenderText.renderText(g, "Select a map from the list above.", RenderText.CENTER, RenderText.CENTER, 0, 0, Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (start.getBounds().contains(x, y) && start.isMousePressed()) {
            System.out.println("start new game");
        }
        start.reset(x, y);
    }

}
