package ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import main.Game;
import utils.ImageLoader;
import utils.RenderText;

public class TextBox {

    public static final int NUMBER = 0;
    public static final int TEXT = 1;

    public static final int TEXT_BOX_HEIGHT = 48;

    private Font font;
    private String text = "";
    private Rectangle bounds;

    private int x, y;
    private int type;
    private int charLimit = 50;
    private int cursorTick, cursorTickMax = 35;
    private int cursorIndex;
    private int textXOffset, textXStart;
    private float maxFontSize = 40f;

    private boolean showCursor = true;
    private boolean focus;

    public TextBox(int type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.font = Game.getGameFont(maxFontSize);
        this.textXOffset = 48;
        this.textXStart = x + textXOffset;
        this.bounds = new Rectangle(x, y, getTextBoxWidth(type), TEXT_BOX_HEIGHT);
    }

    public static int getTextBoxWidth(int type) {
        return switch (type) {
            case TEXT -> 384;
            case NUMBER -> 144;
            default -> 0;
        };
    }

    public void update() {
        if (focus) {
            cursorTick++;
            if (cursorTick >= cursorTickMax) {
                cursorTick = 0;
                showCursor = !showCursor;
            }
        } else {
            showCursor = false;
            cursorTick = 0;
        }
    }

    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawImage(ImageLoader.textBoxBg[type], x, y, bounds.width, bounds.height, null);
        g.setFont(font);
        RenderText.renderText(g, text, RenderText.LEFT, RenderText.CENTER, textXStart, y, bounds.width - textXOffset * 2, bounds.height);

        if (showCursor)
            drawCursor(g);
    }

    private void drawCursor(Graphics g) {
        int cursorX = textXStart + 1;
        if (text != "")
            cursorX = textXStart + g.getFontMetrics().stringWidth(text.substring(0, cursorIndex)) - 1;
        int cursorOffset = 12;
        int cursorY = y + cursorOffset;
        g.drawRect(cursorX, cursorY, 1, bounds.height - cursorOffset * 2);
    }

    private boolean isAccepted(KeyEvent e) {
        String pattern = "";
        if (type == NUMBER)
            pattern = "\\d";
        else if (type == TEXT)
            pattern = "[a-zA-Z\\d\\p{Punct} ]";
        return String.valueOf(e.getKeyChar()).matches(pattern);
    }

    private void adjustFontSize() {
        Canvas temp = new Canvas();
        FontMetrics fm = temp.getFontMetrics(font);
        int maxTextWidth = bounds.width - textXOffset * 2;
        int textWidth = fm.stringWidth(text);
        float currFontSize = font.getSize();

        if (textWidth > maxTextWidth) {
            while (temp.getFontMetrics(font).stringWidth(text) > maxTextWidth) {
                currFontSize--;
                font = font.deriveFont(currFontSize);
            }
        } else if (textWidth < maxTextWidth) {
            while (temp.getFontMetrics(font.deriveFont(currFontSize + 1f)).stringWidth(text) < maxTextWidth
                    && currFontSize < maxFontSize) {
                currFontSize++;
                font = font.deriveFont(currFontSize);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (cursorIndex > 0) {
                text = text.substring(0, cursorIndex - 1) + text.substring(cursorIndex, text.length());
                cursorIndex--;
                adjustFontSize();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && cursorIndex > 0) {
            cursorIndex--;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && cursorIndex < text.length()) {
            cursorIndex++;
        } else if (isAccepted(e) && text.length() < charLimit) {
            text = text.substring(0, cursorIndex) + e.getKeyChar() + text.substring(cursorIndex, text.length());
            cursorIndex++;
            adjustFontSize();
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.cursorIndex = text.length();
        adjustFontSize();
    }

    public boolean getFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
        if (focus) {
            cursorTick = 0;
            showCursor = true;
        }
    }

    public void setCharLimit(int charLimit) {
        this.charLimit = charLimit;
    }

}
