package ui.overlays;

import main.Game;
import ui.buttons.Button;
import ui.buttons.TextButton;
import utils.RenderText;

import java.awt.*;
import java.awt.event.MouseEvent;

import static ui.buttons.Button.TEXT_LARGE;

public class DeleteConfirm extends Overlay {

    public static final int NO = 0;
    public static final int YES = 1;

    private TextButton yes, no;

    private String deleteName;
    private int choice = -1;

    public DeleteConfirm(String deleteName, int x, int y) {
        super(OVERLAY_SMALL, x, y);
        this.deleteName = deleteName;

        float buttonFontSize = 62f;
        int buttonY = bounds.y + bounds.height / 2;
        int buttonWidth = Button.getButtonWidth(TEXT_LARGE);
        int buttonOffset = (bounds.width - buttonWidth * 2) / 3;
        int yesX = bounds.x + buttonOffset;
        yes = new TextButton(TEXT_LARGE, yesX, buttonY, buttonFontSize, "Delete");

        int noX = bounds.x + bounds.width - buttonWidth - buttonOffset;
        no = new TextButton(TEXT_LARGE, noX, buttonY, buttonFontSize, "No");
    }

    @Override
    public void update() {
        super.update();
        yes.update();
        no.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        String[] message = {"Are you sure you'd like to delete", deleteName + "?"};
        g.setColor(Color.BLACK);
        g.setFont(Game.getGameFont(42f));
        RenderText.renderText(g, message, RenderText.CENTER, RenderText.CENTER, bounds.x, bounds.y, bounds.width, bounds.height / 2);

        yes.render(g);
        no.render(g);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1)
            if (yes.getBounds().contains(x, y))
                yes.setMousePressed(true);
            else if (no.getBounds().contains(x, y))
                no.setMousePressed(true);
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (yes.getBounds().contains(x, y) && yes.isMousePressed())
                choice = YES;
            else if (no.getBounds().contains(x, y) && no.isMousePressed())
                choice = NO;
        yes.reset(x, y);
        no.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        yes.setMouseOver(false);
        no.setMouseOver(false);
        if (yes.getBounds().contains(x, y))
            yes.setMouseOver(true);
        else if (no.getBounds().contains(x, y))
            no.setMouseOver(true);
    }

    public int getChoice() {
        return choice;
    }

    public void resetChoice() {
        choice = -1;
    }

    public void setDeleteName(String deleteName) {
        this.deleteName = deleteName;
    }

}
