package src.ui.buttons;

import java.awt.Rectangle;

public class Button {

	public static final int TEXT_SMALL = 0;
	public static final int TEXT_LARGE = 1;
	public static final int TILE = 2;
	public static final int EX = 3;
	public static final int DROP_DOWN = 4;

	public static final int DD_DOWN = 0;
	public static final int DD_UP = 1;

	protected Rectangle bounds;

	protected int index;
	protected int x, y, width, height;

	protected boolean mouseOver, mousePressed, disabled;

	public Button(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		bounds = new Rectangle(x, y, width, height);
	}

	public static int getButtonWidth(int buttonType) {
		switch (buttonType) {
		case TEXT_SMALL:
			return 56;
		case TEXT_LARGE:
			return 168;
		case TILE:
			return 76;
		case EX:
			return 24;
		case DROP_DOWN:
			return 42;
		}
		return 0;
	}

	public static int getButtonHeight(int buttonType) {
		switch (buttonType) {
		case TEXT_SMALL:
		case EX:
			return 27;
		case TEXT_LARGE:
			return 81;
		case TILE:
			return 83;
		case DROP_DOWN:
			return 42;
		}
		return 0;
	}

	public static int getButtonOffset(int buttonType) {
		switch (buttonType) {
		case TEXT_SMALL:
		case EX:
			return 3;
		case TEXT_LARGE:
		case TILE:
			return 7;
		case DROP_DOWN:
			return 5;
		}
		return 0;
	}

	public void update() {
		if (disabled)
			index = 3;
		else {
			index = 0;
			if (mouseOver)
				index = 1;
			if (mousePressed)
				index = 2;
		}
	}

	public void reset() {
		mouseOver = false;
		mousePressed = false;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isMouseOver() {
		return mouseOver;
	}

	public void setMouseOver(boolean mouseOver) {
		if (!mouseOver || (mouseOver && !disabled))
			this.mouseOver = mouseOver;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	public void setMousePressed(boolean mousePressed) {
		if (!mousePressed || (mousePressed && !disabled))
			this.mousePressed = mousePressed;
	}

	public Rectangle getBounds() {
		return bounds;
	}

}
