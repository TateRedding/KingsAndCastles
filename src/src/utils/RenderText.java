package src.utils;

import java.awt.Font;
import java.awt.Graphics;

public class RenderText {

	public static final int RIGHT = 0;
	public static final int CENTER = 1;
	public static final int LEFT = 2;

	public static void renderText(Graphics g, String text, int alignment, int x, int y, int width, int height) {
		int lineWidth = g.getFontMetrics().stringWidth(text);
		int lineHeight = g.getFontMetrics().getHeight();
		int yStart = y + lineHeight - g.getFontMetrics().getDescent() + (height - lineHeight) / 2;
		int xStart = switch (alignment) {
		case RIGHT:
			yield x + width - lineWidth;
		case CENTER:
			yield x + (width - lineWidth) / 2;
		case LEFT:
		default:
			yield x;
		};
		g.drawString(text, xStart, yStart);
	}

	public static void renderText(Graphics g, String[] lines, int alignment, int x, int y, int width, int height) {
		int lineHeight = g.getFontMetrics().getHeight();
		int totalHeight = lineHeight * lines.length;
		int yStart = y + (height - totalHeight) / 2;
		for (String line : lines) {
			renderText(g, line, alignment, x, yStart, width, lineHeight);
			yStart += lineHeight;
		}
	}
	
	public static void renderTextBoxed(Graphics g, String[][] lines, Font leftFont, Font rightFont, int x, int y, int width, int height) {
		float rowHeight = height / lines.length;
		float yStart = y;
		for (String[] row : lines) {
			g.setFont(leftFont);
			renderText(g, row[0], LEFT, x, (int) yStart, width, (int) rowHeight);
			g.setFont(rightFont);
			renderText(g, row[1], RIGHT, x, (int) yStart, width, (int) rowHeight);
			yStart += rowHeight;
		}
	}
	
	public static void renderTextSplitCentered(Graphics g, String[][] lines, Font leftFont, Font rightFont, int x, int y, int width, int height) {
		float rowHeight = height / lines.length;
		float yStart = y;
		int halfWidth = width / 2;
		for (String[] row : lines) {
			g.setFont(leftFont);
			renderText(g, row[0], RIGHT, x, (int) yStart, halfWidth, (int) rowHeight);
			g.setFont(rightFont);
			renderText(g, row[1], LEFT, x + halfWidth, (int) yStart, halfWidth, (int) rowHeight);
			yStart += rowHeight;
		}
	}

}
