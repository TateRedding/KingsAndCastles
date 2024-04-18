package ui.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import utils.LoadSave;
import utils.RenderText;
import utils.ImageLoader;

public class TextButton extends Button {

	private String text;
	private int buttonSize;
	private float fontSize;

	public TextButton(int buttonSize, String text, float fontSize, int x, int y) {
		super(x, y, getButtonWidth(buttonSize), getButtonHeight(buttonSize));
		this.buttonSize = buttonSize;
		this.text = text;
		this.fontSize = fontSize;
	}

	public void render(Graphics g) {
		drawBackground(g);
		drawText(g);
	}

	private void drawBackground(Graphics g) {
		Image bg = switch (buttonSize) {
		case TEXT_SMALL -> ImageLoader.smallTextButton[index];
		case TEXT_LARGE -> ImageLoader.largeTextButton[index];
		default -> null;
		};

		if (bg != null)
			g.drawImage(bg, x, y, width, height, null);
	}

	private void drawText(Graphics g) {
		int offset = getButtonOffset(buttonSize);
		int textAreaHeight = height - offset;
		int yStart = y;
		if (mousePressed) {
			yStart += offset;
		}

		g.setFont(LoadSave.getGameFont(Font.BOLD, fontSize));
		if (disabled)
			g.setColor(new Color(0, 0, 0, 100));
		else
			g.setColor(Color.BLACK);
		RenderText.renderText(g, text, RenderText.CENTER, x, yStart, width, textAreaHeight);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}