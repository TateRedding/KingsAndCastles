package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import main.Game;
import ui.buttons.Button;
import ui.buttons.DropDownButton;
import ui.buttons.ExButton;
import utils.ImageLoader;
import utils.RenderText;

import static ui.buttons.Button.*;

public class DropDownMenu {

    private DropDownButton ddButton;
    protected ExButton unselect;
    private Rectangle bounds;

    private String text;
    private String[] options;
    private ArrayList<Rectangle> rowBounds = new ArrayList<Rectangle>();
    private int x, y;
    private int topHeight, bodyHeight, width;
    private int selectedIndex = -1, hoverIndex = -1, startIndex = 0;
    private int numRows, maxStartIndex;
    private int rowWidth, rowX, rowXOffset = 48;
    private float rowHeight, maxFontSize = 46f;
    private boolean expanded;

    public DropDownMenu(String text, String[] options, int numRows, int x, int y) {
        this.text = text;
        this.options = options;
        this.numRows = numRows;
        this.maxStartIndex = options.length - numRows;
        this.x = x;
        this.y = y;
        this.topHeight = ImageLoader.dropDownTop.getHeight();
        this.bodyHeight = ImageLoader.dropDownBody.getHeight();
        this.width = ImageLoader.dropDownTop.getWidth();
        this.rowHeight = bodyHeight / numRows;
        this.rowWidth = width - rowXOffset * 2;
        this.rowX = x + rowXOffset;
        this.bounds = new Rectangle(x, y, width, topHeight);
        this.ddButton = new DropDownButton(DD_DOWN, x + bounds.width - getButtonWidth(DROP_DOWN), y);

        int unselectX = bounds.x + (bounds.height - getButtonWidth(EX)) / 2;
        int unselectY = bounds.y + (bounds.height - getButtonHeight(EX)) / 2;
        this.unselect = new ExButton(unselectX, unselectY);

        float yStart = y + topHeight;
        int boundsXOffset = 8;
        for (int i = 0; i < 5 && i < options.length; i++) {
            rowBounds.add(new Rectangle(rowX - boundsXOffset, (int) yStart, rowWidth + boundsXOffset * 2, (int) rowHeight));
            yStart += rowHeight;
        }
    }

    public void update() {
        ddButton.update();
        bounds.height = topHeight;
        if (expanded)
            bounds.height += bodyHeight;
        else {
            hoverIndex = -1;
            startIndex = 0;
        }
        if (selectedIndex != -1)
            unselect.update();
    }

    public void render(Graphics g) {
        g.drawImage(ImageLoader.dropDownTop, x, y, null);
        g.setColor(Color.BLACK);
        g.setFont(Game.getGameFont(46f));
        if (selectedIndex != -1) {
            RenderText.renderText(g, options[selectedIndex], RenderText.CENTER, RenderText.CENTER, x, y, bounds.width, topHeight);
        } else {
            RenderText.renderText(g, text, RenderText.CENTER, RenderText.CENTER, x, y, bounds.width, topHeight);
        }

        ddButton.render(g);
        if (selectedIndex != -1)
            unselect.render(g);

        if (expanded) {
            g.drawImage(ImageLoader.dropDownBody, x, y + topHeight, null);
            renderOptions(g);
            renderScrollBar(g);
            if (hoverIndex != -1) {
                Rectangle currRowBounds = rowBounds.get(hoverIndex);
                g.setColor(new Color(0, 0, 0, 75));
                g.fillRect(currRowBounds.x, currRowBounds.y, currRowBounds.width, currRowBounds.height);
            }
        }
    }

    private void renderOptions(Graphics g) {
        float yStart = y + topHeight;
        for (int i = startIndex; i < startIndex + numRows && i < options.length; i++) {
            float fontSize = 46f;
            while (g.getFontMetrics(Game.getGameFont(fontSize)).stringWidth(options[i]) > rowWidth)
                fontSize--;
            g.setFont(Game.getGameFont(fontSize));
            RenderText.renderText(g, options[i], RenderText.LEFT, RenderText.CENTER, rowX, (int) yStart, rowWidth, (int) rowHeight);
            yStart += rowHeight;
            if (i != startIndex + numRows - 1 && i != options.length - 1)
                g.drawRect(rowX, (int) yStart - 1, rowWidth, 1);
        }
    }

    private void renderScrollBar(Graphics g) {
        int yOffset = 16;
        int xOffset = 16;
        int containerWidth = 16;
        int totalHeight = bodyHeight - yOffset * 2;
        int yStart = y + topHeight + yOffset;
        int xStart = x + width - containerWidth - xOffset;

        float scrollHeight = totalHeight;
        if (options.length > 0)
            scrollHeight /= options.length;
        int barHeight = (int) (scrollHeight * numRows);
        int barOffset = 1;
        if (barHeight > totalHeight)
            barHeight = totalHeight;

        g.setColor(new Color(64, 27, 0));
        g.fillRect(xStart, yStart, containerWidth, totalHeight);

        g.setColor(new Color(230, 127, 92));
        int barYStart = yStart + barOffset + (int) (startIndex * scrollHeight);
        if (startIndex == maxStartIndex) {
            barYStart = yStart + totalHeight - barHeight + barOffset;
        }
        g.fillRect(xStart + barOffset, barYStart, containerWidth - barOffset * 2, barHeight - barOffset * 2);

    }

    private void setButtonDirection() {
        if (expanded)
            ddButton.setDirection(DD_UP);
        else
            ddButton.setDirection(DD_DOWN);
    }

    public void resetIndicies() {
        selectedIndex = -1;
        hoverIndex = -1;
        startIndex = 0;
    }

    public void mousePressed(int x, int y, int button) {
        if (button == MouseEvent.BUTTON1)
            if (ddButton.getBounds().contains(x, y))
                ddButton.setMousePressed(true);
            else if (selectedIndex != -1 && unselect.getBounds().contains(x, y))
                unselect.setMousePressed(true);
    }

    public void mouseReleased(int x, int y, int button) {
        if (bounds.contains(x, y) && button == MouseEvent.BUTTON1) {
            if (ddButton.getBounds().contains(x, y) && ddButton.isMousePressed())
                setExpanded(!expanded);
            else if (selectedIndex != -1 && unselect.getBounds().contains(x, y) && unselect.isMousePressed())
                selectedIndex = -1;
            for (Rectangle rowBounds : rowBounds)
                if (rowBounds.contains(x, y) && hoverIndex != -1) {
                    selectedIndex = hoverIndex + startIndex;
                    setExpanded(false);
                }
        }
        ddButton.reset(x, y);
        unselect.reset(x, y);
    }

    public void mouseMoved(int x, int y) {
        ddButton.setMouseOver(false);
        unselect.setMouseOver(false);
        if (ddButton.getBounds().contains(x, y))
            ddButton.setMouseOver(true);
        else if (selectedIndex != -1 && unselect.getBounds().contains(x, y))
            unselect.setMouseOver(true);

        hoverIndex = -1;
        for (int i = 0; i < rowBounds.size(); i++)
            if (rowBounds.get(i).contains(x, y))
                hoverIndex = i;

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() == -1) {
            if (startIndex > 0) {
                startIndex--;
            }
        } else if (startIndex < maxStartIndex)
            startIndex++;

    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        setButtonDirection();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

}
