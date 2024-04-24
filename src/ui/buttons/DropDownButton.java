package ui.buttons;

import java.awt.Graphics;

import utils.ImageLoader;

public class DropDownButton extends Button {

    private int direction;

    public DropDownButton(int direction, int x, int y) {
        super(x, y, getButtonWidth(DROP_DOWN), getButtonHeight(DROP_DOWN));
        this.direction = direction;
        this.x = x;
        this.y = y;
    }

    public void render(Graphics g) {
        g.drawImage(ImageLoader.dropDownButtons[direction][index], x, y, null);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
