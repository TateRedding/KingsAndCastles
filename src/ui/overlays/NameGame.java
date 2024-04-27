package ui.overlays;

import main.Game;
import ui.TextBox;
import ui.buttons.TextButton;
import utils.RenderText;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static ui.TextBox.*;
import static ui.buttons.Button.*;

public class NameGame extends Overlay {

    public static final int CANCEL = 0;
    public static final int GO = 1;

    private TextBox name;
    private TextButton go, cancel;

    private int choice = -1;
    private int numRows = 7;
    private int numColumns = 5;
    private float rowHeight = OVERLAY_HEIGHT / numRows;
    private float columnWidth = OVERLAY_WIDTH / numColumns;

    public NameGame(int x, int y) {
        super(x, y);

        int nameX = x + (bounds.width - getTextBoxWidth(TEXT)) / 2;
        int nameY = y + (int) (rowHeight * 3) - TEXT_BOX_HEIGHT / 2;
        name = new TextBox(TEXT, nameX, nameY);

        float buttonFontSize = 48f;
        int goX = x + (int) columnWidth + ((int) columnWidth - getButtonWidth(TEXT_LARGE)) / 2;
        int buttonY = y + (int) rowHeight * 5 - getButtonHeight(TEXT_LARGE) / 2;
        go = new TextButton(TEXT_LARGE, "Go", buttonFontSize, goX, buttonY);

        int cancelX = x + (int) columnWidth * 3 + ((int) columnWidth - getButtonWidth(TEXT_LARGE)) / 2;
        cancel = new TextButton(TEXT_LARGE, "Cancel", buttonFontSize, cancelX, buttonY);
    }

    @Override
    public void update() {
        super.update();
        name.update();
        go.update();
        cancel.update();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        name.render(g);
        go.render(g);
        cancel.render(g);

        int messageY = y + (int) rowHeight;
        g.setFont(Game.getGameFont(36f));
        RenderText.renderText(g, "Enter a name for your game:", RenderText.CENTER, RenderText.CENTER, x, messageY, OVERLAY_WIDTH, (int) rowHeight);
    }

    @Override
    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
        if (button == MouseEvent.BUTTON1) {
            if (name.getBounds().contains(x, y))
                name.setFocus(true);
            else if (go.getBounds().contains(x, y))
                go.setMousePressed(true);
            else if (cancel.getBounds().contains(x, y))
                cancel.setMousePressed(true);
        }
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1) {
            if (go.getBounds().contains(x, y) && go.isMousePressed())
                choice = GO;
            else if (cancel.getBounds().contains(x, y) && cancel.isMousePressed())
                choice = CANCEL;
        }
        go.reset(x, y);
        cancel.reset(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        go.setMouseOver(false);
        cancel.setMouseOver(false);
        if (go.getBounds().contains(x, y))
            go.setMouseOver(true);
        else if (cancel.getBounds().contains(x, y))
            cancel.setMouseOver(true);
    }

    public void keyPressed(KeyEvent e) {
        if (name.getFocus())
            name.keyPressed(e);
    }

    public int getChoice() {
        return choice;
    }

    public void resetChoice() {
        choice = -1;
    }

    public String getName() {
        return name.getText();
    }

    public void setName(String text) {
        name.setText(text);
    }

}
