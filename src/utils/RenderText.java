package utils;

import main.Game;

import java.awt.*;

public class RenderText {

    public static final int RIGHT = 0;
    public static final int CENTER = 1;
    public static final int LEFT = 2;

    public static final int TOP = 0;
    public static final int BOTTOM = 2;

    public static void renderText(Graphics g, String text, int alignment, int justification, int x, int y, int width, int height) {
        int lineWidth = g.getFontMetrics().stringWidth(text);
        int lineHeight = g.getFontMetrics().getHeight();
        int xStart = switch (alignment) {
            case RIGHT:
                yield x + width - lineWidth;
            case CENTER:
                yield x + (width - lineWidth) / 2;
            case LEFT:
            default:
                yield x;
        };
        int baseY = y + lineHeight - g.getFontMetrics().getDescent();
        int yStart = switch (justification) {
            case TOP:
                yield baseY;
            case CENTER:
            default:
                yield baseY + (height - lineHeight) / 2;
            case BOTTOM:
                yield baseY + height - lineHeight;
        };
        g.drawString(text, xStart, yStart);
    }

    public static void renderText(Graphics g, String[] lines, int alignment, int justification, int x, int y, int width, int height) {
        int lineHeight = g.getFontMetrics().getHeight();
        int totalHeight = lineHeight * lines.length;
        float maxFontSize = g.getFont().getSize();
        int yStart = switch (justification) {
            case TOP:
            default:
                yield y;
            case CENTER:
                yield y + (height - totalHeight) / 2;
            case BOTTOM:
                yield y + height - totalHeight;
        };
        for (String line : lines) {
            float fontSize = maxFontSize;
            while (g.getFontMetrics(Game.getGameFont(fontSize)).stringWidth(line) > width && fontSize > 0)
                fontSize -= 2;
            g.setFont(Game.getGameFont(fontSize));
            renderText(g, line, alignment, CENTER, x, yStart, width, lineHeight);
            yStart += lineHeight;
        }
    }

    public static void renderTextBoxed(Graphics g, String[][] lines, Font leftFont, Font rightFont, int x, int y, int width, int height) {
        float rowHeight = height / lines.length;
        float yStart = y;
        for (String[] row : lines) {
            g.setFont(leftFont);
            renderText(g, row[0], LEFT, CENTER, x, (int) yStart, width, (int) rowHeight);
            g.setFont(rightFont);
            renderText(g, row[1], RIGHT, CENTER, x, (int) yStart, width, (int) rowHeight);
            yStart += rowHeight;
        }
    }

}
