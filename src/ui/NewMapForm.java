package ui;

import static ui.TextBox.*;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import main.Game;
import objects.Map;
import utils.ImageLoader;
import utils.RenderText;

public class NewMapForm extends Overlay {

    private TextBox mapName, mapWidth, mapHeight;
    private ArrayList<TextBox> textBoxes = new ArrayList<TextBox>();
    private int numRows = 7;
    private int numColumns = 5;
    private float rowHeight = height / numRows;
    private float columnWidth = width / numColumns;

    public NewMapForm(int x, int y) {
        super(x, y);
        initTextBoxes();
    }

    private void initTextBoxes() {
        int xStart = x + (width - ImageLoader.textBoxBg[TEXT].getWidth()) / 2;
        int yStart = y + (int) (rowHeight * 2);
        mapName = new TextBox(TEXT, xStart, yStart);
        mapName.setFocus(true);

        int numberTextBoxWidth = ImageLoader.textBoxBg[NUMBER].getWidth();
        xStart = x + (int) columnWidth + ((int) columnWidth - numberTextBoxWidth) / 2;
        yStart = y + (int) rowHeight * 5;
        mapWidth = new TextBox(NUMBER, xStart, yStart);
        mapWidth.setCharLimit(3);

        xStart = x + (int) columnWidth * 3 + ((int) columnWidth - numberTextBoxWidth) / 2;
        mapHeight = new TextBox(NUMBER, xStart, yStart);
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
        String widthBounds = "(" + Integer.toString(Map.MIN_WIDTH) + " - " + Integer.toString(Map.MAX_WIDTH) + ")";
        String heightBounds = "(" + Integer.toString(Map.MIN_HEIGHT) + " - " + Integer.toString(Map.MAX_HEIGHT) + ")";

        RenderText.renderText(g, "Map Name", RenderText.CENTER, RenderText.CENTER, x, y + (int) rowHeight, width, (int) rowHeight);
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
