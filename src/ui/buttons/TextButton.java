package ui.buttons;

import main.Game;
import utils.RenderText;

import java.awt.*;

public class TextButton extends Button {

    private String text;
    private float fontSize;

    public TextButton(int buttonType, int x, int y, float fontSize, String text) {
        super(buttonType, x, y);
        this.fontSize = fontSize;
        this.text = text;
    }

    public void render(Graphics g) {
        super.render(g);
        drawText(g);
    }

    private void drawText(Graphics g) {
        int offset = getButtonOffset(buttonType);
        int textAreaHeight = height - offset;
        int yStart = y;
        if (mousePressed) {
            yStart += offset;
        }

        g.setFont(Game.getGameFont(fontSize));
        if (disabled)
            g.setColor(new Color(0, 0, 0, 100));
        else
            g.setColor(Color.BLACK);
        RenderText.renderText(g, text, RenderText.CENTER, RenderText.CENTER, x, yStart, width, textAreaHeight);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}