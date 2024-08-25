package ui.overlays;

import static ui.TextBox.*;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import objects.Map;
import ui.TextBox;
import utils.RenderText;

public class NewMapForm extends Overlay {

    private TextBox mapName, mapWidth, mapHeight;
    private ArrayList<TextBox> textBoxes = new ArrayList<TextBox>();
    private int numRows = 7;
    private int numColumns = 5;
    private float rowHeight, columnWidth;

    public NewMapForm(int x, int y) {
        super(OVERLAY_SMALL, x, y);
        this.rowHeight = getOverlayHeight(size) / numRows;
        this.columnWidth = getOverlayWidth(size) / numColumns;
        initTextBoxes();
    }

    private void initTextBoxes() {
        int nameX = x + (getOverlayWidth(size) - TextBox.getTextBoxWidth(TEXT)) / 2;
        int nameY = y + (int) (rowHeight * 2);
        mapName = new TextBox(TEXT, nameX, nameY);
        mapName.setFocus(true);

        int numberTextBoxWidth = TextBox.getTextBoxWidth(NUMBER);
        int widthX = x + (int) columnWidth + ((int) columnWidth - numberTextBoxWidth) / 2;
        int numberTextBoxY = y + (int) rowHeight * 5;
        mapWidth = new TextBox(NUMBER, widthX, numberTextBoxY);
        mapWidth.setCharLimit(3);

        int heightX = x + (int) columnWidth * 3 + ((int) columnWidth - numberTextBoxWidth) / 2;
        mapHeight = new TextBox(NUMBER, heightX, numberTextBoxY);
        mapHeight.setCharLimit(3);

        textBoxes.addAll(Arrays.asList(mapName, mapWidth, mapHeight));
        resetTextBoxes();
    }

    public void update() {
        super.update();
        for (TextBox tb : textBoxes)
            tb.update();
    }

    public void render(Graphics g) {
        super.render(g);
        g.setFont(Game.getGameFont(40f));
        String widthBounds = "(" + Map.MIN_WIDTH + " - " + Map.MAX_WIDTH + ")";
        String heightBounds = "(" + Map.MIN_HEIGHT + " - " + Map.MAX_HEIGHT + ")";

        RenderText.renderText(g, "Map Name", RenderText.CENTER, RenderText.CENTER, x, y + (int) rowHeight, getOverlayWidth(size), (int) rowHeight);
        RenderText.renderText(g, "Tile Width", RenderText.CENTER, RenderText.CENTER, x + (int) columnWidth, y + (int) rowHeight * 4, (int) columnWidth, (int) rowHeight);
        RenderText.renderText(g, "Tile Height", RenderText.CENTER, RenderText.CENTER, x + (int) columnWidth * 3, y + (int) rowHeight * 4, (int) columnWidth, (int) rowHeight);

        g.setFont(Game.getGameFont(20f));
        RenderText.renderText(g, widthBounds, RenderText.CENTER, RenderText.CENTER, x + (int) columnWidth, y + (int) rowHeight * 6, (int) columnWidth, (int) rowHeight);
        RenderText.renderText(g, heightBounds, RenderText.CENTER, RenderText.CENTER, x + (int) columnWidth * 3, y + (int) rowHeight * 6, (int) columnWidth, (int) rowHeight);
        for (TextBox tb : textBoxes)
            tb.render(g);
    }

    public void resetTextBoxes() {
        mapName.setText("");
        mapWidth.setText(Integer.toString(Map.MIN_WIDTH));
        mapHeight.setText(Integer.toString(Map.MIN_HEIGHT));
    }

    public boolean isValid() {
        return (!mapName.getText().isEmpty() && !mapWidth.getText().isEmpty() && !mapHeight.getText().isEmpty());
    }

    public void mousePressed(int x, int y, int button) {
        super.mousePressed(x, y, button);
    }

    public void mouseReleased(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            for (TextBox tb : textBoxes)
                if (tb.getBounds().contains(x, y))
                    tb.setFocus(true);
                else
                    tb.setFocus(false);
    }

    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
    }

    public void keyPressed(KeyEvent e) {
        for (TextBox tb : textBoxes)
            if (tb.getFocus())
                tb.keyPressed(e);
    }

    public String getMapName() {
        return mapName.getText();
    }

    public int getMapHeight() {
        return Integer.parseInt(mapHeight.getText());
    }

    public int getMapWidth() {
        return Integer.parseInt(mapWidth.getText());

    }

}
