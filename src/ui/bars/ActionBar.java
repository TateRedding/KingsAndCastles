package ui.bars;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import gamestates.Play;

public class ActionBar extends BottomBar {

    private Play play;

    public ActionBar(Play play) {
        this.play = play;
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
    }

    @Override
    public void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (save.getBounds().contains(x, y) && save.isMousePressed())
                play.saveGame();
        save.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
    }

    public Play getPlay() {
        return play;
    }

}
